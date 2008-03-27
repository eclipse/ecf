/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.jmdns;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ecf.core.util.PlatformHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The main plugin class to be used in the desktop.
 */
public class JMDNSPlugin implements BundleActivator {
	// The shared instance.
	private static JMDNSPlugin plugin;

	private BundleContext context = null;

	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.jmdns"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public JMDNSPlugin() {
		super();
		plugin = this;
	}

	private ServiceTracker adapterManagerTracker = null;

	private ServiceTracker discoveryTracker;

	private ServiceRegistration serviceRegistration;

	private ServiceTracker logServiceTracker = null;

	private LogService logService = null;

	public IAdapterManager getAdapterManager() {
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
		return adapterManager;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext ctxt) throws Exception {
		this.context = ctxt;
	}

	protected Bundle getBundle() {
		if (context == null)
			return null;
		return context.getBundle();
	}

	protected BundleContext getContext() {
		return context;
	}

	public IDiscoveryService getDiscoveryService() {
		if (discoveryTracker == null) {
			discoveryTracker = new ServiceTracker(context, IDiscoveryService.class.getName(), null);
			discoveryTracker.open();
		}
		return (IDiscoveryService) discoveryTracker.getService();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext ctxt) throws Exception {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
		if (adapterManagerTracker != null) {
			adapterManagerTracker.close();
			adapterManagerTracker = null;
		}
		if (discoveryTracker != null) {
			discoveryTracker.close();
			discoveryTracker = null;
		}
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		this.context = ctxt;
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public synchronized static JMDNSPlugin getDefault() {
		return plugin;
	}

	/**
	 * @param string
	 * @param t
	 */
	public void logException(String string, Throwable t) {
		getLogService();
		if (logService != null)
			logService.log(LogService.LOG_ERROR, string, t);
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

	/**
	 * @param errorString
	 */
	public void logError(String errorString) {
		getLogService();
		if (logService != null)
			logService.log(LogService.LOG_ERROR, errorString);
	}

}
