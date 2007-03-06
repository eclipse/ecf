package org.eclipse.ecf.tests.filetransfer;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.tests.filetransfer";

	// The shared instance
	private static Activator plugin;
	
	private ServiceTracker tracker = null;
	
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
		tracker = new ServiceTracker(context,IRetrieveFileTransferFactory.class.getName(),null);
		tracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (tracker != null) {
			tracker.close();
			tracker = null;
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

	/**
	 * @return IRetrieveFileTransferFactory retrieve file transfer factory
	 */
	public IRetrieveFileTransferFactory getRetrieveFileTransferFactory() {
		return (IRetrieveFileTransferFactory) tracker.getService();
	}

}
