/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.core;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.start.ECFStartJob;
import org.eclipse.ecf.core.start.IECFStart;
import org.eclipse.ecf.core.util.*;
import org.eclipse.ecf.internal.core.identity.Activator;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class ECFPlugin implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf"; //$NON-NLS-1$

	private static final String ECFNAMESPACE = PLUGIN_ID;

	private static final String CONTAINER_FACTORY_NAME = "containerFactory"; //$NON-NLS-1$

	private static final String CONTAINER_FACTORY_EPOINT = ECFNAMESPACE + "." + CONTAINER_FACTORY_NAME; //$NON-NLS-1$

	private static final String STARTUP_NAME = "start"; //$NON-NLS-1$

	public static final String START_EPOINT = ECFNAMESPACE + "." + STARTUP_NAME; //$NON-NLS-1$

	public static final String PLUGIN_RESOURCE_BUNDLE = ECFNAMESPACE + ".ECFPluginResources"; //$NON-NLS-1$

	public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	public static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$

	public static final String DESCRIPTION_ATTRIBUTE = "description"; //$NON-NLS-1$

	public static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$

	public static final String SERVER_ATTRIBUTE = "server"; //$NON-NLS-1$

	public static final String HIDDEN_ATTRIBUTE = "hidden"; //$NON-NLS-1$

	public static final String ASYNCH_ATTRIBUTE = "asynchronous"; //$NON-NLS-1$

	public static final String CONTAINER_NAME = "container"; //$NON-NLS-1$

	public static final String CONTAINER_EPOINT = ECFNAMESPACE + "." + CONTAINER_NAME; //$NON-NLS-1$

	public static final String FACTORY_ATTRIBUTE = "factoryName"; //$NON-NLS-1$

	public static final String ID_ATTRIBUTE = "containerId"; //$NON-NLS-1$

	public static final String PARAMETER_ELEMENT = "parameter"; //$NON-NLS-1$

	public static final String PARAMETER_NAME = "name"; //$NON-NLS-1$

	public static final String PARAMETER_VALUE = "value"; //$NON-NLS-1$

	// The shared instance.
	private static ECFPlugin plugin;

	private BundleContext context = null;

	private ServiceTracker extensionRegistryTracker = null;

	private Map disposables = new WeakHashMap();

	private IRegistryChangeListener registryManager = null;

	private ServiceRegistration containerFactoryServiceRegistration;

	private ServiceRegistration containerManagerServiceRegistration;

	private ServiceTracker logServiceTracker = null;

	private LogService logService = null;

	private ServiceTracker adapterManagerTracker = null;

	private BundleActivator ecfTrustManager;

	/**
	 * Returns the shared instance.
	 * @return ECFPlugin
	 */
	public synchronized static ECFPlugin getDefault() {
		if (plugin == null) {
			plugin = new ECFPlugin();
		}
		return plugin;
	}

	public ECFPlugin() {
		// null constructor
	}

	public void start(BundleContext ctxt) throws Exception {
		plugin = this;
		this.context = ctxt;

		// initialize the default ssl socket factory 
		try {
			Class ecfSocketFactoryClass = Class.forName("org.eclipse.ecf.internal.ssl.ECFTrustManager"); //$NON-NLS-1$
			ecfTrustManager = (BundleActivator) ecfSocketFactoryClass.newInstance();
			ecfTrustManager.start(ctxt);
		} catch (ClassNotFoundException e) {
			// will occur if fragment is not installed or not on proper execution environment
		} catch (Throwable t) {
			log(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), "Unexpected Error in ECFPlugin.start", t)); //$NON-NLS-1$
		}

		this.extensionRegistryTracker = new ServiceTracker(ctxt, IExtensionRegistry.class.getName(), null);
		this.extensionRegistryTracker.open();
		final IExtensionRegistry registry = getExtensionRegistry();
		if (registry != null) {
			this.registryManager = new ECFRegistryManager();
			registry.addRegistryChangeListener(registryManager);
		}

		// defer extension execution until first consumer calls
		final ServiceFactory sf = new ServiceFactory() {
			public Object getService(Bundle bundle, ServiceRegistration registration) {
				return ContainerFactory.getDefault();
			}

			public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
				// NOP
			}
		};
		containerFactoryServiceRegistration = ctxt.registerService(IContainerFactory.class.getName(), sf, null);
		containerManagerServiceRegistration = ctxt.registerService(IContainerManager.class.getName(), sf, null);

		// but eagerly start ECF startup extension
		setupStartExtensionPoint(context);
	}

	public void initializeExtensions() {
		setupContainerFactoryExtensionPoint(context);
		setupContainerExtensionPoint(context);
	}

	public void stop(BundleContext ctxt) throws Exception {
		fireDisposables();
		this.disposables = null;
		final IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null)
			reg.removeRegistryChangeListener(registryManager);
		this.registryManager = null;
		if (ecfTrustManager != null) {
			ecfTrustManager.stop(ctxt);
			ecfTrustManager = null;
		}
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		if (extensionRegistryTracker != null) {
			extensionRegistryTracker.close();
			extensionRegistryTracker = null;
		}
		if (containerFactoryServiceRegistration != null) {
			containerFactoryServiceRegistration.unregister();
			containerFactoryServiceRegistration = null;
		}
		if (containerManagerServiceRegistration != null) {
			containerManagerServiceRegistration.unregister();
			containerManagerServiceRegistration = null;
		}
		if (adapterManagerTracker != null) {
			adapterManagerTracker.close();
			adapterManagerTracker = null;
		}
		this.context = null;
	}

	public void addDisposable(IDisposable disposable) {
		disposables.put(disposable, null);
	}

	public void removeDisposable(IDisposable disposable) {
		disposables.remove(disposable);
	}

	protected void fireDisposables() {
		for (final Iterator i = disposables.keySet().iterator(); i.hasNext();) {
			final IDisposable d = (IDisposable) i.next();
			if (d != null)
				d.dispose();
		}
	}

	public Bundle getBundle() {
		if (context == null)
			return null;
		return context.getBundle();
	}

	private LogService systemLogService;

	protected LogService getLogService() {
		if (context == null) {
			if (systemLogService == null)
				systemLogService = new SystemLogService(PLUGIN_ID);
			return systemLogService;
		}
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context, LogService.class.getName(), null);
			logServiceTracker.open();
		}
		logService = (LogService) logServiceTracker.getService();
		if (logService == null)
			logService = new SystemLogService(PLUGIN_ID);
		return logService;
	}

	public void log(IStatus status) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(LogHelper.getLogCode(status), LogHelper.getLogMessage(status), status.getException());
	}

	protected void logException(IStatus status, String method, Throwable exception) {
		log(status);
		Trace.catching(ECFPlugin.PLUGIN_ID, ECFDebugOptions.EXCEPTIONS_CATCHING, ECFPlugin.class, method, exception);
	}

	/**
	 * Remove extensions for container factory extension point
	 * 
	 * @param members
	 *            the members to remove
	 */
	protected void removeContainerFactoryExtensions(IConfigurationElement[] members) {
		final String method = "removeContainerFactoryExtensions"; //$NON-NLS-1$
		Trace.entering(ECFPlugin.PLUGIN_ID, ECFDebugOptions.METHODS_ENTERING, ECFPlugin.class, method, members);
		// For each configuration element
		for (int m = 0; m < members.length; m++) {
			final IConfigurationElement member = members[m];
			// Get the label of the extender plugin and the ID of the extension.
			final IExtension extension = member.getDeclaringExtension();
			String name = null;
			try {
				// Get name and get version, if available
				name = member.getAttribute(NAME_ATTRIBUTE);
				if (name == null) {
					name = member.getAttribute(CLASS_ATTRIBUTE);
				}
				final IContainerFactory factory = ContainerFactory.getDefault();
				final ContainerTypeDescription cd = factory.getDescriptionByName(name);
				if (cd == null || !factory.containsDescription(cd)) {
					continue;
				}
				// remove
				factory.removeDescription(cd);
				Trace.trace(ECFPlugin.PLUGIN_ID, ECFDebugOptions.DEBUG, method + ".removed " + cd + " from factory"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (final Exception e) {
				logException(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, "ECF container factory with name=" + name + " already found. Ignoring registration for containerFactory extension point=" + extension.getExtensionPointUniqueIdentifier(), null), method, e); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
	}

	void removeContainerExtensions(IConfigurationElement[] members) {
		final String method = "removeContainerExtensions"; //$NON-NLS-1$
		Trace.entering(ECFPlugin.PLUGIN_ID, ECFDebugOptions.METHODS_ENTERING, ECFPlugin.class, method, members);
		// For each configuration element
		for (int m = 0; m < members.length; m++) {
			final IConfigurationElement member = members[m];
			// The only required attribute is "factoryName"
			String factoryName = member.getAttribute(FACTORY_ATTRIBUTE);
			// Skip over if factory name is invalid
			if (factoryName == null || "".equals(factoryName))continue; //$NON-NLS-1$
			IContainerManager manager = (IContainerManager) ContainerFactory.getDefault();
			IContainer[] containers = manager.getAllContainers();
			if (containers == null)
				continue;
			for (int i = 0; i < containers.length; i++) {
				ContainerTypeDescription containerTypeDescription = manager.getContainerTypeDescription(containers[i].getID());
				if (containerTypeDescription != null && containerTypeDescription.getName().equals(factoryName)) {
					// Remove from manager
					IContainer removedContainer = manager.removeContainer(containers[i]);
					if (removedContainer != null) {
						try {
							containers[i].dispose();
						} catch (Exception e) {
							logException(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Unexpected exception disposing container with factoryName=" + factoryName + " and id=" + containers[i].getID(), null), method, e); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}
		}
	}

	/**
	 * Add container factory extension point extensions
	 * 
	 * @param members
	 *            to add
	 */
	protected void addContainerFactoryExtensions(IConfigurationElement[] members) {
		final String method = "addContainerFactoryExtensions"; //$NON-NLS-1$
		Trace.entering(ECFPlugin.PLUGIN_ID, ECFDebugOptions.METHODS_ENTERING, ECFPlugin.class, method, members);
		final IContainerFactory factory = ContainerFactory.getDefault();
		// For each configuration element
		for (int m = 0; m < members.length; m++) {
			final IConfigurationElement member = members[m];
			// Get the label of the extender plugin and the ID of the extension.
			final IExtension extension = member.getDeclaringExtension();
			Object exten = null;
			String name = null;
			try {
				// Get value of containerFactory name attribute 
				name = member.getAttribute(NAME_ATTRIBUTE);
				if (name != null) {
					ContainerTypeDescription ctd = factory.getDescriptionByName(name);
					// If we've got one already by this name, then we skip this new one
					if (ctd != null) {
						// log with warning
						log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Factory already has container type description with name=" + name + ".  Ignoring extension from " + member.getContributor().getName())); //$NON-NLS-1$ //$NON-NLS-2$
						// and continue
						continue;
					}
				}
				// The only required attribute is "class"
				exten = member.createExecutableExtension(CLASS_ATTRIBUTE);
				final String clazz = exten.getClass().getName();

				if (name == null) {
					name = clazz;
				}

				// Get description, if present
				String description = member.getAttribute(DESCRIPTION_ATTRIBUTE);
				if (description == null) {
					description = ""; //$NON-NLS-1$
				}

				String s = member.getAttribute(SERVER_ATTRIBUTE);
				final boolean server = (s == null) ? false : Boolean.valueOf(s).booleanValue();
				s = member.getAttribute(HIDDEN_ATTRIBUTE);
				final boolean hidden = (s == null) ? false : Boolean.valueOf(s).booleanValue();

				// Now make description instance
				final ContainerTypeDescription scd = new ContainerTypeDescription(name, (IContainerInstantiator) exten, description, server, hidden);

				if (factory.containsDescription(scd)) {
					log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Factory already has container type description=" + scd + ".  Ignoring extension from " + member.getContributor().getName())); //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}
				// Now add the description and we're ready to go.
				factory.addDescription(scd);
				Trace.trace(ECFPlugin.PLUGIN_ID, ECFDebugOptions.DEBUG, method + ".added " + scd + " to factory " + factory); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (final CoreException e) {
				logException(e.getStatus(), method, e);
			} catch (final Exception e) {
				logException(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, "ECF container factory with name=" + name + " already found. Ignoring registration for containerFactory extension point=" + extension.getExtensionPointUniqueIdentifier(), null), method, e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	void addContainerExtensions(IConfigurationElement[] members) {
		final String method = "addContainerExtensions"; //$NON-NLS-1$
		Trace.entering(ECFPlugin.PLUGIN_ID, ECFDebugOptions.METHODS_ENTERING, ECFPlugin.class, method, members);
		// For each configuration element
		for (int m = 0; m < members.length; m++) {
			final IConfigurationElement member = members[m];
			String factory = null;
			String id = null;
			try {
				// The only required attribute is "factoryName"
				factory = member.getAttribute(FACTORY_ATTRIBUTE);
				// Skip over if factory name is invalid
				if (factory == null || "".equals(factory))continue; //$NON-NLS-1$
				// get id attribute
				id = member.getAttribute(ID_ATTRIBUTE);
				id = (id == null || "".equals(id)) ? null : id; //$NON-NLS-1$
				Map parameters = getParametersForContainer(member);
				ContainerFactory.getDefault().createContainer(factory, id, parameters);
				Trace.trace(ECFPlugin.PLUGIN_ID, ECFDebugOptions.DEBUG, method + ".added container with factoryName=" + factory + " and id=" + id); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (final CoreException e) {
				logException(e.getStatus(), method, e);
			} catch (final Exception e) {
				logException(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Unexpected exception creating container with factoryName=" + factory + " and id=" + id, null), method, e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	Map getParametersForContainer(IConfigurationElement member) {
		IConfigurationElement[] elements = member.getChildren(PARAMETER_ELEMENT);
		if (elements == null)
			return null;
		Map results = null;
		for (int i = 0; i < elements.length; i++) {
			String name = elements[i].getAttribute(PARAMETER_NAME);
			String value = elements[i].getAttribute(PARAMETER_VALUE);
			if (name != null && !"".equals(name) && value != null && !"".equals(value)) { //$NON-NLS-1$ //$NON-NLS-2$
				if (results == null)
					results = new Properties();
				results.put(name, value);
			}
		}
		return results;
	}

	/**
	 * Setup container factory extension point
	 * 
	 * @param bc
	 *            the BundleContext for this bundle
	 */
	protected void setupContainerFactoryExtensionPoint(BundleContext bc) {
		final IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			final IExtensionPoint extensionPoint = reg.getExtensionPoint(CONTAINER_FACTORY_EPOINT);
			if (extensionPoint == null) {
				return;
			}
			addContainerFactoryExtensions(extensionPoint.getConfigurationElements());
		}
	}

	protected void setupContainerExtensionPoint(BundleContext bc) {
		final IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			final IExtensionPoint extensionPoint = reg.getExtensionPoint(CONTAINER_EPOINT);
			if (extensionPoint == null) {
				return;
			}
			addContainerExtensions(extensionPoint.getConfigurationElements());
		}
	}

	public IExtensionRegistry getExtensionRegistry() {
		if (context == null)
			return null;
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}

	/**
	 * Setup start extension point
	 * 
	 * @param bc
	 *            the BundleContext for this bundle
	 */
	protected void setupStartExtensionPoint(BundleContext bc) {
		final IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			final IExtensionPoint extensionPoint = reg.getExtensionPoint(START_EPOINT);
			if (extensionPoint == null) {
				return;
			}
			runStartExtensions(extensionPoint.getConfigurationElements());
		}
	}

	protected void runStartExtensions(IConfigurationElement[] configurationElements) {
		final String method = "runStartExtensions"; //$NON-NLS-1$
		// For each configuration element
		for (int m = 0; m < configurationElements.length; m++) {
			final IConfigurationElement member = configurationElements[m];
			try {
				// The only required attribute is "class"
				boolean sync = (member.getAttribute(ASYNCH_ATTRIBUTE) == null);
				IECFStart clazz = (IECFStart) member.createExecutableExtension(CLASS_ATTRIBUTE);
				startExtension(clazz.getClass().getName(), clazz, sync);
			} catch (final CoreException e) {
				logException(e.getStatus(), method, e);
			} catch (final Exception e) {
				logException(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, "Unknown start exception", e), method, e); //$NON-NLS-1$
			}
		}
	}

	private void startExtension(String name, IECFStart exten, boolean synchronous) {
		// Create job to do start, and schedule
		if (synchronous) {
			IStatus result = null;
			try {
				result = exten.run(new NullProgressMonitor());
			} catch (final Throwable e) {
				final String message = "startup extension error"; //$NON-NLS-1$
				logException(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e), message, e);
			}
			if (result != null && !result.isOK())
				logException(result, result.getMessage(), result.getException());
		} else {
			final ECFStartJob job = new ECFStartJob(name, exten);
			job.schedule();
		}
	}

	protected class ECFRegistryManager implements IRegistryChangeListener {
		public void registryChanged(IRegistryChangeEvent event) {
			final IExtensionDelta factoryDeltas[] = event.getExtensionDeltas(ECFNAMESPACE, CONTAINER_FACTORY_NAME);
			for (int i = 0; i < factoryDeltas.length; i++) {
				switch (factoryDeltas[i].getKind()) {
					case IExtensionDelta.ADDED :
						addContainerFactoryExtensions(factoryDeltas[i].getExtension().getConfigurationElements());
						break;
					case IExtensionDelta.REMOVED :
						removeContainerFactoryExtensions(factoryDeltas[i].getExtension().getConfigurationElements());
						break;
				}
			}
			final IExtensionDelta containerDeltas[] = event.getExtensionDeltas(ECFNAMESPACE, CONTAINER_NAME);
			for (int i = 0; i < containerDeltas.length; i++) {
				switch (containerDeltas[i].getKind()) {
					case IExtensionDelta.ADDED :
						addContainerExtensions(containerDeltas[i].getExtension().getConfigurationElements());
						break;
					case IExtensionDelta.REMOVED :
						removeContainerExtensions(containerDeltas[i].getExtension().getConfigurationElements());
						break;
				}
			}
		}
	}

	public IAdapterManager getAdapterManager() {
		if (context == null)
			return null;
		// First, try to get the adapter manager via
		if (adapterManagerTracker == null) {
			adapterManagerTracker = new ServiceTracker(this.context, IAdapterManager.class.getName(), null);
			adapterManagerTracker.open();
		}
		IAdapterManager adapterManager = (IAdapterManager) adapterManagerTracker.getService();
		// Then, if the service isn't there, try to get from Platform class via
		// PlatformHelper class
		if (adapterManager == null)
			adapterManager = PlatformHelper.getPlatformAdapterManager();
		if (adapterManager == null)
			getDefault().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Cannot get adapter manager", null)); //$NON-NLS-1$
		return adapterManager;
	}

}