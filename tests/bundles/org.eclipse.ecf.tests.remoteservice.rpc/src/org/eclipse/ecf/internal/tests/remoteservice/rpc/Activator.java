/******************************************************************************* 
 * Copyright (c) 2010-2011 Naumen. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pavel Samolisov - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.tests.remoteservice.rpc;

import org.eclipse.equinox.http.jetty.JettyConfigurator;

import java.util.Hashtable;

import java.util.Dictionary;

import org.apache.xmlrpc.webserver.XmlRpcServlet;

import org.eclipse.ecf.tests.remoteservice.rpc.RpcConstants;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private static final String HTTP_PORT_KEY = "http.port"; //$NON-NLS-1$

    private static final String SERVER_NAME = "xmlrpcserver"; //$NON-NLS-1$
	
	private static BundleContext context;
	
	private HttpServiceConnector httpServiceConnector;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		httpServiceConnector = new HttpServiceConnector(context, RpcConstants.TEST_SERVLETS_PATH, new XmlRpcServlet());
		
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(HTTP_PORT_KEY, RpcConstants.HTTP_PORT);
        JettyConfigurator.startServer(SERVER_NAME, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		JettyConfigurator.stopServer(SERVER_NAME);
		
		if (httpServiceConnector != null) {
			httpServiceConnector.close();
			httpServiceConnector = null;
		}
	}
}
