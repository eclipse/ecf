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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The main plugin class to be used in the desktop.
 */
public class JMDNSPlugin implements BundleActivator {
	// The shared instance.
	private static JMDNSPlugin plugin;

	private BundleContext context = null;
	
	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.jmdns";
	
	public static final String NAMESPACE_IDENTIFIER = Messages
			.getString("JMDNSPlugin.namespace.identifier"); //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public JMDNSPlugin() {
		super();
		plugin = this;
	}

	private ServiceTracker adapterManagerTracker = null;

	public IAdapterManager getAdapterManager() {
		// First, try to get the adapter manager via
		if (adapterManagerTracker == null) {
			adapterManagerTracker = new ServiceTracker(this.context,
					IAdapterManager.class.getName(), null);
			adapterManagerTracker.open();
		}
		IAdapterManager adapterManager = (IAdapterManager) adapterManagerTracker
				.getService();
		// Then, if the service isn't there, try to get from Platform class via
		// PlatformHelper class
		if (adapterManager == null)
			adapterManager = PlatformHelper.getPlatformAdapterManager();
		return adapterManager;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
	}

	protected Bundle getBundle() {
		if (context == null) return null;
		else return context.getBundle();
	}
	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		if (adapterManagerTracker != null) {
			adapterManagerTracker.close();
			adapterManagerTracker = null;
		}
		this.context = context;
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public synchronized static JMDNSPlugin getDefault() {
		if (plugin == null) {
			plugin = new JMDNSPlugin();
		}
		return plugin;
	}

	public String getNamespaceIdentifier() {
		return NAMESPACE_IDENTIFIER;
	}

}
