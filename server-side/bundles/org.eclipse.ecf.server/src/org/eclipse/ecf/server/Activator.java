package org.eclipse.ecf.server;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends Plugin {

	//The shared instance.
	private static Activator plugin;
	
	ECFTCPServerStartup servers;
	
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		startServers();
	}

	private void startServers() {
		if (servers == null) {
			try {
				servers = new ECFTCPServerStartup("server.xml");
			} catch (Exception e) {
				Activator.log("Exception starting ecf tcp servers",e);
			}
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		stopServers();
		plugin = null;
	}

	private void stopServers() {
		if (servers != null) {
			servers.destroyServers();
			servers = null;
		}
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(String message) {
		getDefault().getLog().log(
				new Status(IStatus.OK, Activator.getDefault().getBundle().getSymbolicName(), IStatus.OK, message, null));
	}
	public static void log(String message, Throwable e) {
		getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), IStatus.OK,
						message, e));
	}

}
