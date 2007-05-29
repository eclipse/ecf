/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.presence.ui;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ecf.presence.service.IPresenceService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.presence.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ServiceTracker tracker;

	private ServiceTracker extensionRegistryTracker = null;
	
	private BundleContext context;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public IPresenceService[] getPresenceServices() {
		ServiceReference[] references = tracker.getServiceReferences();
		if (references == null) {
			return new IPresenceService[0];
		}
		int length = references.length;
		IPresenceService[] services = new IPresenceService[length];
		for (int i = 0; i < length; i++) {
			services[i] = (IPresenceService) tracker.getService(references[i]);
		}
		return services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
		tracker = new ServiceTracker(context, IPresenceService.class.getName(),
				null);
		tracker.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (extensionRegistryTracker != null) {
			extensionRegistryTracker.close();
			extensionRegistryTracker = null;
		}
		this.context = null;
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

	public IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistryTracker == null) {
			this.extensionRegistryTracker = new ServiceTracker(context,
					IExtensionRegistry.class.getName(), null);
			this.extensionRegistryTracker.open();
		}
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}
	

}
