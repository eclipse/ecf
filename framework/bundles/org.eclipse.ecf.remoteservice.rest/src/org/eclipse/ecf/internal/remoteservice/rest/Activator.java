/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.internal.remoteservice.rest;

import org.eclipse.ecf.remoteservice.rest.resource.IRestResource;
import org.eclipse.ecf.remoteservice.rest.resource.XMLResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.remoteservice.rest";

	// The shared instance
	private static Activator plugin;

	private BundleContext context;
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
		registerXMLService( context );
	}

	private void registerXMLService(BundleContext context) {
		IRestResource xmlResource = new XMLResource();
		context.registerService(IRestResource.class.getName(), xmlResource, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static synchronized Activator getDefault() {
		if(plugin == null) {
			plugin = new Activator();			
		}
		return plugin;
	}
	
	public BundleContext getContext() {
		return context;
	}

}
