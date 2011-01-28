/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.distribution"; //$NON-NLS-1$

	public static final boolean autoCreateProxyContainer = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.distribution.autoCreateProxyContainer", //$NON-NLS-1$
					"true")).booleanValue(); //$NON-NLS-1$

	public static final boolean autoCreateHostContainer = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.distribution.autoCreateHostContainer", //$NON-NLS-1$
					"true")).booleanValue(); //$NON-NLS-1$

	public static final String defaultHostConfigType = System.getProperty(
			"org.eclipse.ecf.osgi.services.distribution.defaultConfigType", //$NON-NLS-1$
			"ecf.generic.server"); //$NON-NLS-1$

	private static Activator plugin;
	private BundleContext context;

	private ServiceTracker logServiceTracker = null;
	private LogService logService = null;

	private BasicTopologyManager basicTopologyManager;

	public static Activator getDefault() {
		return plugin;
	}

	public BundleContext getContext() {
		return context;
	}

	protected synchronized LogService getLogService() {
		if (this.context == null)
			return null;
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
			logService.log(null, LogHelper.getLogCode(status),
					LogHelper.getLogMessage(status), status.getException());
	}

	public void log(ServiceReference sr, IStatus status) {
		log(sr, LogHelper.getLogCode(status), LogHelper.getLogMessage(status),
				status.getException());
	}

	public void log(ServiceReference sr, int level, String message, Throwable t) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(sr, level, message, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(final BundleContext ctxt) throws Exception {
		plugin = this;
		this.context = ctxt;
		basicTopologyManager = new BasicTopologyManager(context);
		// start topology manager first
		basicTopologyManager.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext ctxt) throws Exception {
		if (basicTopologyManager != null) {
			basicTopologyManager.close();
			basicTopologyManager = null;
		}
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		synchronized (this) {
			this.context = null;
		}
		plugin = null;
	}

}
