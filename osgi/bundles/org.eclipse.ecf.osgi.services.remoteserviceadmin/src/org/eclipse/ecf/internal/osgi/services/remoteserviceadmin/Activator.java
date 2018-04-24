/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.util.BundleStarter;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionLocator;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IServiceInfoFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.ServiceInfoFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Capability;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.namespace.DiscoveryNamespace;
import org.osgi.service.remoteserviceadmin.namespace.DistributionNamespace;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.remoteserviceadmin"; //$NON-NLS-1$

	private static final String RSA_PROXY_BUNDLE_SYMBOLIC_ID = "org.eclipse.ecf.osgi.services.remoteserviceadmin.proxy"; //$NON-NLS-1$

	private static BundleContext context;
	private static Activator instance;

	private static final String[] DEPENDENT_BUNDLES = new String[] { "org.eclipse.ecf.identity", "org.eclipse.ecf", //$NON-NLS-1$ //$NON-NLS-2$
			"org.eclipse.ecf.discovery", "org.eclipse.ecf.remoteservice", "org.eclipse.ecf.remoteservice.asyncproxy" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}

	public IContainerManager getContainerManager() {
		return (IContainerManager) ContainerFactory.getDefault();
	}

	private ServiceRegistration remoteServiceAdminRegistration;
	private Dictionary rsaProps;
	private ServiceTracker<ContainerTypeDescription, ContainerTypeDescription> ctdTracker;

	private EndpointDescriptionLocator endpointDescriptionLocator;
	private ServiceRegistration<?> iServiceInfoFactoryRegistration;

	// Logging
	private ServiceTracker logServiceTracker = null;
	private LogService logService = null;
	private Object logServiceTrackerLock = new Object();
	// Sax parser
	private Object saxParserFactoryTrackerLock = new Object();
	private ServiceTracker saxParserFactoryTracker;

	private BundleContext proxyServiceFactoryBundleContext;

	private Collection<ExportRegistration> exportedRegistrations;
	private Collection<ImportRegistration> importedRegistrations;

	private void initializeProxyServiceFactoryBundle() throws Exception {
		// First, find proxy bundle
		for (Bundle b : context.getBundles()) {
			if (RSA_PROXY_BUNDLE_SYMBOLIC_ID.equals(b.getSymbolicName())) {
				// first start it
				b.start();
				// then get its bundle context
				proxyServiceFactoryBundleContext = b.getBundleContext();
			}
		}
		if (proxyServiceFactoryBundleContext == null)
			throw new IllegalStateException("RSA Proxy bundle (symbolic id=='" //$NON-NLS-1$
					+ RSA_PROXY_BUNDLE_SYMBOLIC_ID + "') cannot be found, so RSA cannot be started"); //$NON-NLS-1$
	}

	private void initializeProviders(Bundle hostBundle, String namespace, String startErrorMessage) {
		for (final Bundle b : context.getBundles()) {
			BundleRevision rb = AccessController.doPrivileged(new PrivilegedAction<BundleRevision>() {
				public BundleRevision run() {
					return b.adapt(BundleRevision.class);
				}
			});
			List<Capability> capabilities = rb.getCapabilities(namespace);
			if (capabilities != null && capabilities.size() > 0
					&& !b.getSymbolicName().equals(hostBundle.getSymbolicName()))
				try {
					b.start();
				} catch (BundleException e) {
					LogUtility.logError("RemoteServiceAdmin.initializeProviders", DebugOptions.REMOTE_SERVICE_ADMIN, //$NON-NLS-1$
							Activator.class, startErrorMessage + " bundle=" + b.getSymbolicName(), e); //$NON-NLS-1$
				}
		}
	}

	private void stopProxyServiceFactoryBundle() {
		if (proxyServiceFactoryBundleContext != null) {
			// stop it
			try {
				proxyServiceFactoryBundleContext.getBundle().stop();
			} catch (Exception e) {
				// we don't care
			}
			proxyServiceFactoryBundleContext = null;
		}
	}

	public BundleContext getProxyServiceFactoryBundleContext(EndpointDescription endpointDescription) {
		return proxyServiceFactoryBundleContext;
	}

	private Map<Bundle, RemoteServiceAdmin> remoteServiceAdmins = new HashMap<Bundle, RemoteServiceAdmin>(1);

	private void removeSupportedConfigsAndIntents(ContainerTypeDescription ctd) {
		String[] remoteConfigsSupported = (String[]) rsaProps
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
		List<String> rcs = new ArrayList<String>();
		if (remoteConfigsSupported != null)
			for (int i = 0; i < remoteConfigsSupported.length; i++)
				rcs.add(remoteConfigsSupported[i]);
		String[] remoteIntentsSupported = (String[]) rsaProps
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED);
		List<String> ris = new ArrayList<String>();
		if (remoteIntentsSupported != null)
			for (int i = 0; i < remoteIntentsSupported.length; i++)
				ris.add(remoteIntentsSupported[i]);

		String[] descSupportedConfigs = ctd.getSupportedConfigs();
		if (descSupportedConfigs != null) {
			for (int j = 0; j < descSupportedConfigs.length; j++)
				rcs.remove(descSupportedConfigs[j]);
			String[] descSupportedIntents = ctd.getSupportedIntents();
			for (int j = 0; j < descSupportedIntents.length; j++)
				ris.remove(descSupportedIntents[j]);
		}
		// set rsaProps to new values
		rsaProps.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
				rcs.toArray(new String[rcs.size()]));
		rsaProps.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED,
				ris.toArray(new String[ris.size()]));
	}

	void addSupportedConfigsAndIntents(ContainerTypeDescription desc) {
		// Get the existing remoteConfigsSupported from rsaProps
		String[] remoteConfigsSupported = (String[]) rsaProps
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
		// Add all the existing to rcs list
		List<String> rcs = new ArrayList<String>();
		if (remoteConfigsSupported != null)
			for (int i = 0; i < remoteConfigsSupported.length; i++)
				rcs.add(remoteConfigsSupported[i]);
		// Get the existing remoteIntentsSupported from rsaProps
		String[] remoteIntentsSupported = (String[]) rsaProps
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED);
		// Add all the existing to ris list
		List<String> ris = new ArrayList<String>();
		if (remoteIntentsSupported != null)
			for (int i = 0; i < remoteIntentsSupported.length; i++)
				ris.add(remoteIntentsSupported[i]);

		// Get the supported configs from the given description
		String[] descSupportedConfigs = desc.getSupportedConfigs();

		if (descSupportedConfigs != null) {
			// Add all supported configs...as long as they are not already
			// present
			for (int j = 0; j < descSupportedConfigs.length; j++)
				if (!rcs.contains(descSupportedConfigs[j]))
					rcs.add(descSupportedConfigs[j]);
			// Get supported intents
			String[] descSupportedIntents = desc.getSupportedIntents();
			// Add them if not already present
			for (int j = 0; j < descSupportedIntents.length; j++)
				if (!ris.contains(descSupportedIntents[j]))
					ris.add(descSupportedIntents[j]);
		}
		// set rsaProps to new values
		rsaProps.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
				rcs.toArray(new String[rcs.size()]));
		rsaProps.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED,
				ris.toArray(new String[ris.size()]));
	}

	private void initializeDependents() {
		try {
			BundleStarter.startDependents(context, DEPENDENT_BUNDLES, Bundle.RESOLVED | Bundle.STARTING);
		} catch (BundleException e) {
			LogUtility.logError("RemoteServiceAdmin.initializeDependents", DebugOptions.REMOTE_SERVICE_ADMIN, //$NON-NLS-1$
					this.getClass(), "Cannot start RSA dependent bundle", e); //$NON-NLS-1$
		}
	}

	private void initializeSAXParserFactory() {
		ServiceReference<?> ref = Activator.context.getServiceReference(SAXParserFactory.class.getName());
		if (ref == null) 
			Activator.context.registerService(SAXParserFactory.class.getName(), SAXParserFactory.newInstance(), null);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.instance = this;
		this.exportedRegistrations = new ArrayList<ExportRegistration>();
		this.importedRegistrations = new ArrayList<ImportRegistration>();
		// start dependent bundles first
		initializeDependents();
		// initialize the RSA proxy service factory bundle...so that we
		// can get/use *that bundle's BundleContext for registering the
		// proxy ServiceFactory.
		// See osgi-dev thread here for info about this
		// approach/using the ServiceFactory extender approach for this purpose:
		// https://mail.osgi.org/pipermail/osgi-dev/2011-February/003000.html
		initializeProxyServiceFactoryBundle();
		// Start distribution providers if not already started
		initializeProviders(context.getBundle(), DistributionNamespace.DISTRIBUTION_NAMESPACE,
				"Could not start distribution provider. "); //$NON-NLS-1$
		// Start distribution providers if not already started
		initializeProviders(context.getBundle(), DiscoveryNamespace.DISCOVERY_NAMESPACE,
				"Could not start discovery provider. "); //$NON-NLS-1$
		// make remote service admin available
		rsaProps = new Properties();
		rsaProps.put(RemoteServiceAdmin.SERVICE_PROP, new Boolean(true));
		// Register Remote Service Admin factory, with rsaProps
		remoteServiceAdminRegistration = context.registerService(
				org.osgi.service.remoteserviceadmin.RemoteServiceAdmin.class.getName(), new ServiceFactory() {
					public Object getService(Bundle bundle, ServiceRegistration registration) {
						RemoteServiceAdmin result = null;
						synchronized (remoteServiceAdmins) {
							result = remoteServiceAdmins.get(bundle);
							if (result == null) {
								result = new RemoteServiceAdmin(bundle, exportedRegistrations, importedRegistrations);
								remoteServiceAdmins.put(bundle, result);
							}
						}
						return result;
					}

					public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
						synchronized (remoteServiceAdmins) {
							RemoteServiceAdmin rsa = remoteServiceAdmins.remove(bundle);
							if (rsa != null)
								rsa.close();
						}
					}
				}, (Dictionary) rsaProps);

		ctdTracker = new ServiceTracker<ContainerTypeDescription, ContainerTypeDescription>(context,
				ContainerTypeDescription.class,
				new ServiceTrackerCustomizer<ContainerTypeDescription, ContainerTypeDescription>() {
					public ContainerTypeDescription addingService(
							ServiceReference<ContainerTypeDescription> reference) {
						ContainerTypeDescription ctd = null;
						if (reference != null && context != null) {
							ctd = context.getService(reference);
							if (ctd != null) {
								// Add any new supported configs to rsaProps
								addSupportedConfigsAndIntents(ctd);
								if (remoteServiceAdminRegistration != null)
									// Set the new properties for
									// remoteServiceRegistration
									remoteServiceAdminRegistration.setProperties(rsaProps);
							}
						}
						return ctd;
					}

					public void modifiedService(ServiceReference<ContainerTypeDescription> reference,
							ContainerTypeDescription service) {
					}

					public void removedService(ServiceReference<ContainerTypeDescription> reference,
							ContainerTypeDescription service) {
						if (remoteServiceAdminRegistration != null && service != null) {
							// Remove supported configs and intents from
							// rsaProps
							removeSupportedConfigsAndIntents(service);
							// Reset properties for remoteServiceAdmin
							remoteServiceAdminRegistration.setProperties(rsaProps);
						}
					}
				});
		ctdTracker.open();

		// create endpoint description locator
		endpointDescriptionLocator = new EndpointDescriptionLocator(context);
		// create and register endpoint description advertiser
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING, new Integer(Integer.MIN_VALUE));
		iServiceInfoFactoryRegistration = context.registerService(IServiceInfoFactory.class.getName(),
				new ServiceInfoFactory(), (Dictionary) properties);

		// start endpointDescriptionLocator
		endpointDescriptionLocator.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (endpointDescriptionLocator != null) {
			endpointDescriptionLocator.close();
			endpointDescriptionLocator = null;
		}
		if (ctdTracker != null) {
			ctdTracker.close();
			ctdTracker = null;
		}
		if (remoteServiceAdminRegistration != null) {
			remoteServiceAdminRegistration.unregister();
			remoteServiceAdminRegistration = null;
		}
		if (iServiceInfoFactoryRegistration != null) {
			iServiceInfoFactoryRegistration.unregister();
			iServiceInfoFactoryRegistration = null;
		}
		synchronized (saxParserFactoryTrackerLock) {
			if (saxParserFactoryTracker != null) {
				saxParserFactoryTracker.close();
				saxParserFactoryTracker = null;
			}
		}
		synchronized (logServiceTrackerLock) {
			if (logServiceTracker != null) {
				logServiceTracker.close();
				logServiceTracker = null;
				logService = null;
			}
		}
		stopProxyServiceFactoryBundle();

		synchronized (importedRegistrations) {
			if (importedRegistrations != null) {
				importedRegistrations.clear();
				importedRegistrations = null;
			}
		}
		synchronized (exportedRegistrations) {
			if (exportedRegistrations != null) {
				exportedRegistrations.clear();
				exportedRegistrations = null;
			}
		}
		Activator.context = null;
		Activator.instance = null;
	}

	public boolean isOldEquinox() {
		if (context == null)
			return false;
		Bundle systemBundle = context.getBundle(0);
		String systemBSN = systemBundle.getSymbolicName();
		if ("org.eclipse.osgi".equals(systemBSN)) { //$NON-NLS-1$
			Version fixedVersion = new Version("3.7.0"); //$NON-NLS-1$
			// running on equinox; check the version
			Version systemVersion = systemBundle.getVersion();
			if (systemVersion.compareTo(fixedVersion) < 0)
				return true;
		}
		return false;
	}

	public String getFrameworkUUID() {
		if (context == null)
			return null;
		// code get and set the framework uuid property as specified in
		// r2.enterprise.pdf pg 297
		synchronized ("org.osgi.framework.uuid") { //$NON-NLS-1$
			String result = context.getProperty("org.osgi.framework.uuid"); //$NON-NLS-1$
			if (result == null) {
				UUID newUUID = UUID.randomUUID();
				result = newUUID.toString();
				System.setProperty("org.osgi.framework.uuid", //$NON-NLS-1$
						newUUID.toString());
			}
			return result;
		}
	}

	public SAXParserFactory getSAXParserFactory() {
		if (instance == null)
			return null;
		synchronized (saxParserFactoryTrackerLock) {
			if (saxParserFactoryTracker == null) {
				saxParserFactoryTracker = new ServiceTracker(context, SAXParserFactory.class.getName(), null);
				saxParserFactoryTracker.open();
			}
			SAXParserFactory result = (SAXParserFactory) saxParserFactoryTracker.getService();
			if (result == null) {
				result = SAXParserFactory.newInstance();
				context.registerService(SAXParserFactory.class.getName(), result, null);
			}
			return result;
		}
	}

	public LogService getLogService() {
		if (context == null)
			return null;
		synchronized (logServiceTrackerLock) {
			if (logServiceTracker == null) {
				logServiceTracker = new ServiceTracker(context, LogService.class.getName(), null);
				logServiceTracker.open();
			}
			logService = (LogService) logServiceTracker.getService();
			if (logService == null)
				logService = new SystemLogService(PLUGIN_ID);
			return logService;
		}
	}

	public void log(IStatus status) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(null, LogHelper.getLogCode(status), LogHelper.getLogMessage(status), status.getException());
	}

	public void log(ServiceReference sr, IStatus status) {
		log(sr, LogHelper.getLogCode(status), LogHelper.getLogMessage(status), status.getException());
	}

	public void log(ServiceReference sr, int level, String message, Throwable t) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(sr, level, message, t);
	}

}
