/*******************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.internal.provider.r_osgi;

import ch.ethz.iks.r_osgi.RemoteOSGiService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public final class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.r_osgi"; //$NON-NLS-1$

	// The shared instance
	static Activator plugin;

	// The bundle context
	private BundleContext context;

	// The service tracker for the R-OSGi remote service
	private ServiceTracker r_osgi_tracker;

	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * Called when the OSGi framework starts the bundle.
	 * 
	 * @param bc
	 *            the bundle context.
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext bc) throws Exception {
		this.context = bc;
		r_osgi_tracker = new ServiceTracker(context, RemoteOSGiService.class.getName(), null);
		r_osgi_tracker.open();
	}

	/**
	 * Called when the OSGi framework stops the bundle.
	 * 
	 * @param bc
	 *            the bundle context.
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext bc) throws Exception {
		r_osgi_tracker.close();
		r_osgi_tracker = null;
		this.context = null;
		plugin = null;
	}

	/**
	 * get the bundle context.
	 * 
	 * @return the bundle context.
	 */
	public BundleContext getContext() {
		return context;
	}

	/**
	 * get the R-OSGi service instance.
	 * 
	 * @return the R-OSGi service instance or null, if there is none.
	 */
	public RemoteOSGiService getRemoteOSGiService() {
		if (r_osgi_tracker == null) {
			return null;
		}
		return (RemoteOSGiService) r_osgi_tracker.getService();
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static synchronized Activator getDefault() {
		if (plugin == null) {
			plugin = new Activator();
		}
		return plugin;
	}

}
