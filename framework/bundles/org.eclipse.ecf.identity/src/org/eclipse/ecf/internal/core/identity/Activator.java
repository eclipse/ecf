/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.core.identity;

import org.eclipse.core.runtime.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.util.*;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.identity"; //$NON-NLS-1$

	protected static final String NAMESPACE_NAME = "namespace"; //$NON-NLS-1$

	protected static final String NAMESPACE_EPOINT = PLUGIN_ID + "." //$NON-NLS-1$
			+ NAMESPACE_NAME;

	protected static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$

	protected static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	protected static final int REMOVE_NAMESPACE_ERRORCODE = 100;

	protected static final int FACTORY_NAME_COLLISION_ERRORCODE = 200;

	protected static final String DESCRIPTION_ATTRIBUTE = "description"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private BundleContext context = null;

	private IRegistryChangeListener registryManager = null;

	private ServiceRegistration idFactoryServiceRegistration = null;

	private ServiceTracker extensionRegistryTracker = null;

	private ServiceTracker debugOptionsTracker = null;

	private ServiceTracker logServiceTracker = null;

	private LogService logService = null;

	private ServiceTracker adapterManagerTracker = null;

	public synchronized IAdapterManager getAdapterManager() {
		if (this.context == null)
			return null;
		// First, try to get the adapter manager via
		if (adapterManagerTracker == null) {
			adapterManagerTracker = new ServiceTracker(this.context,
					IAdapterManager.class.getName(), null);
			adapterManagerTracker.open();
		}
		IAdapterManager adapterManager = (IAdapterManager) adapterManagerTracker
				.getService();
		// Then, if the service isn't there, try to get from Platform class via
		// PlatformHelper class
		if (adapterManager == null)
			adapterManager = PlatformHelper.getPlatformAdapterManager();
		if (adapterManager == null)
			getDefault().log(
					new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR,
							"Cannot get adapter manager", null)); //$NON-NLS-1$
		return adapterManager;
	}

	/**
	 * The constructor
	 */
	public Activator() {
		// public null constructor
	}

	public synchronized IExtensionRegistry getExtensionRegistry() {
		if (this.context == null)
			return null;
		if (extensionRegistryTracker == null) {
			extensionRegistryTracker = new ServiceTracker(context,
					IExtensionRegistry.class.getName(), null);
			extensionRegistryTracker.open();
		}
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}

	public synchronized DebugOptions getDebugOptions() {
		if (context == null)
			return null;
		if (debugOptionsTracker == null) {
			debugOptionsTracker = new ServiceTracker(context,
					DebugOptions.class.getName(), null);
			debugOptionsTracker.open();
		}
		return (DebugOptions) debugOptionsTracker.getService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext ctxt) throws Exception {
		plugin = this;
		this.context = ctxt;
		// Register IIDFactory service
		idFactoryServiceRegistration = context.registerService(
				IIDFactory.class.getName(), IDFactory.getDefault(), null);

		final IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			this.registryManager = new IdentityRegistryManager();
			reg.addRegistryChangeListener(registryManager);
		}
	}

	public BundleContext getBundleContext() {
		return context;
	}

	protected class IdentityRegistryManager implements IRegistryChangeListener {
		public void registryChanged(IRegistryChangeEvent event) {
			final IExtensionDelta delta[] = event.getExtensionDeltas(PLUGIN_ID,
					NAMESPACE_NAME);
			for (int i = 0; i < delta.length; i++) {
				switch (delta[i].getKind()) {
				case IExtensionDelta.ADDED:
					addNamespaceExtensions(delta[i].getExtension()
							.getConfigurationElements());
					break;
				case IExtensionDelta.REMOVED:
					removeNamespaceExtensions(delta[i].getExtension()
							.getConfigurationElements());
					break;
				}
			}
		}
	}

	/**
	 * Remove extensions for identity namespace extension point
	 * 
	 * @param members
	 *            the members to remove
	 */
	protected void removeNamespaceExtensions(IConfigurationElement[] members) {
		for (int m = 0; m < members.length; m++) {
			final IConfigurationElement member = members[m];
			String name = null;
			try {
				name = member.getAttribute(NAME_ATTRIBUTE);
				if (name == null) {
					name = member.getAttribute(CLASS_ATTRIBUTE);
				}
				if (name == null)
					continue;
				final IIDFactory factory = IDFactory.getDefault();
				final Namespace n = factory.getNamespaceByName(name);
				if (n == null || !factory.containsNamespace(n)) {
					continue;
				}
				// remove
				factory.removeNamespace(n);
			} catch (final Exception e) {
				getDefault().log(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								REMOVE_NAMESPACE_ERRORCODE,
								"Exception removing namespace", e)); //$NON-NLS-1$
			}
		}
	}

	public Bundle getBundle() {
		if (context == null)
			return null;
		return context.getBundle();
	}

	protected synchronized LogService getLogService() {
		if (context == null) {
			if (logService == null)
				logService = new SystemLogService(PLUGIN_ID);
			return logService;
		}
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context,
					LogService.class.getName(), null);
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
			logService.log(LogHelper.getLogCode(status),
					LogHelper.getLogMessage(status), status.getException());
	}

	/**
	 * Add identity namespace extension point extensions
	 * 
	 * @param members
	 *            the members to add
	 */
	protected void addNamespaceExtensions(IConfigurationElement[] members) {
		final String bundleName = getDefault().getBundle().getSymbolicName();
		for (int m = 0; m < members.length; m++) {
			final IConfigurationElement member = members[m];
			// Get the label of the extender plugin and the ID of the
			// extension.
			final IExtension extension = member.getDeclaringExtension();
			String nsName = null;
			try {
				final Namespace ns = (Namespace) member
						.createExecutableExtension(CLASS_ATTRIBUTE);
				final String clazz = ns.getClass().getName();
				nsName = member.getAttribute(NAME_ATTRIBUTE);
				if (nsName == null) {
					nsName = clazz;
				}
				final String nsDescription = member
						.getAttribute(DESCRIPTION_ATTRIBUTE);
				ns.initialize(nsName, nsDescription);
				// Check to see if we have a namespace name collision
				if (!IDFactory.containsNamespace0(ns)) {
					// Now add to known namespaces
					IDFactory.addNamespace0(ns);
				}
			} catch (final CoreException e) {
				getDefault().log(e.getStatus());
			} catch (final Exception e) {
				getDefault()
						.log(new Status(
								IStatus.ERROR,
								bundleName,
								FACTORY_NAME_COLLISION_ERRORCODE,
								"name=" //$NON-NLS-1$
										+ nsName
										+ ";extension point id=" //$NON-NLS-1$
										+ extension
												.getExtensionPointUniqueIdentifier(),
								null));
			}
		}
	}

	/**
	 * Setup identity namespace extension point
	 * 
	 */
	public void setupNamespaceExtensionPoint() {
		// Process extension points
		final IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			final IExtensionPoint extensionPoint = reg
					.getExtensionPoint(NAMESPACE_EPOINT);
			if (extensionPoint == null) {
				return;
			}
			addNamespaceExtensions(extensionPoint.getConfigurationElements());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext ctxt) throws Exception {
		final IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null)
			reg.removeRegistryChangeListener(registryManager);
		registryManager = null;
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		if (debugOptionsTracker != null) {
			debugOptionsTracker.close();
			debugOptionsTracker = null;
		}
		if (extensionRegistryTracker != null) {
			extensionRegistryTracker.close();
			extensionRegistryTracker = null;
		}
		if (idFactoryServiceRegistration != null) {
			idFactoryServiceRegistration.unregister();
			idFactoryServiceRegistration = null;
		}
		if (adapterManagerTracker != null) {
			adapterManagerTracker.close();
			adapterManagerTracker = null;
		}
		context = null;
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public synchronized static Activator getDefault() {
		if (plugin == null) {
			plugin = new Activator();
		}
		return plugin;
	}

}
