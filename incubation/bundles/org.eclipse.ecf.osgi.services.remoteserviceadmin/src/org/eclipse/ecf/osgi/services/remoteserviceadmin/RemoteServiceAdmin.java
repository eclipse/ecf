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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.osgi.framework.ServiceReference;

public class RemoteServiceAdmin extends AbstractRemoteServiceAdmin implements org.osgi.service.remoteserviceadmin.RemoteServiceAdmin {

	public Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> exportService(
			ServiceReference reference, Map<String, Object> properties) {
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> result = new ArrayList<org.osgi.service.remoteserviceadmin.ExportRegistration>();
		IRemoteServiceContainer[] rsContainers = (IRemoteServiceContainer[]) properties.get(RemoteConstants.RSA_CONTAINERS);
		
		if (rsContainers == null || rsContainers.length == 0) {
			logError("exportService",RemoteConstants.RSA_CONTAINERS+" must be non-null and have at least one IRemoteServiceContainer in array");
			return result;
		}
		
		for(int i=0; i < rsContainers.length; i++) {
			
		}
		// TODO Auto-generated method stub
		return result;
	}

	public org.osgi.service.remoteserviceadmin.ImportRegistration importService(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ExportReference> getExportedServices() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ImportReference> getImportedEndpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
	}
}
