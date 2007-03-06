package org.eclipse.ecf.internal.tests;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ecf.core.IContainerFactory;
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
	
	private static ServiceTracker idFactoryServiceTracker;
	private static IIDFactory idFactory;
	
	private static ServiceTracker containerFactoryServiceTracker;
	private static IContainerFactory containerFactory;
	
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
		idFactoryServiceTracker = new ServiceTracker(context,IIDFactory.class.getName(), null);
		idFactoryServiceTracker.open();
		idFactory = (IIDFactory) idFactoryServiceTracker.getService();
		containerFactoryServiceTracker = new ServiceTracker(context,IContainerFactory.class.getName(),null);
		containerFactoryServiceTracker.open();
		containerFactory = (IContainerFactory) containerFactoryServiceTracker.getService();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		idFactory = null;
		if (idFactoryServiceTracker != null) {
			idFactoryServiceTracker.close();
			idFactoryServiceTracker = null;
		}
		if (containerFactoryServiceTracker != null) {
			containerFactoryServiceTracker.close();
			containerFactoryServiceTracker = null;
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
	
	public IContainerFactory getContainerFactory() {
		return containerFactory;
	}
}
