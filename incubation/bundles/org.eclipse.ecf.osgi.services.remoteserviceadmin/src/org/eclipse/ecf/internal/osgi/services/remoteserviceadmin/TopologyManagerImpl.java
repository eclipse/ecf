/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.osgi.framework.BundleContext;
import org.osgi.service.remoteserviceadmin.EndpointListener;

public class TopologyManagerImpl implements EndpointListener {

	private RemoteServiceAdminImpl remoteServiceAdminImpl;
	
	public TopologyManagerImpl(BundleContext context) {
		this.remoteServiceAdminImpl = new RemoteServiceAdminImpl(context,this);
	}
	
	public void close() {
		if (remoteServiceAdminImpl != null) {
			remoteServiceAdminImpl.close();
			remoteServiceAdminImpl = null;
		}
	}

	public void endpointAdded(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint, String matchedFilter) {
		if (endpoint instanceof EndpointDescription) {
			handleEndpointAdded((EndpointDescription) endpoint);
		} else logWarning("ECF Topology Manager:  Non-ECF endpointAdded="+endpoint+",matchedFilter="+matchedFilter);
	}

	public void endpointRemoved(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (endpoint instanceof EndpointDescription) {
			handleEndpointRemoved((EndpointDescription) endpoint);
		} else logWarning("ECF Topology Manager:  Non-ECF endpointRemoved="+endpoint+",matchedFilter="+matchedFilter);
	}

	private void handleEndpointAdded(EndpointDescription endpoint) {
		// TODO Auto-generated method stub
		trace("handleEndpointAdded","endpoint="+endpoint);
	}

	private void trace(String method, String message) {
		// TODO Auto-generated method stub
		System.out.println("TopologyManager."+method+": "+message);
	}

	private void logWarning(String string) {
		System.out.println(string);
	}

	private void handleEndpointRemoved(EndpointDescription endpoint) {
		// TODO Auto-generated method stub
		trace("handleEndpointRemoved","endpoint="+endpoint);
	}

}
