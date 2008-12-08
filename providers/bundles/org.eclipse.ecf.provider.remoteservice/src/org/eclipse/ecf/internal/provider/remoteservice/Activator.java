/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.remoteservice;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.LogHelper;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	private ServiceTracker logServiceTracker = null;

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.remoteservice"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private BundleContext context;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	public void start(BundleContext ctxt) throws Exception {
		this.context = ctxt;
	}

	public void stop(BundleContext ctxt) throws Exception {
		this.context = null;
		plugin = null;
	}

	public BundleContext getContext() {
		return context;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public synchronized static Activator getDefault() {
		if (plugin == null) {
			plugin = new Activator();
		}
		return plugin;
	}

	/**
	 * @param filter
	 * @return Fileter created via context
	 */
	public Filter createFilter(String filter) throws InvalidSyntaxException {
		return context.createFilter(filter);
	}

	protected LogService getLogService() {
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context, LogService.class.getName(), null);
			logServiceTracker.open();
		}
		return (LogService) logServiceTracker.getService();
	}

	public void log(IStatus status) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(LogHelper.getLogCode(status), LogHelper.getLogMessage(status), status.getException());
		}
	}

}
