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
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.IDUtil;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
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
			ServiceReference serviceReference, Map<String, Object> properties) {
		String[] exportedInterfaces = (String[]) getPropertyValue(
				org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
				serviceReference, properties);
		String[] exportedConfigs = (String[]) getPropertyValue(
				org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS,
				serviceReference, properties);
		String[] serviceIntents = (String[]) getPropertyValue(
				org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
				serviceReference, properties);
		// Get a host container selector
		IHostContainerSelector hostContainerSelector = getHostContainerSelector();
		if (hostContainerSelector == null) {
			logError("handleServiceRegistering",
					"No hostContainerSelector available");
			return Collections.EMPTY_LIST;
		}
		// select ECF remote service containers that match given exported
		// interfaces, configs, and intents
		IRemoteServiceContainer[] rsContainers = hostContainerSelector
				.selectHostContainers(serviceReference, exportedInterfaces,
						exportedConfigs, serviceIntents);
		// If none found, log a warning and we're done
		if (rsContainers == null || rsContainers.length == 0) {
			logWarning(
					"handleServiceRegistered", "No remote service containers found for serviceReference=" //$NON-NLS-1$
							+ serviceReference
							+ ". Remote service NOT EXPORTED"); //$NON-NLS-1$
			return Collections.EMPTY_LIST;
		}
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> results = new ArrayList<org.osgi.service.remoteserviceadmin.ExportRegistration>();
		synchronized (exportedRegistrations) {
			for (int i = 0; i < rsContainers.length; i++) {
				ExportRegistration rsRegistration = null;
				try {
					rsRegistration = doExportService(serviceReference,
							properties, exportedInterfaces, serviceIntents,
							rsContainers[i]);
				} catch (Exception e) {
					logError("exportService",
							"Exception exporting serviceReference="
									+ serviceReference + " with properties="
									+ properties + " rsContainerID="
									+ rsContainers[i].getContainer().getID(), e);
					rsRegistration = new ExportRegistration(e);
				}
				results.add(rsRegistration);
				exportedRegistrations.add(rsRegistration);
			}
		}
		return results;
	}

	private ExportRegistration doExportService(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties,
			String[] exportedInterfaces, String[] serviceIntents,
			IRemoteServiceContainer rsContainer) throws Exception {
		IRemoteServiceRegistration remoteRegistration = null;
		try {
			Dictionary remoteServiceProperties = PropertiesUtil
					.createDictionaryFromMap(copyNonReservedProperties(
							serviceReference, overridingProperties,
							new TreeMap<String, Object>(
									String.CASE_INSENSITIVE_ORDER)));
			// Get container adapter
			IRemoteServiceContainerAdapter containerAdapter = rsContainer
					.getContainerAdapter();
			// If it's an IOSGiRemoteServiceContainerAdapter then call it one
			// way
			if (containerAdapter instanceof IOSGiRemoteServiceContainerAdapter) {
				IOSGiRemoteServiceContainerAdapter osgiContainerAdapter = (IOSGiRemoteServiceContainerAdapter) containerAdapter;
				remoteRegistration = osgiContainerAdapter
						.registerRemoteService(exportedInterfaces,
								serviceReference, remoteServiceProperties);
			} else {
				// call it the normal way
				remoteRegistration = containerAdapter.registerRemoteService(
						exportedInterfaces, getService(serviceReference),
						remoteServiceProperties);
			}
			// Create EndpointDescription from remoteRegistration
			EndpointDescription endpointDescription = createExportEndpointDescription(
					serviceReference, overridingProperties, exportedInterfaces,
					serviceIntents, remoteRegistration, rsContainer);
			// Create ExportRegistration
			return createExportRegistration(remoteRegistration,
					serviceReference, endpointDescription);
		} catch (Exception e) {
			// If we actually created an IRemoteRegistration then unregister
			if (remoteRegistration != null)
				remoteRegistration.unregister();
			// rethrow
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

	public org.osgi.service.remoteserviceadmin.ImportRegistration importService(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		// First check to see whether it's one of ECF's endpoint descriptions
		if (endpointDescription instanceof EndpointDescription) {
			EndpointDescription ed = (EndpointDescription) endpointDescription;
			// Now get IConsumerContainerSelector, to select the ECF container
			// for the given endpointDescription
			IConsumerContainerSelector consumerContainerSelector = getConsumerContainerSelector();
			// If there is none, then we can go no further
			if (consumerContainerSelector == null) {
				logError("importService",
						"No consumerContainerSelector available");
				return null;
			}
			// Select the rsContainer to handle the endpoint description
			IRemoteServiceContainer rsContainer = consumerContainerSelector
					.selectConsumerContainer(ed);
			// If none found, log a warning and we're done
			if (rsContainer == null) {
				logWarning(
						"importService", "No remote service container selected for endpoint=" //$NON-NLS-1$
								+ endpointDescription
								+ ". Remote service NOT IMPORTED"); //$NON-NLS-1$
				return null;
			}
			// If one selected then import the service to create an import
			// registration
			ImportRegistration importRegistration = null;
			synchronized (importedRegistrations) {
				try {
					importRegistration = doImportService(ed, rsContainer);
				} catch (Exception e) {
					importRegistration = handleImportServiceException(ed,
							rsContainer, e);
				} catch (NoClassDefFoundError e) {
					importRegistration = handleImportServiceException(ed,
							rsContainer, e);
				}
				// If we actually created an importRegistration...whether
				// successful or not, add it to the
				// set of imported registrationa
				if (importRegistration != null)
					importedRegistrations.add(importRegistration);
			}
			// Finally, return the importRegistration. It may be null or not.
			return importRegistration;
		} else {
			logWarning("importService", "endpointDescription="
					+ endpointDescription
					+ " is not ECFEndpointDescription...ignoring");
			return null;
		}
	}

	private ImportRegistration handleImportServiceException(
			EndpointDescription endpoint,
			IRemoteServiceContainer iRemoteServiceContainer, Throwable e) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Collection<String> getInterfaces(
			EndpointDescription endpointDescription) {
		return endpointDescription.getInterfaces();
	}

	protected ID getEndpointID(EndpointDescription endpointDescription) {
		return IDUtil.createContainerID(endpointDescription);
	}

	protected ID getConnectTargetID(EndpointDescription endpointDescription) {
		return endpointDescription.getConnectTargetID();
	}

	protected ID[] getIDFilter(EndpointDescription endpointDescription,
			ID endpointID) {
		ID[] idFilter = endpointDescription.getIDFilter();
		// If it is null,
		return (idFilter == null) ? new ID[] { endpointID } : idFilter;
	}

	protected String getRemoteServiceFilter(
			EndpointDescription endpointDescription) {
		long rsId = endpointDescription.getRemoteServiceId();
		if (rsId == 0) {
			// It's not known...so we just return the 'raw' remote service
			// filter
			return endpointDescription.getRemoteServiceFilter();
		} else {
			String edRsFilter = endpointDescription.getRemoteServiceFilter();
			// It's a real remote service id...so we return
			StringBuffer result = new StringBuffer("(&(") //$NON-NLS-1$
					.append(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID)
					.append("=").append(rsId).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			if (edRsFilter != null)
				result.append(edRsFilter);
			result.append(")"); //$NON-NLS-1$
			return result.toString();
		}
	}

	protected ImportRegistration handleNonOSGiService(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer) {
		// With non-OSGi service id (service id=0), we log a warning and return
		// null;
		logWarning("doImportService",
				"OSGi remote service id is 0 for endpointDescription="
						+ endpointDescription);
		return null;
	}

	private ImportRegistration doImportService(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer) throws Exception {
		long osgiServiceId = endpointDescription.getServiceId();
		if (osgiServiceId == 0)
			return handleNonOSGiService(endpointDescription, rsContainer);
		// Get interfaces from endpoint description
		Collection<String> interfaces = getInterfaces(endpointDescription);
		Assert.isNotNull(interfaces);
		Assert.isTrue(interfaces.size() > 0);
		// Get ECF endpoint ID...if this throws IDCreateException (because the
		// local system does not have
		// namespace for creating ID, or no namespace is present in
		// endpointDescription or endpoint id,
		// then it will be caught by the caller
		ID endpointID = getEndpointID(endpointDescription);
		Assert.isNotNull(endpointID);
		// Get connect target ID. May be null
		ID targetID = getConnectTargetID(endpointDescription);
		if (targetID == null) targetID = endpointID;
		// Get idFilter...also may be null
		ID[] idFilter = getIDFilter(endpointDescription, endpointID);
		// Get remote service filter
		String rsFilter = getRemoteServiceFilter(endpointDescription);
		// IRemoteServiceReferences from query
		Collection<IRemoteServiceReference> rsRefs = new ArrayList<IRemoteServiceReference>();
		// Get IRemoteServiceContainerAdapter
		IRemoteServiceContainerAdapter containerAdapter = rsContainer
				.getContainerAdapter();
		// rsContainerID
		ID rsContainerID = rsContainer.getContainer().getID();
		// For all given interfaces
		for (String intf : interfaces) {
			// Get/lookup remote service references
			IRemoteServiceReference[] refs = containerAdapter
					.getRemoteServiceReferences(targetID, idFilter, intf,
							rsFilter);
			if (refs == null) {
				logWarning("doImportService",
						"getRemoteServiceReferences return null for targetID="
								+ targetID + ",idFilter=" + idFilter + ",intf="
								+ intf + ",rsFilter=" + rsFilter
								+ " on rsContainerID=" + rsContainerID);
				continue;
			}
			for (int i = 0; i < refs.length; i++)
				rsRefs.add(refs[i]);
		}
		IRemoteServiceReference selectedRsReference = selectRemoteServiceReference(
				rsRefs, targetID, idFilter, interfaces, rsFilter, rsContainer);

		if (selectedRsReference == null) {
			logWarning("doImportService",
					"selectRemoteServiceReference returned null for rsRefs="
							+ rsRefs + ",targetID=" + targetID + ",idFilter="
							+ idFilter + ",interfaces=" + interfaces
							+ ",rsFilter=" + rsFilter + ",rsContainerID="
							+ rsContainerID);
			return null;
		}

		return createAndRegisterProxy(endpointDescription, rsContainer,
				selectedRsReference);
	}

	protected ImportRegistration createAndRegisterProxy(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer,
			IRemoteServiceReference selectedRsReference) throws Exception {
		IRemoteServiceContainerAdapter containerAdapter = rsContainer
				.getContainerAdapter();
		ID rsContainerID = rsContainer.getContainer().getID();
		// First get IRemoteService for selectedRsReference
		IRemoteService rs = containerAdapter
				.getRemoteService(selectedRsReference);
		
		if (rs == null)
			throw new ECFException(
					"getRemoteService returned null for selectedRsReference="
							+ selectedRsReference + ",rsContainerID="
							+ rsContainerID);
		// Now get proxy from IRemoteService
		Object proxy = rs.getProxy();
		if (proxy == null)
			throw new ECFException("getProxy returned null for rsReference="
					+ selectedRsReference + ",rsContainerID=" + rsContainerID);

		Map proxyProperties = createProxyProperties(endpointDescription,
				rsContainer, selectedRsReference, rs, proxy);

		List<String> interfaces = endpointDescription.getInterfaces();
		
		ServiceRegistration proxyRegistration = getContext().registerService(
				(String[]) interfaces.toArray(new String[interfaces.size()]), proxy,
				(Dictionary) PropertiesUtil.createDictionaryFromMap(proxyProperties));
		
		// Now create import registration for newly registered proxy
		return new ImportRegistration(rsContainer, selectedRsReference,
				endpointDescription, proxyRegistration);
	}

	protected IRemoteServiceReference selectRemoteServiceReference(
			Collection<IRemoteServiceReference> rsRefs, ID targetID,
			ID[] idFilter, Collection<String> interfaces, String rsFilter,
			IRemoteServiceContainer rsContainer) {
		if (rsRefs.size() == 0)
			return null;
		if (rsRefs.size() > 1) {
			logWarning("selectRemoteServiceReference", "rsRefs=" + rsRefs
					+ ",targetID=" + targetID + ",idFilter=" + idFilter
					+ ",interfaces=" + interfaces + ",rsFilter=" + rsFilter
					+ ",rsContainer=" + rsContainer.getContainer().getID()
					+ " has " + rsRefs.size()
					+ " values.  Selecting the first element");
		}
		return rsRefs.iterator().next();
	}

	protected Map createProxyProperties(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer,
			IRemoteServiceReference rsReference, IRemoteService remoteService, Object proxy) {
		
		Map resultProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);
		copyNonReservedProperties(rsReference, resultProperties);
		// remove OBJECTCLASS
		resultProperties.remove(org.eclipse.ecf.remoteservice.Constants.OBJECTCLASS);
		// remove remote service id
		resultProperties.remove(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		// Set intents if there are intents
		Object intentsValue = PropertiesUtil.getStringPlusValue(endpointDescription.getIntents());
		if (intentsValue != null) resultProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS, intentsValue);
		// Set service.imported to IRemoteService
		resultProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED, remoteService);
		String[] exporterSupportedConfigs = (String[]) endpointDescription.getProperties().get(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
		String[] importedConfigs = getImportedConfigs(rsContainer.getContainer(), exporterSupportedConfigs);
		// Set service.imported.configs
		resultProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS, importedConfigs);
		return resultProperties;
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

	private ExportRegistration[] findExportRegistrations(
			ServiceReference serviceReference) {
		List<ExportRegistration> results = new ArrayList<ExportRegistration>();
		for (ExportRegistration exportReg : exportedRegistrations)
			if (exportReg.matchesServiceReference(serviceReference))
				results.add(exportReg);
		return results.toArray(new ExportRegistration[results.size()]);
	}

	public EndpointDescription[] unexportService(
			ServiceReference serviceReference) {
		List<EndpointDescription> endpointDescriptions = new ArrayList<EndpointDescription>();
		synchronized (exportedRegistrations) {
			ExportRegistration[] exportRegs = findExportRegistrations(serviceReference);
			if (exportRegs != null) {
				for (int i = 0; i < exportRegs.length; i++) {
					org.osgi.service.remoteserviceadmin.ExportReference exportRef = exportRegs[i]
							.getExportReference();
					if (exportRef != null) {
						org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription = exportRef
								.getExportedEndpoint();
						if (endpointDescription != null
								&& endpointDescription instanceof EndpointDescription) {
							endpointDescriptions
									.add((EndpointDescription) endpointDescription);
						}
					}
					exportRegs[i].close();
					exportedRegistrations.remove(exportRegs[i]);
				}
			}
		}
		return endpointDescriptions
				.toArray(new EndpointDescription[endpointDescriptions.size()]);
	}
}
