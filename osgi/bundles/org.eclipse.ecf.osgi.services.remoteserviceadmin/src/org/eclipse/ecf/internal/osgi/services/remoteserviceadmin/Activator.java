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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionAdvertiser;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionLocator;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionAdvertiser;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.remoteserviceadmin"; //$NON-NLS-1$

	private static BundleContext context;
	private static Activator instance;

	public static BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}

	private ServiceRegistration remoteServiceAdminRegistration;

	private EndpointDescriptionLocator endpointDescriptionLocator;
	private EndpointDescriptionAdvertiser endpointDescriptionAdvertiser;
	private ServiceRegistration endpointDescriptionAdvertiserRegistration;

	private ServiceTracker containerManagerTracker;
	// Logging
	private ServiceTracker logServiceTracker = null;
	private LogService logService = null;
	private Object logServiceTrackerLock = new Object();
	// Sax parser
	private Object saxParserFactoryTrackerLock = new Object();
	private ServiceTracker saxParserFactoryTracker;

	private static final String RSA_PROXY_BUNDLE_SYMBOLIC_ID = "org.eclipse.ecf.osgi.services.remoteserviceadmin.proxy"; //$NON-NLS-1$

	private BundleContext proxyServiceFactoryBundleContext;

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
					+ RSA_PROXY_BUNDLE_SYMBOLIC_ID
					+ "') cannot be found, so RSA cannot be started"); //$NON-NLS-1$
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

	public BundleContext getProxyServiceFactoryBundleContext() {
		return proxyServiceFactoryBundleContext;
	}

	private Map<Bundle, RemoteServiceAdmin> remoteServiceAdmins = new HashMap<Bundle, RemoteServiceAdmin>(
			1);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.instance = this;
		// initialize the RSA proxy service factory bundle...so that we
		// can get/use *that bundle's BundleContext for registering the
		// proxy ServiceFactory.
		// See osgi-dev thread here for info about this
		// approach/using the ServiceFactory extender approach for this purpose:
		// https://mail.osgi.org/pipermail/osgi-dev/2011-February/003000.html
		initializeProxyServiceFactoryBundle();

		// make remote service admin available
		Properties rsaProps = new Properties();
		rsaProps.put(RemoteServiceAdmin.SERVICE_PROP, new Boolean(true));
		remoteServiceAdminRegistration = context.registerService(
				org.osgi.service.remoteserviceadmin.RemoteServiceAdmin.class
						.getName(), new ServiceFactory() {
					public Object getService(Bundle bundle,
							ServiceRegistration registration) {
						RemoteServiceAdmin result = null;
						synchronized (remoteServiceAdmins) {
							RemoteServiceAdmin rsa = remoteServiceAdmins
									.get(bundle);
							if (rsa == null) {
								rsa = new RemoteServiceAdmin(bundle);
								remoteServiceAdmins.put(bundle, rsa);
							}
							result = rsa;
						}
						return result;
					}

					public void ungetService(Bundle bundle,
							ServiceRegistration registration, Object service) {
						synchronized (remoteServiceAdmins) {
							RemoteServiceAdmin rsa = remoteServiceAdmins
									.remove(bundle);
							if (rsa != null)
								rsa.close();
						}
					}
				}, (Dictionary) rsaProps);

		// create endpoint description locator
		endpointDescriptionLocator = new EndpointDescriptionLocator(context);
		// create and register endpoint description advertiser
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		endpointDescriptionAdvertiser = new EndpointDescriptionAdvertiser(
				endpointDescriptionLocator);
		endpointDescriptionAdvertiserRegistration = getContext()
				.registerService(
						IEndpointDescriptionAdvertiser.class.getName(),
						endpointDescriptionAdvertiser, (Dictionary) properties);

		// start endpointDescriptionLocator
		endpointDescriptionLocator.start();
	}

	private void clearRSAs() {
		synchronized (remoteServiceAdmins) {
			for (Iterator<Entry<Bundle, RemoteServiceAdmin>> i = remoteServiceAdmins
					.entrySet().iterator(); i.hasNext();) {
				Entry<Bundle, RemoteServiceAdmin> entry = i.next();
				RemoteServiceAdmin rsa = entry.getValue();
				rsa.close();
				i.remove();
			}
		}
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
		if (remoteServiceAdminRegistration != null) {
			remoteServiceAdminRegistration.unregister();
			remoteServiceAdminRegistration = null;
		}
		clearRSAs();
		if (endpointDescriptionAdvertiserRegistration != null) {
			endpointDescriptionAdvertiserRegistration.unregister();
			endpointDescriptionAdvertiserRegistration = null;
		}
		if (endpointDescriptionAdvertiser != null) {
			endpointDescriptionAdvertiser.close();
			endpointDescriptionAdvertiser = null;
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
		if (containerManagerTracker != null) {
			containerManagerTracker.close();
			containerManagerTracker = null;
		}
		stopProxyServiceFactoryBundle();
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
				saxParserFactoryTracker = new ServiceTracker(context,
						SAXParserFactory.class.getName(), null);
				saxParserFactoryTracker.open();
			}
			return (SAXParserFactory) saxParserFactoryTracker.getService();
		}
	}

	public LogService getLogService() {
		if (context == null)
			return null;
		synchronized (logServiceTrackerLock) {
			if (logServiceTracker == null) {
				logServiceTracker = new ServiceTracker(context,
						LogService.class.getName(), null);
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
			logService.log(null, LogHelper.getLogCode(status),
					LogHelper.getLogMessage(status), status.getException());
	}

	public void log(ServiceReference sr, IStatus status) {
		log(sr, LogHelper.getLogCode(status), LogHelper.getLogMessage(status),
				status.getException());
	}

	public void log(ServiceReference sr, int level, String message, Throwable t) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(sr, level, message, t);
	}

	public IContainerManager getContainerManager() {
		if (containerManagerTracker == null) {
			containerManagerTracker = new ServiceTracker(context,
					IContainerManager.class.getName(), null);
			containerManagerTracker.open();
		}
		return (IContainerManager) containerManagerTracker.getService();
	}
}
