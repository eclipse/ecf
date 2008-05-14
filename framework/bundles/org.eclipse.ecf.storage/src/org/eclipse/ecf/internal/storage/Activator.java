package org.eclipse.ecf.internal.storage;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.eclipse.ecf.storage.IIDStore;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.storage"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ServiceTracker logServiceTracker = null;

	private LogService logService = null;

	private BundleContext context = null;

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
		plugin = this;
		this.context = context;
		context.registerService(IIDStore.class.getName(), new IDStore(), null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		context = null;
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	protected LogService getLogService() {
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

}
