/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.filetransfer;

import java.util.Hashtable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ecf.filetransfer.urlservice.UrlStreamHandlerService;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.filetransfer";

	private static final String URLCONNECTION_FACTORY_EPOINT = PLUGIN_ID + "."
	+ "urlConnectionFactory";

	private static final String PROTOCOL_ATTRIBUTE = "protocol";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		setupProtocolHandlers(context);
	}

	/**
	 * 
	 */
	private void setupProtocolHandlers(BundleContext context) {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = reg
				.getExtensionPoint(URLCONNECTION_FACTORY_EPOINT);
		if (extensionPoint == null) {
			return;
		}
		IConfigurationElement[] configurationElements = extensionPoint
				.getConfigurationElements();

		for (int i = 0; i < configurationElements.length; i++) {
			UrlStreamHandlerService svc = new UrlStreamHandlerService(
					configurationElements[i]);
			Hashtable properties = new Hashtable();
			properties.put(URLConstants.URL_HANDLER_PROTOCOL,
					new String[] { configurationElements[i]
							.getAttribute(PROTOCOL_ATTRIBUTE) });
			context.registerService(URLStreamHandlerService.class
					.getName(), svc, properties);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
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

}
