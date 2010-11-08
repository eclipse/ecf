/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.remoteserviceadmin;

import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	ServiceRegistration endpointListenerRegistration;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Properties props = new Properties();
		props.put(org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE,"("+RemoteConstants.ENDPOINT_ID+"=*)");
		endpointListenerRegistration = context.registerService(EndpointListener.class.getName(), createEndpointListener(), props);
	}

	private EndpointListener createEndpointListener() {
		return new EndpointListener() {

			public void endpointAdded(EndpointDescription endpoint,
					String matchedFilter) {
				System.out.println("endpointAdded endpoint="+endpoint+",matchedFilter="+matchedFilter);
			}

			public void endpointRemoved(EndpointDescription endpoint,
					String matchedFilter) {
				System.out.println("endpointRemoved endpoint="+endpoint+",matchedFilter="+matchedFilter);
			}
			
		};
	}
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (endpointListenerRegistration != null) {
			endpointListenerRegistration.unregister();
			endpointListenerRegistration = null;
		}
		Activator.context = null;
	}

}
