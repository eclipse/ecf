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

import org.eclipse.ecf.remoteservice.IOSGiRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RemoteServiceAdmin extends AbstractRemoteServiceAdmin implements
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin {

	private Collection<ExportRegistration> exportedRegistrations = new ArrayList<ExportRegistration>();

	public RemoteServiceAdmin(BundleContext context) {
		super(context);
	}

	public Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> exportService(
			ServiceReference reference, Map<String, Object> properties) {
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> results = new ArrayList<org.osgi.service.remoteserviceadmin.ExportRegistration>();
		IRemoteServiceContainer[] rsContainers = (IRemoteServiceContainer[]) properties
				.get(RemoteConstants.RSA_CONTAINERS);

		if (rsContainers == null || rsContainers.length == 0) {
			logError(
					"exportService",
					RemoteConstants.RSA_CONTAINERS
							+ " must be non-null and have at least one IRemoteServiceContainer in array");
			return results;
		}
		synchronized (exportedRegistrations) {
			for (int i = 0; i < rsContainers.length; i++) {
				ExportRegistration rsRegistration = null;
				try {
					rsRegistration = doExportService(reference, properties,
							rsContainers[i]);
					exportedRegistrations.add(rsRegistration);
				} catch (Exception e) {
					rsRegistration = handleExportServiceException(reference,
							properties, rsContainers[i], e);
				}
				results.add(rsRegistration);
			}
		}
		return results;
	}

	private ExportRegistration handleExportServiceException(
			ServiceReference reference, Map<String, Object> properties,
			IRemoteServiceContainer iRemoteServiceContainer, Exception e) {
		// TODO Auto-generated method stub
		return null;
	}

	private ExportRegistration doExportService(
			ServiceReference serviceReference, Map<String, Object> properties,
			IRemoteServiceContainer container) throws Exception {
		String[] exportedInterfaces = (String[]) properties
				.get(RemoteConstants.RSA_EXPORTED_INTERFACES);
		Dictionary remoteServiceProperties = getRemoteServiceProperties(
				serviceReference, properties, container);
		IRemoteServiceContainerAdapter containerAdapter = container
				.getContainerAdapter();
		IRemoteServiceRegistration remoteRegistration;
		if (containerAdapter instanceof IOSGiRemoteServiceContainerAdapter) {
			IOSGiRemoteServiceContainerAdapter osgiContainerAdapter = (IOSGiRemoteServiceContainerAdapter) containerAdapter;
			remoteRegistration = osgiContainerAdapter.registerRemoteService(
					exportedInterfaces, serviceReference,
					remoteServiceProperties);
		} else {
			remoteRegistration = containerAdapter.registerRemoteService(
					exportedInterfaces, getService(serviceReference),
					remoteServiceProperties);
		}
		try {
			// Create EndpointDescription
			EndpointDescription endpointDescription = createEndpointDescription(
					serviceReference, properties, remoteRegistration, container);
			// Create ExportRegistration
			ExportRegistration exportRegistration = createExportRegistration(
					remoteRegistration, serviceReference, endpointDescription);
			return exportRegistration;
		} catch (Exception e) {
			remoteRegistration.unregister();
			throw e;
		}
	}

	private ExportRegistration createExportRegistration(
			IRemoteServiceRegistration remoteRegistration,
			ServiceReference serviceReference,
			EndpointDescription endpointDescription) {
		return new ExportRegistration(remoteRegistration, serviceReference,
				endpointDescription);
	}

	private Dictionary getRemoteServiceProperties(
			ServiceReference serviceReference, Map<String, Object> properties,
			IRemoteServiceContainer container) {
		// TODO Auto-generated method stub
		return null;
	}

	public org.osgi.service.remoteserviceadmin.ImportRegistration importService(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<org.eclipse.ecf.osgi.services.remoteserviceadmin.ExportRegistration> getExportedRegistrations() {
		Collection<org.eclipse.ecf.osgi.services.remoteserviceadmin.ExportRegistration> results = new ArrayList<org.eclipse.ecf.osgi.services.remoteserviceadmin.ExportRegistration>();
		synchronized (exportedRegistrations) {
			results.addAll(exportedRegistrations);
		}
		return results;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ExportReference> getExportedServices() {
		Collection<org.osgi.service.remoteserviceadmin.ExportReference> results = new ArrayList<org.osgi.service.remoteserviceadmin.ExportReference>();
		synchronized (exportedRegistrations) {
			for (ExportRegistration reg : exportedRegistrations) {
				results.add(reg.getExportReference());
			}
		}
		return results;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ImportReference> getImportedEndpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		synchronized (exportedRegistrations) {
			exportedRegistrations.clear();
		}
		super.close();
	}
}
