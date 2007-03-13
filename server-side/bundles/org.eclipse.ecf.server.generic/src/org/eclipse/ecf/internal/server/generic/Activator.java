package org.eclipse.ecf.internal.server.generic;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.server.generic.ServerManager;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.server.generic"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ServerManager serverManager;
	
	private ServiceTracker extensionRegistryTracker = null;

	private ServiceTracker discoveryTracker = null;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public IExtensionRegistry getExtensionRegistry() {
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}

	public IDiscoveryService getDiscovery() {
		return (IDiscoveryService) discoveryTracker.getService();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.extensionRegistryTracker = new ServiceTracker(context,
				IExtensionRegistry.class.getName(), null);
		this.extensionRegistryTracker.open();
		this.discoveryTracker = new ServiceTracker(context, IDiscoveryService.class.getName(), null);
		this.discoveryTracker.open();
		serverManager = new ServerManager();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (serverManager != null) {
			serverManager.closeServers();
			serverManager = null;
		}
		if (extensionRegistryTracker != null) {
			extensionRegistryTracker.close();
			extensionRegistryTracker = null;
		}
		if (discoveryTracker != null) {
			discoveryTracker.close();
			discoveryTracker = null;
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(String message) {
		getDefault().getLog().log(
				new Status(IStatus.INFO, getDefault().getBundle().getSymbolicName(), IStatus.INFO, message, null));
	}
	public static void log(String message, Throwable e) {
		getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), IStatus.ERROR,
						message, e));
	}


}
