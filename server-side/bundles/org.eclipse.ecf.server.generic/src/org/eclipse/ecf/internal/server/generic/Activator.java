package org.eclipse.ecf.internal.server.generic;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.server.generic.ServerStarter;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.server.generic"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ServerStarter servers;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		servers = new ServerStarter(context.getBundle().getEntry(Messages.Activator_SERVER_XML).openStream());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (servers != null) {
			servers.destroyServers();
			servers = null;
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
