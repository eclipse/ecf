/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.discovery;

import java.net.URL;

import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.discovery.DiscoveredServiceTracker;
import org.osgi.service.discovery.ServicePublication;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.discovery";

	private ServiceTracker servicePublicationTracker;
	private ServiceTracker discoveryServiceTracker;
	private ServiceTracker discoveredServiceTrackerTracker;

	private BundleContext context;
	private static Activator plugin;
	private ServicePublicationHandler servicePublicationHandler;

	public static final Activator getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext ctxt) throws Exception {
		plugin = this;
		this.context = ctxt;
		IDiscoveryService discovery = getDiscoveryService();
		URL bundleURL = ctxt.getBundle().getEntry("/");
		String bundleURLString = bundleURL.toExternalForm();
		if (!bundleURLString.endsWith("/"))
			bundleURLString = bundleURLString + "/";
		servicePublicationHandler = new ServicePublicationHandler(discovery,
				bundleURLString);
		servicePublicationTracker = new ServiceTracker(context,
				ServicePublication.class.getName(), servicePublicationHandler);
		servicePublicationTracker.open();
	}

	BundleContext getContext() {
		return context;
	}

	public ServicePublicationHandler getServicePublicationHandler() {
		return servicePublicationHandler;
	}

	public IDiscoveryService getDiscoveryService() {
		if (discoveryServiceTracker == null) {
			discoveryServiceTracker = new ServiceTracker(this.context,
					IDiscoveryService.class.getName(), null);
			discoveryServiceTracker.open();
		}
		return (IDiscoveryService) discoveryServiceTracker.getService();
	}

	public ServiceReference[] getDiscoveredServiceTrackerReferences() {
		if (discoveredServiceTrackerTracker == null) {
			discoveredServiceTrackerTracker = new ServiceTracker(this.context,
					DiscoveredServiceTracker.class.getName(), null);
			discoveredServiceTrackerTracker.open();
		}
		return discoveredServiceTrackerTracker.getServiceReferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (discoveredServiceTrackerTracker != null) {
			discoveredServiceTrackerTracker.close();
			discoveredServiceTrackerTracker = null;
		}
		if (discoveryServiceTracker != null) {
			discoveryServiceTracker.close();
			discoveryServiceTracker = null;
		}
		if (servicePublicationTracker != null) {
			servicePublicationTracker.close();
			servicePublicationTracker = null;
		}
		if (servicePublicationHandler != null) {
			servicePublicationHandler.dispose();
			servicePublicationHandler = null;
		}
		this.context = null;
		plugin = null;
	}

}
