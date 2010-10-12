/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.Map;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;

public class ECFEndpointDescription extends EndpointDescription {

	public ECFEndpointDescription(final ServiceReference reference,
			final Map osgiProperties) {
		super(reference,osgiProperties);
	}

	public ECFEndpointDescription(IServiceID serviceId,
			IServiceInfo discoveredServiceInfo, Map osgiProperties) {
		super(osgiProperties);
		// XXX todo
	}
	
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		return super.equals(other);
	}
	
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
}
