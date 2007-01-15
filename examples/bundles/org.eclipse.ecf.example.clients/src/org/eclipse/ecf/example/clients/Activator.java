package org.eclipse.ecf.example.clients;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	private static Activator instance = null;
	
	public static final String PLUGIN_ID = "org.eclipse.ecf.example.clients";
	public Activator() {
		super();
		instance = this;
	}

	public static Activator getDefault() {
		return instance;
	}
	
	public void log(int status, String message, Throwable exception) {
		getLog().log(new Status(status,PLUGIN_ID,message,exception));
	}
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		instance = null;
	}


}
