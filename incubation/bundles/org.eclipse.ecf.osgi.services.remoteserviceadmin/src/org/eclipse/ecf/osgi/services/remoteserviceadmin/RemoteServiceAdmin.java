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

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IOSGiRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class RemoteServiceAdmin extends AbstractRemoteServiceAdmin implements
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin {

	private Collection<ExportRegistration> exportedRegistrations = new ArrayList<ExportRegistration>();

	private Collection<ImportRegistration> importedRegistrations = new ArrayList<ImportRegistration>();

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
			IRemoteServiceContainer container) throws ECFException {
		IRemoteServiceRegistration remoteRegistration = null;
		try {
			String[] exportedInterfaces = (String[]) properties
					.get(RemoteConstants.RSA_EXPORTED_INTERFACES);
			Dictionary remoteServiceProperties = getRemoteServiceProperties(
					serviceReference, properties, container);
			IRemoteServiceContainerAdapter containerAdapter = container
					.getContainerAdapter();
			if (containerAdapter instanceof IOSGiRemoteServiceContainerAdapter) {
				IOSGiRemoteServiceContainerAdapter osgiContainerAdapter = (IOSGiRemoteServiceContainerAdapter) containerAdapter;
				remoteRegistration = osgiContainerAdapter
						.registerRemoteService(exportedInterfaces,
								serviceReference, remoteServiceProperties);
			} else {
				remoteRegistration = containerAdapter.registerRemoteService(
						exportedInterfaces, getService(serviceReference),
						remoteServiceProperties);
			}
			// Create EndpointDescription
			EndpointDescription endpointDescription = createExportEndpointDescription(
					serviceReference, properties, remoteRegistration, container);
			return createExportRegistration(
					remoteRegistration, serviceReference, endpointDescription);
		} catch (Exception e) {
			if (remoteRegistration != null) remoteRegistration.unregister();
			throw new ECFException("Exception exporting serviceReference="
					+ serviceReference, e);
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
		EndpointDescription ed = (EndpointDescription) endpoint;
		IRemoteServiceContainer rsContainer = ed
				.getImportRemoteServiceContainer();
		if (rsContainer == null) {
			logError(
					"importService",
					"endpoint description getImportRemoteServiceContainer return value must be non-null");
			return null;
		}
		ImportRegistration result = null;
		synchronized (importedRegistrations) {
			try {
				result = doImportService(ed, rsContainer);
				if (result != null)
					importedRegistrations.add(result);
			} catch (ECFException e) {
				result = handleImportServiceException(ed, rsContainer, e);
			}
		}
		return result;
	}

	private ImportRegistration handleImportServiceException(
			EndpointDescription endpoint,
			IRemoteServiceContainer iRemoteServiceContainer, Exception e) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getFullRemoteServicesFilter(String remoteServicesFilter,
			long remoteServiceId) {
		if (remoteServiceId < 0)
			return remoteServicesFilter;
		StringBuffer filter = new StringBuffer("(&(") //$NON-NLS-1$
				.append(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID)
				.append("=").append(remoteServiceId).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		if (remoteServicesFilter != null)
			filter.append(remoteServicesFilter);
		filter.append(")"); //$NON-NLS-1$
		return filter.toString();
	}

	private ImportRegistration doImportService(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer) throws ECFException {
		Collection<String> interfaces = endpointDescription.getInterfaces();
		ID endpointID = endpointDescription.getContainerID();
		ID targetID = endpointDescription.getConnectTargetID();
		ID[] idFilter = endpointDescription.getIDFilter();
		if (idFilter == null)
			idFilter = new ID[] { endpointID };

		String rsFilter = getFullRemoteServicesFilter(
				endpointDescription.getRemoteServiceFilter(),
				endpointDescription.getRemoteServiceId());

		Collection<IRemoteServiceReference> rsRefs = new ArrayList<IRemoteServiceReference>();
		IRemoteServiceContainerAdapter containerAdapter = rsContainer
				.getContainerAdapter();

		for (String intf : interfaces) {
			try {
				IRemoteServiceReference[] refs = containerAdapter
						.getRemoteServiceReferences(targetID, idFilter, intf,
								rsFilter);
				if (refs == null || refs.length == 0) {
					logWarning("doImportService",
							"getRemoteServiceReferences targetID=" + targetID
									+ ",idFilter=" + idFilter + ",intf=" + intf
									+ ",rsFilter=" + rsFilter
									+ " on rsContainer="
									+ rsContainer.getContainer().getID()
									+ " return null");
					continue;
				}
				for (int i = 0; i < refs.length; i++)
					rsRefs.add(refs[i]);
			} catch (Exception e) {

			}
		}
		if (rsRefs.size() == 0) {
			// This is an error...as no remote service reference was
			// available/reachable with given endpointDescription
			logError("doImportService",
					"remote service reference not found for targetID="
							+ targetID + ",idFilter=" + idFilter
							+ ",interfaces=" + interfaces + ",rsFilter="
							+ rsFilter + " on rsContainer="
							+ rsContainer.getContainer().getID());
			return null;
		}
		// The rsRefs collection should have a single reference in it. If it has
		// more than one, then something is wrong.
		if (rsRefs.size() > 1) {
			logWarning("doImportService",
					"getRemoteServiceReferences for interfaces=" + interfaces
							+ " returned multiple rsRefs=" + rsRefs);
		}
		// Now get first/only one
		IRemoteServiceReference rsReference = rsRefs.iterator().next();
		IRemoteService rs = rsContainer.getContainerAdapter().getRemoteService(
				rsReference);
		if (rs == null)
			throw new ECFException("getRemoteService for rsReference="
					+ rsReference + " returned null for rsContainer="
					+ rsContainer);

		Object proxy = rs.getProxy();
		if (proxy == null)
			throw new ECFException("getProxy() returned null for rsReference="
					+ rsReference + " and rsContainer=" + rsContainer);

		Dictionary proxyProperties = getProxyProperties(rsContainer,
				endpointDescription, rsReference);

		ServiceRegistration proxyRegistration = getContext().registerService(
				(String[]) interfaces.toArray(), proxy, proxyProperties);
		// Now create import registration for newly registered proxy
		return new ImportRegistration(rsContainer, rsReference,
				endpointDescription, proxyRegistration);
	}

	private Dictionary getProxyProperties(IRemoteServiceContainer rsContainer,
			EndpointDescription endpointDescription,
			IRemoteServiceReference rsReference) {
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

	public Collection<org.eclipse.ecf.osgi.services.remoteserviceadmin.ImportRegistration> getImportedRegistrations() {
		Collection<org.eclipse.ecf.osgi.services.remoteserviceadmin.ImportRegistration> results = new ArrayList<org.eclipse.ecf.osgi.services.remoteserviceadmin.ImportRegistration>();
		synchronized (importedRegistrations) {
			results.addAll(importedRegistrations);
		}
		return results;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ImportReference> getImportedEndpoints() {
		Collection<org.osgi.service.remoteserviceadmin.ImportReference> results = new ArrayList<org.osgi.service.remoteserviceadmin.ImportReference>();
		synchronized (importedRegistrations) {
			for (ImportRegistration reg : importedRegistrations) {
				results.add(reg.getImportReference());
			}
		}
		return results;
	}

	public void close() {
		synchronized (exportedRegistrations) {
			exportedRegistrations.clear();
		}
		synchronized (importedRegistrations) {
			importedRegistrations.clear();
		}
		super.close();
	}
}
