package org.eclipse.ecf.internal.tests;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.tests";

	// The shared instance
	private static Activator plugin;
	
	private static ServiceTracker serviceTracker;
	private static IIDFactory idFactory;
	
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
		serviceTracker = new ServiceTracker(context,IIDFactory.class.getName(), null);
		serviceTracker.open();
		idFactory = (IIDFactory) serviceTracker.getService();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		idFactory = null;
		if (serviceTracker != null) {
			serviceTracker.close();
			serviceTracker = null;
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

	public IIDFactory getIDFactory() {
		return idFactory;
	}
}
