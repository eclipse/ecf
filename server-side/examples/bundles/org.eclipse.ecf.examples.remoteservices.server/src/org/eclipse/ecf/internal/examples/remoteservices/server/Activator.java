/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.remoteservices.server;

import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The shared instance
	private static Activator plugin;

	private BundleContext context;

	private ServiceTracker environmentInfoTracker;

	private ServiceTracker discoveryTracker;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	public EnvironmentInfo getEnvironmentInfo() {
		if (environmentInfoTracker == null) {
			environmentInfoTracker = new ServiceTracker(context, org.eclipse.osgi.service.environment.EnvironmentInfo.class.getName(), null);
			environmentInfoTracker.open();
		}
		return (EnvironmentInfo) environmentInfoTracker.getService();
	}

	public IDiscoveryService getDiscoveryService(int waittime) throws InterruptedException {
		if (discoveryTracker == null) {
			discoveryTracker = new ServiceTracker(context, org.eclipse.ecf.discovery.service.IDiscoveryService.class.getName(), null);
			discoveryTracker.open();
		}
		return (IDiscoveryService) discoveryTracker.waitForService(waittime);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (environmentInfoTracker != null) {
			environmentInfoTracker.close();
			environmentInfoTracker = null;
		}
		if (discoveryTracker != null) {
			discoveryTracker.close();
			discoveryTracker = null;
		}
		this.context = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
