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
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.provider.jmdns.container.JMDNSDiscoveryContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

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

	private ServiceRegistration serviceRegistration = null;
	
	/**
	 * The constructor.
	 */
	public JMDNSPlugin() {
		super();
		plugin = this;
	}

	public IAdapterManager getAdapterManager() {
		// XXX todo...replace with new adaptermanager service
		return Platform.getAdapterManager();
		//return null;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		JMDNSDiscoveryContainer container = new JMDNSDiscoveryContainer();
		container.connect(null, null);
		serviceRegistration = context.registerService(IDiscoveryService.class
				.getName(), container, null);
	}

	protected Bundle getBundle() {
		if (context == null) return null;
		else return context.getBundle();
	}
	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
		this.context = context;
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static JMDNSPlugin getDefault() {
		return plugin;
	}

	public String getNamespaceIdentifier() {
		return NAMESPACE_IDENTIFIER;
	}

}
