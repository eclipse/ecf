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
import java.util.Dictionary;
import java.util.Map;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;

public class ContainerRemoteServiceAdmin implements RemoteServiceAdmin {

	private BundleContext bundleContext;
	private IRemoteServiceContainer rsContainer;
	
	public ContainerRemoteServiceAdmin(BundleContext bundleContext, IRemoteServiceContainer rsContainer) {
		this.bundleContext = bundleContext;
		this.rsContainer = rsContainer;
	}
	
	public Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> exportService(
			ServiceReference reference, Map<String, Object> properties) {
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> result = new ArrayList();
		try {
			String [] rsClazzes = getRemoteServiceClasses(reference,properties);
			Object remoteService = getRemoteService(reference,properties);
			Dictionary rsProperties = getRemoteServiceProperties(reference,properties);
			EndpointDescription endpointDescription = getEndpointDescription(reference,properties);
			IRemoteServiceRegistration rsRegistration = rsContainer.getContainerAdapter().registerRemoteService(rsClazzes, remoteService, rsProperties);
			result.add(new ExportRegistration(rsRegistration, reference, endpointDescription));
		} catch (Exception e) {
			logException("Exception exporting serviceReference="+reference+", properties="+properties);
			result.add(new ExportRegistration(e));
		}
		return result;
	}

	private void logException(String string) {
		// TODO Auto-generated method stub
		
	}

	private EndpointDescription getEndpointDescription(
			ServiceReference reference, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	private Dictionary getRemoteServiceProperties(ServiceReference reference,
			Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object getRemoteService(ServiceReference reference,
			Map<String, Object> properties) {
		return bundleContext.getService(reference);
	}

	private String[] getRemoteServiceClasses(ServiceReference reference,
			Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	public ImportRegistration importService(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ExportReference> getExportedServices() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ImportReference> getImportedEndpoints() {
		// TODO Auto-generated method stub
		return null;
	}

}
