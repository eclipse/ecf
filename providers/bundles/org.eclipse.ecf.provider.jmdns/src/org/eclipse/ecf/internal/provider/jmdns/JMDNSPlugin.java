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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.provider.jmdns.container.JMDNSDiscoveryContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The main plugin class to be used in the desktop.
 */
public class JMDNSPlugin extends Plugin {
	// The shared instance.
	private static JMDNSPlugin plugin;
	// Resource bundle.
	private ResourceBundle resourceBundle;

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

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		JMDNSDiscoveryContainer container = new JMDNSDiscoveryContainer();
		container.connect(null, null);
		serviceRegistration = context.registerService(IDiscoveryService.class
				.getName(), container, null);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		resourceBundle = null;
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static JMDNSPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = JMDNSPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle
						.getBundle("org.eclipse.ecf.internal.provider.jmdns.JmdnsPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	public String getNamespaceIdentifier() {
		return NAMESPACE_IDENTIFIER;
	}

}
