/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.eclipse.ecf.remoteservice.IOSGiRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceUnregisteredEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.remoteserviceadmin.EndpointPermission;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;
import org.osgi.util.tracker.ServiceTracker;

/**
 * ECF implementation of <a
 * href="http://www.osgi.org/download/r4v42/r4.enterprise.pdf">OSGI 4.2 Remote
 * Service Admin service</a>. This service can be used by topology managers to
 * to export and/or import remote services with any <a
 * href="http://wiki.eclipse.org/ECF_Connection_Creation_and_Management">ECF
 * container</a> that implements the <a
 * href="http://wiki.eclipse.org/ECF/API_Docs#Remote_Services_API">ECF remote
 * service API</a>.
 */
public class RemoteServiceAdmin implements
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin {

	public static final String SERVICE_PROP = "org.eclipse.ecf.rsa"; //$NON-NLS-1$

	private Bundle clientBundle;

	private boolean hostAutoCreateContainer = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.remoteserviceadmin.hostAutoCreateContainer", //$NON-NLS-1$
					"true")).booleanValue(); //$NON-NLS-1$
	private String[] hostDefaultConfigTypes = new String[] { System
			.getProperty(
					"org.eclipse.ecf.osgi.services.remoteserviceadmin.hostDefaultConfigType", //$NON-NLS-1$
					"ecf.generic.server") }; //$NON-NLS-1$

	private boolean consumerAutoCreateContainer = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.remoteserviceadmin.consumerAutoCreateContainer", //$NON-NLS-1$
					"true")).booleanValue(); //$NON-NLS-1$

	private ServiceTracker packageAdminTracker;
	private Object packageAdminTrackerLock = new Object();

	private Object eventAdminTrackerLock = new Object();
	private ServiceTracker eventAdminTracker;

	private Object remoteServiceAdminListenerTrackerLock = new Object();
	private ServiceTracker remoteServiceAdminListenerTracker;

	private HostContainerSelector defaultHostContainerSelector;
	private ServiceRegistration defaultHostContainerSelectorRegistration;

	private ConsumerContainerSelector defaultConsumerContainerSelector;
	private ServiceRegistration defaultConsumerContainerSelectorRegistration;

	private Collection<ExportRegistration> exportedRegistrations = new ArrayList<ExportRegistration>();
	private Collection<ImportRegistration> importedRegistrations = new ArrayList<ImportRegistration>();

	public RemoteServiceAdmin(Bundle clientBundle) {
		this.clientBundle = clientBundle;
		Assert.isNotNull(this.clientBundle);
		trace("RemoteServiceAdmin<init>", //$NON-NLS-1$
				"clientBundle=" + clientBundle.getSymbolicName()); //$NON-NLS-1$
		// Only setup defaults if it hasn't already been done by some other
		// Remote Service Admin instance
		Properties props = new Properties();
		props.put(org.osgi.framework.Constants.SERVICE_RANKING, new Integer(
				Integer.MIN_VALUE));
		// host container selector
		ServiceReference[] hostContainerSelectorRefs = null;
		try {
			hostContainerSelectorRefs = getClientBundleContext()
					.getServiceReferences(
							IHostContainerSelector.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			// will not happen
		}
		// register a default only if no others already exist
		if (hostContainerSelectorRefs == null
				|| hostContainerSelectorRefs.length == 0) {
			defaultHostContainerSelector = new HostContainerSelector(
					hostDefaultConfigTypes, hostAutoCreateContainer);
			defaultHostContainerSelectorRegistration = getClientBundleContext()
					.registerService(IHostContainerSelector.class.getName(),
							defaultHostContainerSelector, (Dictionary) props);
		}
		// consumer container selector
		ServiceReference[] consumerContainerSelectorRefs = null;
		try {
			consumerContainerSelectorRefs = getClientBundleContext()
					.getServiceReferences(
							IConsumerContainerSelector.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			// will not happen
		}
		// register a default only if no others already exist
		if (consumerContainerSelectorRefs == null
				|| consumerContainerSelectorRefs.length == 0) {
			defaultConsumerContainerSelector = new ConsumerContainerSelector(
					consumerAutoCreateContainer);
			defaultConsumerContainerSelectorRegistration = getClientBundleContext()
					.registerService(
							IConsumerContainerSelector.class.getName(),
							defaultConsumerContainerSelector,
							(Dictionary) props);
		}
	}

	private boolean validExportedInterfaces(ServiceReference serviceReference,
			String[] exportedInterfaces) {
		if (exportedInterfaces == null || exportedInterfaces.length == 0)
			return false;
		List<String> objectClassList = Arrays
				.asList((String[]) serviceReference
						.getProperty(org.osgi.framework.Constants.OBJECTCLASS));
		for (int i = 0; i < exportedInterfaces.length; i++)
			if (!objectClassList.contains(exportedInterfaces[i]))
				return false;
		return true;
	}

	// RemoteServiceAdmin service interface impl methods
	public Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> exportService(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties) {

		trace("exportService", "serviceReference=" + serviceReference //$NON-NLS-1$ //$NON-NLS-2$
				+ ",properties=" + overridingProperties); //$NON-NLS-1$

		overridingProperties = PropertiesUtil.mergeProperties(serviceReference,
				overridingProperties == null ? Collections.EMPTY_MAP
						: overridingProperties);

		// First get exported interfaces
		String[] exportedInterfaces = PropertiesUtil.getExportedInterfaces(
				serviceReference, overridingProperties);
		if (exportedInterfaces == null)
			throw new IllegalArgumentException(
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES
							+ " not set"); //$NON-NLS-1$
		// verifyExportedInterfaces
		if (!validExportedInterfaces(serviceReference, exportedInterfaces))
			return Collections.EMPTY_LIST;

		// Get optional exported configs
		String[] exportedConfigs = PropertiesUtil
				.getStringArrayFromPropertyValue(overridingProperties
						.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS));
		if (exportedConfigs == null) {
			exportedConfigs = PropertiesUtil
					.getStringArrayFromPropertyValue(serviceReference
							.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS));
		}
		// Get all intents (service.intents, service.exported.intents,
		// service.exported.intents.extra)
		String[] serviceIntents = PropertiesUtil.getServiceIntents(
				serviceReference, overridingProperties);

		// Get a host container selector, and use it to
		IHostContainerSelector hostContainerSelector = getHostContainerSelector();
		// select ECF remote service containers that match given exported
		// interfaces, configs, and intents
		IRemoteServiceContainer[] rsContainers = null;
		try {
			rsContainers = hostContainerSelector.selectHostContainers(
					serviceReference, overridingProperties, exportedInterfaces,
					exportedConfigs, serviceIntents);
		} catch (SelectContainerException e) {
			ExportRegistration errorRegistration = createErrorExportRegistration(
					serviceReference, overridingProperties,
					"Error selecting or creating host container for serviceReference=" //$NON-NLS-1$
							+ serviceReference + " properties=" //$NON-NLS-1$
							+ overridingProperties, e);
			Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> result = new ArrayList<org.osgi.service.remoteserviceadmin.ExportRegistration>();
			result.add(errorRegistration);
			publishExportEvent(errorRegistration);
			return result;
		}
		// If none found, log warning and return
		if (rsContainers == null || rsContainers.length == 0) {
			String errorMessage = "No containers found for serviceReference=" //$NON-NLS-1$ 
					+ serviceReference
					+ " properties=" + overridingProperties + ". Remote service NOT EXPORTED"; //$NON-NLS-1$//$NON-NLS-2$
			logWarning("exportService", errorMessage); //$NON-NLS-1$
			return Collections.EMPTY_LIST;
		}
		Collection<ExportRegistration> exportRegistrations = new ArrayList<ExportRegistration>();
		synchronized (exportedRegistrations) {
			for (int i = 0; i < rsContainers.length; i++) {
				ExportRegistration exportRegistration = null;
				// If we've already got an export endpoint
				// for this service reference/containerID combination,
				// then create an ExportRegistration that uses the endpoint
				ExportEndpoint exportEndpoint = findExistingExportEndpoint(
						serviceReference, rsContainers[i].getContainer()
								.getID());
				// If we've already got one, then create a new
				// ExportRegistration for it and we're done
				if (exportEndpoint != null)
					exportRegistration = new ExportRegistration(exportEndpoint);
				else {
					Map endpointDescriptionProperties = createExportEndpointDescriptionProperties(
							serviceReference, overridingProperties,
							exportedInterfaces, serviceIntents, rsContainers[i]);
					// otherwise, actually export the service to create a new
					// ExportEndpoint and use it to create a new
					// ExportRegistration
					EndpointDescription endpointDescription = new EndpointDescription(
							serviceReference, endpointDescriptionProperties);
					try {
						// Check security access for export
						checkEndpointPermission(endpointDescription,
								EndpointPermission.EXPORT);
						// Actually do the export and return export registration
						exportRegistration = exportService(serviceReference,
								overridingProperties, exportedInterfaces,
								rsContainers[i], endpointDescriptionProperties);
					} catch (Exception e) {
						exportRegistration = new ExportRegistration(e,
								endpointDescription);
					}
				}
				// If no exception, we add it to our known set of exported
				// registrations
				if (exportRegistration.getException() == null)
					addExportRegistration(exportRegistration);
				// We add it to the results in either case
				exportRegistrations.add(exportRegistration);
			}
		}
		// publish all activeExportRegistrations
		for (ExportRegistration exportReg : exportRegistrations)
			publishExportEvent(exportReg);
		// and return
		return new ArrayList<org.osgi.service.remoteserviceadmin.ExportRegistration>(
				exportRegistrations);
	}

	private ExportRegistration createErrorExportRegistration(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties, String errorMessage,
			SelectContainerException exception) {
		ContainerTypeDescription ctd = exception.getContainerTypeDescription();
		overridingProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
						"noendpoint"); //$NON-NLS-1$
		overridingProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
						(ctd == null) ? "noconfig" : ctd.getName()); //$NON-NLS-1$
		return new ExportRegistration(exception, new EndpointDescription(
				serviceReference, overridingProperties));
	}

	public org.osgi.service.remoteserviceadmin.ImportRegistration importService(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {

		trace("importService", "endpointDescription=" + endpointDescription); //$NON-NLS-1$ //$NON-NLS-2$
		// First, make sure that the client bundle has the IMPORT endpoint
		// permission
		checkEndpointPermission(endpointDescription, EndpointPermission.IMPORT);

		EndpointDescription ed = null;
		if (endpointDescription instanceof EndpointDescription)
			ed = (EndpointDescription) endpointDescription;
		else
			ed = new EndpointDescription(endpointDescription.getProperties());

		// Now get IConsumerContainerSelector, to select the ECF container
		// for the given endpointDescription
		IConsumerContainerSelector consumerContainerSelector = getConsumerContainerSelector();
		// If there is none, then we can go no further
		if (consumerContainerSelector == null) {
			logError("importService", //$NON-NLS-1$
					"No defaultConsumerContainerSelector available"); //$NON-NLS-1$
			return null;
		}
		// Select the rsContainer to handle the endpoint description
		IRemoteServiceContainer rsContainer = null;
		try {
			rsContainer = consumerContainerSelector.selectConsumerContainer(ed);
		} catch (SelectContainerException e) {
			ImportRegistration errorRegistration = new ImportRegistration(ed, e);
			publishImportEvent(errorRegistration);
			return errorRegistration;
		}
		// If none found, log a warning and we're done
		if (rsContainer == null) {
			logWarning(
					"importService", "No remote service container selected for endpoint=" //$NON-NLS-1$ //$NON-NLS-2$
							+ endpointDescription
							+ ". Remote service NOT IMPORTED"); //$NON-NLS-1$
			return null;
		}
		// If one selected then import the service to create an import
		// registration
		ImportRegistration importRegistration = null;
		synchronized (importedRegistrations) {
			ImportEndpoint importEndpoint = findImportEndpoint(ed);
			if (importEndpoint != null)
				importRegistration = new ImportRegistration(importEndpoint);
			else {
				importRegistration = importService(ed, rsContainer);
				if (importRegistration.getException() == null)
					addImportRegistration(importRegistration);
			}
		}
		// publish import event
		publishImportEvent(importRegistration);
		// Finally, return the importRegistration. It may be null or not.
		return importRegistration;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ExportReference> getExportedServices() {
		Collection<org.osgi.service.remoteserviceadmin.ExportReference> results = new ArrayList<org.osgi.service.remoteserviceadmin.ExportReference>();
		synchronized (exportedRegistrations) {
			for (ExportRegistration reg : exportedRegistrations) {
				org.osgi.service.remoteserviceadmin.ExportReference eRef = reg
						.getExportReference();
				if (eRef != null
						&& checkEndpointPermissionRead("getExportedServices", //$NON-NLS-1$
								eRef.getExportedEndpoint()))
					results.add(eRef);
			}
		}
		return results;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ImportReference> getImportedEndpoints() {
		Collection<org.osgi.service.remoteserviceadmin.ImportReference> results = new ArrayList<org.osgi.service.remoteserviceadmin.ImportReference>();
		synchronized (importedRegistrations) {
			for (ImportRegistration reg : importedRegistrations) {
				org.osgi.service.remoteserviceadmin.ImportReference iRef = reg
						.getImportReference();
				if (iRef != null
						&& checkEndpointPermissionRead("getImportedEndpoints", //$NON-NLS-1$
								iRef.getImportedEndpoint()))
					results.add(iRef);
			}
		}
		return results;
	}

	// end RemoteServiceAdmin service interface impl methods

	private boolean checkEndpointPermissionRead(
			String methodName,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		try {
			checkEndpointPermission(endpointDescription,
					EndpointPermission.READ);
			return true;
		} catch (SecurityException e) {
			logError(methodName,
					"permission check failed for read access to endpointDescription=" //$NON-NLS-1$
							+ endpointDescription, e);
			return false;
		}
	}

	private BundleContext getClientBundleContext() {
		return clientBundle.getBundleContext();
	}

	private Bundle getRSABundle() {
		BundleContext bundleContext = Activator.getContext();
		if (bundleContext == null)
			return null;
		return bundleContext.getBundle();
	}

	private void addImportRegistration(ImportRegistration importRegistration) {
		synchronized (importedRegistrations) {
			importedRegistrations.add(importRegistration);
			trace("addImportRegistration", "importRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
					+ importRegistration + " importedRegistrations=" //$NON-NLS-1$
					+ importedRegistrations);
		}
	}

	private void addExportRegistration(ExportRegistration exportRegistration) {
		synchronized (exportedRegistrations) {
			exportedRegistrations.add(exportRegistration);
			trace("addExportRegistration", "exportRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
					+ exportRegistration + " exportedRegistrations=" //$NON-NLS-1$
					+ exportedRegistrations);
		}
	}

	private boolean removeExportRegistration(
			ExportRegistration exportRegistration) {
		synchronized (exportedRegistrations) {
			boolean removed = exportedRegistrations.remove(exportRegistration);
			trace("removeExportRegistration", "exportRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
					+ exportRegistration + " exportedRegistrations=" //$NON-NLS-1$
					+ exportedRegistrations + " removed=" + removed); //$NON-NLS-1$
			return removed;
		}
	}

	private boolean removeImportRegistration(
			ImportRegistration importRegistration) {
		synchronized (importedRegistrations) {
			boolean removed = importedRegistrations.remove(importRegistration);
			trace("removeImportRegistration", "importRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
					+ importRegistration + " importedRegistrations=" //$NON-NLS-1$
					+ importedRegistrations + " removed=" + removed); //$NON-NLS-1$
			return removed;
		}
	}

	private void closeDefaultContainerSelectors() {
		if (defaultHostContainerSelectorRegistration != null) {
			defaultHostContainerSelectorRegistration.unregister();
			defaultHostContainerSelectorRegistration = null;
		}
		if (defaultHostContainerSelector != null) {
			defaultHostContainerSelector.close();
			defaultHostContainerSelector = null;
		}
		if (defaultConsumerContainerSelectorRegistration != null) {
			defaultConsumerContainerSelectorRegistration.unregister();
			defaultConsumerContainerSelectorRegistration = null;
		}
		if (defaultConsumerContainerSelector != null) {
			defaultConsumerContainerSelector.close();
			defaultConsumerContainerSelector = null;
		}
	}

	private void checkEndpointPermission(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			String permissionType) throws SecurityException {
		SecurityManager sm = System.getSecurityManager();
		if (sm == null)
			return;
		sm.checkPermission(new EndpointPermission(endpointDescription,
				Activator.getDefault().getFrameworkUUID(), permissionType));
	}

	class ExportEndpoint {

		private ServiceReference serviceReference;
		private EndpointDescription endpointDescription;

		private IRemoteServiceRegistration rsRegistration;
		private Set<ExportRegistration> activeExportRegistrations = new HashSet<ExportRegistration>();

		ExportEndpoint(ServiceReference serviceReference,
				EndpointDescription endpointDescription,
				IRemoteServiceRegistration reg) {
			Assert.isNotNull(serviceReference);
			this.serviceReference = serviceReference;
			Assert.isNotNull(endpointDescription);
			this.endpointDescription = endpointDescription;
			Assert.isNotNull(reg);
			this.rsRegistration = reg;
		}

		synchronized ID getContainerID() {
			return endpointDescription.getContainerID();
		}

		synchronized ServiceReference getServiceReference() {
			return serviceReference;
		}

		synchronized EndpointDescription getEndpointDescription() {
			return endpointDescription;
		}

		synchronized IRemoteServiceRegistration getRemoteServiceRegistration() {
			return rsRegistration;
		}

		synchronized boolean addExportRegistration(
				ExportRegistration exportRegistration) {
			return this.activeExportRegistrations.add(exportRegistration);
		}

		synchronized boolean close(ExportRegistration exportRegistration) {
			boolean removed = this.activeExportRegistrations
					.remove(exportRegistration);
			if (removed && activeExportRegistrations.size() == 0) {
				if (rsRegistration != null) {
					rsRegistration.unregister();
					rsRegistration = null;
				}
				serviceReference = null;
				endpointDescription = null;
			}
			return removed;
		}
	}

	class ExportRegistration implements
			org.osgi.service.remoteserviceadmin.ExportRegistration {

		private ExportReference exportReference;

		private boolean closed = false;

		ExportRegistration(ExportEndpoint exportEndpoint) {
			Assert.isNotNull(exportEndpoint);
			exportEndpoint.addExportRegistration(this);
			this.exportReference = new ExportReference(exportEndpoint);
		}

		ExportRegistration(Throwable exception,
				EndpointDescription errorEndpointDescription) {
			Assert.isNotNull(exception);
			this.exportReference = new ExportReference(exception,
					errorEndpointDescription);
			this.closed = true;
		}

		ID getContainerID() {
			return exportReference.getContainerID();
		}

		ServiceReference getServiceReference() {
			return exportReference.getExportedService();
		}

		public org.osgi.service.remoteserviceadmin.ExportReference getExportReference() {
			Throwable t = getException();
			if (t != null)
				throw new IllegalStateException(
						"Cannot get export reference as export registration is invalid", //$NON-NLS-1$
						t);
			return exportReference;
		}

		boolean match(ServiceReference serviceReference) {
			return match(serviceReference, null);
		}

		boolean match(ServiceReference serviceReference,
				ID containerID) {
			ServiceReference ourServiceReference = getServiceReference();
			if (ourServiceReference == null)
				return false;
			boolean serviceReferenceCompare = ourServiceReference
					.equals(serviceReference);
			// If the second parameter is null, then we compare only on service
			// references
			if (containerID == null)
				return serviceReferenceCompare;
			ID ourContainerID = getContainerID();
			if (ourContainerID == null)
				return false;
			return serviceReferenceCompare
					&& ourContainerID.equals(containerID);
		}

		synchronized ExportEndpoint getExportEndpoint(
				ServiceReference serviceReference, ID containerID) {
			return match(serviceReference, containerID) ? exportReference
					.getExportEndpoint() : null;
		}

		IRemoteServiceRegistration getRemoteServiceRegistration() {
			return exportReference.getRemoteServiceRegistration();
		}

		EndpointDescription getEndpointDescription() {
			return exportReference.getEndpointDescription();
		}

		public void close() {
			boolean publish = false;
			ID containerID = null;
			Throwable exception = null;
			EndpointDescription endpointDescription = null;
			synchronized (this) {
				// Only do this once
				if (!closed) {
					containerID = getContainerID();
					exception = getException();
					endpointDescription = getEndpointDescription();
					publish = exportReference.close(this);
					closed = true;
				}
			}
			removeExportRegistration(this);
			Bundle rsaBundle = getRSABundle();
			// Only publish events
			if (publish && rsaBundle != null)
				publishEvent(new RemoteServiceAdminEvent(containerID,
						RemoteServiceAdminEvent.EXPORT_UNREGISTRATION,
						rsaBundle, exportReference, exception),
						endpointDescription);
		}

		public Throwable getException() {
			return exportReference.getException();
		}

	}

	class ExportReference implements
			org.osgi.service.remoteserviceadmin.ExportReference {

		private ExportEndpoint exportEndpoint;

		private Throwable exception;
		private EndpointDescription errorEndpointDescription;

		ExportReference(ExportEndpoint exportEndpoint) {
			Assert.isNotNull(exportEndpoint);
			this.exportEndpoint = exportEndpoint;
		}

		ExportReference(Throwable exception,
				EndpointDescription errorEndpointDescription) {
			Assert.isNotNull(exception);
			this.exception = exception;
			Assert.isNotNull(exception);
			this.errorEndpointDescription = errorEndpointDescription;
		}

		synchronized Throwable getException() {
			return exception;
		}

		synchronized boolean close(ExportRegistration exportRegistration) {
			if (exportEndpoint == null)
				return false;
			boolean result = exportEndpoint.close(exportRegistration);
			exportEndpoint = null;
			return result;
		}

		synchronized ExportEndpoint getExportEndpoint() {
			return exportEndpoint;
		}

		synchronized IRemoteServiceRegistration getRemoteServiceRegistration() {
			return (exportEndpoint == null) ? null : exportEndpoint
					.getRemoteServiceRegistration();
		}

		synchronized ID getContainerID() {
			return (exportEndpoint == null) ? null : exportEndpoint
					.getContainerID();
		}

		public synchronized ServiceReference getExportedService() {
			return (exportEndpoint == null) ? null : exportEndpoint
					.getServiceReference();
		}

		public synchronized org.osgi.service.remoteserviceadmin.EndpointDescription getExportedEndpoint() {
			return (exportEndpoint == null) ? null : exportEndpoint
					.getEndpointDescription();
		}

		synchronized EndpointDescription getEndpointDescription() {
			return (exportEndpoint == null) ? errorEndpointDescription
					: exportEndpoint.getEndpointDescription();
		}

	}

	class ImportEndpoint {

		private IRemoteServiceContainerAdapter rsContainerAdapter;
		private EndpointDescription endpointDescription;
		private IRemoteServiceListener rsListener;
		private IRemoteServiceReference rsReference;
		private ServiceRegistration proxyRegistration;
		private Set<ImportRegistration> activeImportRegistrations = new HashSet<ImportRegistration>();

		ImportEndpoint(IRemoteServiceContainerAdapter rsContainerAdapter,
				IRemoteServiceReference rsReference,
				IRemoteServiceListener rsListener,
				ServiceRegistration proxyRegistration,
				EndpointDescription endpointDescription) {
			this.rsContainerAdapter = rsContainerAdapter;
			this.endpointDescription = endpointDescription;
			this.rsReference = rsReference;
			this.rsListener = rsListener;
			this.proxyRegistration = proxyRegistration;
			// Add the remoteservice listener to the container adapter, so that
			// the rsListener notified asynchronously if our underlying remote
			// service
			// reference is unregistered locally due to disconnect or remote
			// ejection
			this.rsContainerAdapter.addRemoteServiceListener(this.rsListener);
		}

		synchronized EndpointDescription getEndpointDescription() {
			return endpointDescription;
		}

		synchronized ServiceRegistration getProxyRegistration() {
			return proxyRegistration;
		}

		synchronized ID getContainerID() {
			return (rsReference == null) ? null : rsReference.getContainerID();
		}

		synchronized boolean addImportRegistration(
				ImportRegistration importRegistration) {
			return this.activeImportRegistrations.add(importRegistration);
		}

		synchronized boolean close(ImportRegistration importRegistration) {
			boolean removed = this.activeImportRegistrations
					.remove(importRegistration);
			if (removed && activeImportRegistrations.size() == 0) {
				if (proxyRegistration != null) {
					proxyRegistration.unregister();
					proxyRegistration = null;
				}
				if (rsContainerAdapter != null) {
					if (rsReference != null) {
						rsContainerAdapter.ungetRemoteService(rsReference);
						rsReference = null;
					}
					// remove remote service listener
					if (rsListener != null) {
						rsContainerAdapter
								.removeRemoteServiceListener(rsListener);
						rsListener = null;
					}
					rsContainerAdapter = null;
				}
				endpointDescription = null;
			}
			return removed;
		}

		synchronized boolean match(IRemoteServiceID remoteServiceID) {
			if (remoteServiceID == null || rsReference == null)
				return false;
			return rsReference.getID().equals(remoteServiceID);
		}

		synchronized boolean match(EndpointDescription ed) {
			if (activeImportRegistrations.size() == 0)
				return false;
			return this.endpointDescription.isSameService(ed);
		}

	}

	class ImportRegistration implements
			org.osgi.service.remoteserviceadmin.ImportRegistration {

		private ImportReference importReference;

		private boolean closed = false;

		ImportRegistration(ImportEndpoint importEndpoint) {
			Assert.isNotNull(importEndpoint);
			importEndpoint.addImportRegistration(this);
			this.importReference = new ImportReference(importEndpoint);
		}

		ImportRegistration(EndpointDescription errorEndpointDescription,
				Throwable exception) {
			this.importReference = new ImportReference(
					errorEndpointDescription, exception);
		}

		ID getContainerID() {
			return importReference.getContainerID();
		}

		EndpointDescription getEndpointDescription() {
			return importReference.getEndpointDescription();
		}

		boolean match(IRemoteServiceID remoteServiceID) {
			return importReference.match(remoteServiceID);
		}

		ImportEndpoint getImportEndpoint(EndpointDescription ed) {
			return importReference.match(ed);
		}

		public org.osgi.service.remoteserviceadmin.ImportReference getImportReference() {
			Throwable t = getException();
			if (t != null)
				throw new IllegalStateException(
						"Cannot get import reference as registration not properly initialized", //$NON-NLS-1$
						t);
			return importReference;
		}

		public void close() {
			boolean publish = false;
			ID containerID = null;
			Throwable exception = null;
			EndpointDescription endpointDescription = null;
			synchronized (this) {
				// only do this once
				if (!closed) {
					containerID = getContainerID();
					exception = getException();
					endpointDescription = getEndpointDescription();
					publish = importReference.close(this);
					closed = true;
				}
			}
			removeImportRegistration(this);
			Bundle rsaBundle = getRSABundle();
			if (publish && rsaBundle != null)
				publishEvent(
						new RemoteServiceAdminEvent(
								containerID,
								RemoteServiceAdminEvent.IMPORT_UNREGISTRATION,
								rsaBundle, importReference, exception),
						endpointDescription);

		}

		public Throwable getException() {
			return importReference.getException();
		}

	}

	class ImportReference implements
			org.osgi.service.remoteserviceadmin.ImportReference {

		private ImportEndpoint importEndpoint;

		private Throwable exception;
		private EndpointDescription errorEndpointDescription;

		ImportReference(ImportEndpoint importEndpoint) {
			Assert.isNotNull(importEndpoint);
			this.importEndpoint = importEndpoint;
		}

		ImportReference(EndpointDescription endpointDescription,
				Throwable exception) {
			Assert.isNotNull(exception);
			this.exception = exception;
			Assert.isNotNull(endpointDescription);
			this.errorEndpointDescription = endpointDescription;
		}

		synchronized Throwable getException() {
			return exception;
		}

		synchronized boolean match(IRemoteServiceID remoteServiceID) {
			return (importEndpoint == null) ? null : importEndpoint
					.match(remoteServiceID);
		}

		synchronized ImportEndpoint match(EndpointDescription ed) {
			if (importEndpoint != null && importEndpoint.match(ed))
				return importEndpoint;
			return null;
		}

		synchronized EndpointDescription getEndpointDescription() {
			return (importEndpoint == null) ? errorEndpointDescription
					: importEndpoint.getEndpointDescription();
		}

		synchronized ID getContainerID() {
			return (importEndpoint == null) ? null : importEndpoint
					.getContainerID();
		}

		public synchronized ServiceReference getImportedService() {
			return (importEndpoint == null) ? null : importEndpoint
					.getProxyRegistration().getReference();
		}

		public synchronized org.osgi.service.remoteserviceadmin.EndpointDescription getImportedEndpoint() {
			return (importEndpoint == null) ? null : importEndpoint
					.getEndpointDescription();
		}

		synchronized boolean close(ImportRegistration importRegistration) {
			if (importEndpoint == null)
				return false;
			boolean result = importEndpoint.close(importRegistration);
			importEndpoint = null;
			return result;
		}

	}

	private void publishEvent(RemoteServiceAdminEvent event,
			EndpointDescription endpointDescription) {
		// send event synchronously to RemoteServiceAdminListeners
		EndpointPermission perm = new EndpointPermission(endpointDescription,
				Activator.getDefault().getFrameworkUUID(),
				EndpointPermission.READ);
		// notify synchronously all appropriate listeners (those with READ
		// permission)
		RemoteServiceAdminListener[] listeners = getListeners(perm);
		if (listeners != null)
			for (int i = 0; i < listeners.length; i++)
				listeners[i].remoteAdminEvent(event);
		// Now also post the event asynchronously to EventAdmin
		postEvent(event, endpointDescription);
	}

	private void postEvent(RemoteServiceAdminEvent event,
			EndpointDescription endpointDescription) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin == null) {
			//logWarning("RemoteServiceAdmin.postEvent", "No event admin service available to post event=" //$NON-NLS-1$ //$NON-NLS-2$
			//		+ event);
			return;
		}
		int eventType = event.getType();
		String eventTypeName = null;
		String registrationTypeName = null;
		switch (eventType) {
		case (RemoteServiceAdminEvent.EXPORT_REGISTRATION):
			eventTypeName = "EXPORT_REGISTRATION"; //$NON-NLS-1$
			registrationTypeName = "export.registration";//$NON-NLS-1$
			break;
		case (RemoteServiceAdminEvent.EXPORT_ERROR):
			eventTypeName = "EXPORT_ERROR"; //$NON-NLS-1$
			registrationTypeName = "export.registration";//$NON-NLS-1$
			break;
		case (RemoteServiceAdminEvent.EXPORT_UNREGISTRATION):
			eventTypeName = "EXPORT_UNREGISTRATION"; //$NON-NLS-1$
			registrationTypeName = "export.registration";//$NON-NLS-1$
			break;
		case (RemoteServiceAdminEvent.EXPORT_WARNING):
			eventTypeName = "EXPORT_WARNING"; //$NON-NLS-1$
			registrationTypeName = "export.registration";//$NON-NLS-1$
			break;
		case (RemoteServiceAdminEvent.IMPORT_REGISTRATION):
			eventTypeName = "IMPORT_REGISTRATION"; //$NON-NLS-1$
			registrationTypeName = "import.registration";//$NON-NLS-1$
			break;
		case (RemoteServiceAdminEvent.IMPORT_ERROR):
			eventTypeName = "IMPORT_ERROR"; //$NON-NLS-1$
			registrationTypeName = "import.registration";//$NON-NLS-1$
			break;
		case (RemoteServiceAdminEvent.IMPORT_UNREGISTRATION):
			eventTypeName = "IMPORT_UNREGISTRATION"; //$NON-NLS-1$
			registrationTypeName = "import.registration";//$NON-NLS-1$
			break;
		case (RemoteServiceAdminEvent.IMPORT_WARNING):
			eventTypeName = "IMPORT_WARNING"; //$NON-NLS-1$
			registrationTypeName = "import.registration";//$NON-NLS-1$
			break;
		}
		if (eventTypeName == null) {
			logError("postEvent", "Event type=" + eventType //$NON-NLS-1$ //$NON-NLS-2$
					+ " not understood for event=" + event + ".  Not posting"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		String topic = "org/osgi/service/remoteserviceadmin/" + eventTypeName; //$NON-NLS-1$
		Bundle rsaBundle = getRSABundle();
		if (rsaBundle == null) {
			logError(
					"postEvent", "RSA Bundle is null.  Not posting remote service admin event=" + event); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		Dictionary eventProperties = new Properties();
		eventProperties.put("bundle", rsaBundle); //$NON-NLS-1$
		eventProperties.put("bundle.id", //$NON-NLS-1$
				new Long(rsaBundle.getBundleId()));
		eventProperties.put("bundle.symbolicname", //$NON-NLS-1$
				rsaBundle.getSymbolicName());
		eventProperties.put("bundle.version", rsaBundle.getVersion()); //$NON-NLS-1$
		String[] signers = getSignersForBundle(clientBundle);
		if (signers != null && signers.length > 0)
			eventProperties.put("bundle.signer", signers); //$NON-NLS-1$
		Throwable t = event.getException();
		if (t != null)
			eventProperties.put("cause", t); //$NON-NLS-1$
		long serviceId = endpointDescription.getServiceId();
		if (serviceId != 0)
			eventProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
							new Long(serviceId));
		String frameworkUUID = endpointDescription.getFrameworkUUID();
		if (frameworkUUID != null)
			eventProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
							frameworkUUID);
		String endpointId = endpointDescription.getId();
		if (endpointId != null)
			eventProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
							endpointId);
		List<String> interfaces = endpointDescription.getInterfaces();
		if (interfaces != null && interfaces.size() > 0)
			eventProperties.put(org.osgi.framework.Constants.OBJECTCLASS,
					interfaces.toArray(new String[interfaces.size()]));
		List<String> importedConfigs = endpointDescription
				.getConfigurationTypes();
		if (importedConfigs != null && importedConfigs.size() > 0)
			eventProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
							importedConfigs.toArray(new String[importedConfigs
									.size()]));
		eventProperties.put("timestamp", new Long(new Date().getTime())); //$NON-NLS-1$
		eventProperties.put("event", event); //$NON-NLS-1$
		if (registrationTypeName != null) {
			eventProperties.put(registrationTypeName, endpointDescription);
		}
		postRemoteServiceAdminEvent(topic, eventProperties);

	}

	private String[] getSignersForBundle(Bundle bundle) {
		List<String> result = new ArrayList<String>();
		Map signers = bundle.getSignerCertificates(Bundle.SIGNERS_ALL);
		for (Iterator i = signers.keySet().iterator(); i.hasNext();)
			result.add(i.next().toString());
		return (String[]) result.toArray(new String[result.size()]);
	}

	private void publishExportEvent(ExportRegistration exportRegistration) {
		Throwable exception = exportRegistration.getException();
		org.osgi.service.remoteserviceadmin.ExportReference exportReference = (exception == null) ? exportRegistration
				.getExportReference() : null;
		RemoteServiceAdminEvent rsaEvent = new RemoteServiceAdminEvent(
				exportRegistration.getContainerID(),
				(exception == null) ? RemoteServiceAdminEvent.EXPORT_REGISTRATION
						: RemoteServiceAdminEvent.EXPORT_ERROR, getRSABundle(),
				exportReference, exception);
		publishEvent(rsaEvent, exportRegistration.getEndpointDescription());
	}

	private void publishImportEvent(ImportRegistration importRegistration) {
		Throwable exception = importRegistration.getException();
		org.osgi.service.remoteserviceadmin.ImportReference importReference = (exception == null) ? importRegistration
				.getImportReference() : null;
		RemoteServiceAdminEvent rsaEvent = new RemoteServiceAdminEvent(
				importRegistration.getContainerID(),
				(exception == null) ? RemoteServiceAdminEvent.IMPORT_REGISTRATION
						: RemoteServiceAdminEvent.IMPORT_ERROR, getRSABundle(),
				importReference, exception);
		publishEvent(rsaEvent, importRegistration.getEndpointDescription());
	}

	private void closeRemoteServiceAdminListenerTracker() {
		synchronized (remoteServiceAdminListenerTrackerLock) {
			if (remoteServiceAdminListenerTracker != null) {
				remoteServiceAdminListenerTracker.close();
				remoteServiceAdminListenerTracker = null;
			}
		}
	}

	private RemoteServiceAdminListener[] getListeners(EndpointPermission perm) {
		synchronized (remoteServiceAdminListenerTrackerLock) {
			if (remoteServiceAdminListenerTracker == null) {
				remoteServiceAdminListenerTracker = new ServiceTracker(
						getClientBundleContext(),
						RemoteServiceAdminListener.class.getName(), null);
				remoteServiceAdminListenerTracker.open();
			}
		}
		ServiceReference[] unfilteredRefs = remoteServiceAdminListenerTracker
				.getServiceReferences();
		if (unfilteredRefs == null)
			return null;
		// Filter by Bundle.hasPermission
		List<ServiceReference> filteredRefs = new ArrayList<ServiceReference>();
		for (ServiceReference ref : unfilteredRefs)
			if (perm == null || ref.getBundle().hasPermission(perm))
				filteredRefs.add(ref);
		List<RemoteServiceAdminListener> results = new ArrayList<RemoteServiceAdminListener>();
		for (ServiceReference ref : filteredRefs) {
			RemoteServiceAdminListener l = (RemoteServiceAdminListener) remoteServiceAdminListenerTracker
					.getService(ref);
			if (l != null)
				results.add(l);
		}
		return results.toArray(new RemoteServiceAdminListener[results.size()]);
	}

	private ExportEndpoint findExistingExportEndpoint(
			ServiceReference serviceReference, ID containerID) {
		for (ExportRegistration eReg : exportedRegistrations) {
			ExportEndpoint exportEndpoint = eReg.getExportEndpoint(
					serviceReference, containerID);
			if (exportEndpoint != null)
				return exportEndpoint;
		}
		return null;
	}

	private EventAdmin getEventAdmin() {
		synchronized (eventAdminTrackerLock) {
			eventAdminTracker = new ServiceTracker(getClientBundleContext(),
					EventAdmin.class.getName(), null);
			eventAdminTracker.open();
		}
		return (EventAdmin) eventAdminTracker.getService();
	}

	private void postRemoteServiceAdminEvent(String topic,
			Dictionary eventProperties) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin == null) {
			logError("postRemoteServiceAdminEvent", //$NON-NLS-1$
					"No EventAdmin service available to send eventTopic=" //$NON-NLS-1$
							+ topic + " eventProperties=" + eventProperties); //$NON-NLS-1$
			return;
		}
		eventAdmin.postEvent(new Event(topic, eventProperties));
	}

	private void closeEventAdminTracker() {
		synchronized (eventAdminTrackerLock) {
			if (eventAdminTracker != null) {
				eventAdminTracker.close();
				eventAdminTracker = null;
			}
		}
	}

	private void closePackageAdminTracker() {
		synchronized (packageAdminTrackerLock) {
			if (packageAdminTracker != null) {
				packageAdminTracker.close();
				packageAdminTracker = null;
			}
		}
	}

	private Object consumerContainerSelectorTrackerLock = new Object();
	private ServiceTracker consumerContainerSelectorTracker;

	private void closeConsumerContainerSelectorTracker() {
		synchronized (consumerContainerSelectorTrackerLock) {
			if (consumerContainerSelectorTracker != null) {
				consumerContainerSelectorTracker.close();
				consumerContainerSelectorTracker = null;
			}
		}
		if (defaultConsumerContainerSelector != null) {
			defaultConsumerContainerSelector.close();
			defaultConsumerContainerSelector = null;
		}
	}

	private Object hostContainerSelectorTrackerLock = new Object();
	private ServiceTracker hostContainerSelectorTracker;

	private void closeHostContainerSelectorTracker() {
		synchronized (hostContainerSelectorTrackerLock) {
			if (hostContainerSelectorTracker != null) {
				hostContainerSelectorTracker.close();
				hostContainerSelectorTracker = null;
			}
		}
		if (defaultHostContainerSelector != null) {
			defaultHostContainerSelector.close();
			defaultHostContainerSelector = null;
		}
	}

	protected IHostContainerSelector getHostContainerSelector() {
		synchronized (hostContainerSelectorTrackerLock) {
			if (hostContainerSelectorTracker == null) {
				hostContainerSelectorTracker = new ServiceTracker(
						getClientBundleContext(),
						IHostContainerSelector.class.getName(), null);
				hostContainerSelectorTracker.open();
			}
		}
		return (IHostContainerSelector) hostContainerSelectorTracker
				.getService();
	}

	protected IConsumerContainerSelector getConsumerContainerSelector() {
		synchronized (consumerContainerSelectorTrackerLock) {
			if (consumerContainerSelectorTracker == null) {
				consumerContainerSelectorTracker = new ServiceTracker(
						getClientBundleContext(),
						IConsumerContainerSelector.class.getName(), null);
				consumerContainerSelectorTracker.open();
			}
		}
		return (IConsumerContainerSelector) consumerContainerSelectorTracker
				.getService();
	}

	private Version getPackageVersion(ServiceReference serviceReference,
			String serviceInterface, String packageName) {
		Object service = getClientBundleContext().getService(serviceReference);
		if (service == null)
			return null;
		Class[] interfaceClasses = service.getClass().getInterfaces();
		if (interfaceClasses == null)
			return null;
		Class interfaceClass = null;
		for (int i = 0; i < interfaceClasses.length; i++) {
			if (interfaceClasses[i].getName().equals(serviceInterface))
				interfaceClass = interfaceClasses[i];
		}
		if (interfaceClass == null)
			return null;
		ExportedPackage exportedPackage = getExportedPackageForClass(
				getPackageAdmin(), interfaceClass);
		return (exportedPackage == null) ? null : exportedPackage.getVersion();
	}

	private Map<String, Object> createExportEndpointDescriptionProperties(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties,
			String[] exportedInterfaces, String[] serviceIntents,
			IRemoteServiceContainer rsContainer) {
		IContainer container = rsContainer.getContainer();
		ID containerID = container.getID();

		Map<String, Object> endpointDescriptionProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);

		// OSGi properties
		// OBJECTCLASS set to exportedInterfaces
		endpointDescriptionProperties.put(
				org.osgi.framework.Constants.OBJECTCLASS, exportedInterfaces);

		// Service interface versions
		for (int i = 0; i < exportedInterfaces.length; i++) {
			String packageName = getPackageName(exportedInterfaces[i]);
			String packageVersionKey = org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_PACKAGE_VERSION_
					+ packageName;
			// If it's pre-set...by registration or by overridingProperties,
			// then use that value
			String packageVersion = (String) PropertiesUtil.getPropertyValue(
					serviceReference, overridingProperties, packageVersionKey);
			if (packageVersion == null) {
				Version version = getPackageVersion(serviceReference,
						exportedInterfaces[i], packageName);
				if (version != null && !version.equals(Version.emptyVersion))
					packageVersion = version.toString();
			}
			// Only set the package version if we have a non-null value
			if (packageVersion != null)
				endpointDescriptionProperties.put(packageVersionKey,
						packageVersion);
		}

		// ENDPOINT_ID
		String endpointId = (String) PropertiesUtil
				.getPropertyValue(
						serviceReference,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID);
		if (endpointId == null)
			endpointId = containerID.getName();
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
						endpointId);

		// ENDPOINT_SERVICE_ID
		// This is always set to the value from serviceReference as per 122.5.1
		Long serviceId = (Long) serviceReference
				.getProperty(org.osgi.framework.Constants.SERVICE_ID);
		endpointDescriptionProperties.put(
				org.osgi.framework.Constants.SERVICE_ID, serviceId);

		// ENDPOINT_FRAMEWORK_ID
		String frameworkId = (String) PropertiesUtil
				.getPropertyValue(
						serviceReference,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID);
		if (frameworkId == null)
			frameworkId = Activator.getDefault().getFrameworkUUID();
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
						frameworkId);

		// REMOTE_CONFIGS_SUPPORTED
		String[] remoteConfigsSupported = getSupportedConfigs(container);
		if (remoteConfigsSupported != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
							remoteConfigsSupported);
		// SERVICE_IMPORTED_CONFIGS...set to constant value for all ECF
		// providers
		// supported (which is computed
		// for the exporting ECF container
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
						remoteConfigsSupported);

		// SERVICE_INTENTS
		Object intents = PropertiesUtil
				.getPropertyValue(
						null,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS);
		if (intents == null)
			intents = serviceIntents;
		if (intents != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
							intents);

		// REMOTE_INTENTS_SUPPORTED
		String[] remoteIntentsSupported = getSupportedIntents(container);
		if (remoteIntentsSupported != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED,
							remoteIntentsSupported);

		// ECF properties
		// ID namespace
		String idNamespace = containerID.getNamespace().getName();
		endpointDescriptionProperties.put(
				RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE, idNamespace);

		// ENDPOINT_CONNECTTARGET_ID
		String connectTarget = (String) PropertiesUtil.getPropertyValue(
				serviceReference, overridingProperties,
				RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		if (connectTarget == null) {
			ID connectedID = rsContainer.getContainer().getConnectedID();
			if (connectedID != null && !connectedID.equals(containerID))
				connectTarget = connectedID.getName();
		}
		if (connectTarget != null)
			endpointDescriptionProperties.put(
					RemoteConstants.ENDPOINT_CONNECTTARGET_ID, connectTarget);

		// ENDPOINT_IDFILTER_IDS
		String[] idFilter = (String[]) PropertiesUtil.getPropertyValue(
				serviceReference, overridingProperties,
				RemoteConstants.ENDPOINT_IDFILTER_IDS);
		if (idFilter != null && idFilter.length > 0)
			endpointDescriptionProperties.put(
					RemoteConstants.ENDPOINT_IDFILTER_IDS, idFilter);

		// ENDPOINT_REMOTESERVICE_FILTER
		String rsFilter = (String) PropertiesUtil.getPropertyValue(
				serviceReference, overridingProperties,
				RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
		if (rsFilter != null)
			endpointDescriptionProperties.put(
					RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER, rsFilter);

		// Finally, copy all non-reserved properties
		return PropertiesUtil.copyNonReservedProperties(overridingProperties,
				endpointDescriptionProperties);
	}

	private Map<String, Object> copyNonReservedProperties(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties, Map<String, Object> target) {
		// copy all other properties...from service reference
		PropertiesUtil.copyNonReservedProperties(serviceReference, target);
		// And override with overridingProperties
		PropertiesUtil.copyNonReservedProperties(overridingProperties, target);
		return target;
	}

	private ContainerTypeDescription getContainerTypeDescription(
			IContainer container) {
		IContainerManager containerManager = Activator.getDefault()
				.getContainerManager();
		if (containerManager == null)
			return null;
		return containerManager.getContainerTypeDescription(container.getID());
	}

	private String[] getSupportedConfigs(IContainer container) {
		ContainerTypeDescription ctd = getContainerTypeDescription(container);
		return (ctd == null) ? null : ctd.getSupportedConfigs();
	}

	private String[] getImportedConfigs(IContainer container,
			String[] exporterSupportedConfigs) {
		ContainerTypeDescription ctd = getContainerTypeDescription(container);
		return (ctd == null) ? null : ctd
				.getImportedConfigs(exporterSupportedConfigs);
	}

	private String[] getSupportedIntents(IContainer container) {
		ContainerTypeDescription ctd = getContainerTypeDescription(container);
		return (ctd == null) ? null : ctd.getSupportedIntents();
	}

	private ID[] getIDFilter(EndpointDescription endpointDescription,
			ID endpointID) {
		ID[] idFilter = endpointDescription.getIDFilter();
		// If it is null,
		return (idFilter == null) ? new ID[] { endpointID } : idFilter;
	}

	private String getRemoteServiceFilter(
			EndpointDescription endpointDescription) {

		long rsId = 0;
		// if the ECF remote service id is present in properties, allow it to
		// override
		Long l = (Long) endpointDescription.getProperties().get(
				org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		if (l != null)
			rsId = l.longValue();
		// if rsId is still zero, use the endpoint.service.id from
		// endpoint description
		if (rsId == 0)
			rsId = endpointDescription.getServiceId();
		// If it's *still* zero, then just use the raw filter
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

	private ImportEndpoint createAndRegisterProxy(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer,
			IRemoteServiceReference selectedRsReference) throws Exception {

		BundleContext proxyServiceFactoryContext = getProxyServiceFactoryContext(endpointDescription);
		if (proxyServiceFactoryContext == null)
			throw new NullPointerException(
					"getProxyServiceFactoryContext returned null.  Cannot register proxy service factory"); //$NON-NLS-1$

		IRemoteServiceContainerAdapter containerAdapter = rsContainer
				.getContainerAdapter();
		ID rsContainerID = rsContainer.getContainer().getID();
		// First get IRemoteService for selectedRsReference
		IRemoteService rs = containerAdapter
				.getRemoteService(selectedRsReference);
		if (rs == null)
			throw new NullPointerException(
					"getRemoteService returned null for selectedRsReference=" //$NON-NLS-1$
							+ selectedRsReference + ",rsContainerID=" //$NON-NLS-1$
							+ rsContainerID);

		Map proxyProperties = createProxyProperties(endpointDescription,
				rsContainer, selectedRsReference, rs);

		// sync sref props with endpoint props
		endpointDescription.setPropertiesOverrides(proxyProperties);

		List<String> serviceTypes = endpointDescription.getInterfaces();

		ServiceRegistration proxyRegistration = proxyServiceFactoryContext
				.registerService((String[]) serviceTypes
						.toArray(new String[serviceTypes.size()]),
						createProxyServiceFactory(endpointDescription, rs),
						(Dictionary) PropertiesUtil
								.createDictionaryFromMap(proxyProperties));

		return new ImportEndpoint(containerAdapter, selectedRsReference,
				new RemoteServiceListener(), proxyRegistration,
				endpointDescription);
	}

	private BundleContext getProxyServiceFactoryContext(
			EndpointDescription endpointDescription) throws Exception {
		Activator a = Activator.getDefault();
		if (a == null)
			throw new NullPointerException(
					"ECF RemoteServiceAdmin Activator cannot be null."); //$NON-NLS-1$
		if (a.isOldEquinox()) {
			PackageAdmin packageAdmin = getPackageAdmin();
			if (packageAdmin == null)
				throw new NullPointerException(
						"PackageAdmin cannot be accessed by ECF RemoteServiceAdmin"); //$NON-NLS-1$
			// In this case, we get the Bundle that exposes the first service
			// interface class
			BundleContext rsaContext = Activator.getContext();
			if (rsaContext == null)
				throw new NullPointerException(
						"RSA BundleContext cannot be null"); //$NON-NLS-1$
			List<String> interfaces = endpointDescription.getInterfaces();
			Collection<Class> serviceInterfaceClasses = loadServiceInterfacesViaBundle(
					rsaContext.getBundle(),
					interfaces.toArray(new String[interfaces.size()]));
			if (serviceInterfaceClasses.size() == 0)
				throw new NullPointerException(
						"No interface classes loadable for endpointDescription=" //$NON-NLS-1$
								+ endpointDescription);
			// Get the bundle responsible for the first service interface class
			Class serviceInterfaceClass = serviceInterfaceClasses.iterator()
					.next();
			Bundle bundle = packageAdmin.getBundle(serviceInterfaceClass);
			if (bundle == null)
				throw new BundleException("Bundle for service interface class=" //$NON-NLS-1$
						+ serviceInterfaceClass.getName() + " cannot be found"); //$NON-NLS-1$
			int bundleState = bundle.getState();
			BundleContext bundleContext = bundle.getBundleContext();
			if (bundleContext == null)
				throw new BundleException("Bundle=" + bundle.getSymbolicName() //$NON-NLS-1$
						+ " in wrong state (" + bundleState //$NON-NLS-1$
						+ ") for using BundleContext proxy service factory"); //$NON-NLS-1$
			return bundleContext;
		}
		return a.getProxyServiceFactoryBundleContext();
	}

	private ServiceFactory createProxyServiceFactory(
			EndpointDescription endpointDescription,
			IRemoteService remoteService) {
		return new ProxyServiceFactory(
				endpointDescription.getInterfaceVersions(), remoteService);
	}

	private Collection<Class> loadServiceInterfacesViaBundle(Bundle bundle,
			String[] interfaces) {
		List<Class> result = new ArrayList<Class>();
		for (int i = 0; i < interfaces.length; i++) {
			try {
				result.add(bundle.loadClass(interfaces[i]));
			} catch (ClassNotFoundException e) {
				logError("loadInterfacesViaBundle", "interface=" //$NON-NLS-1$ //$NON-NLS-2$
						+ interfaces[i] + " cannot be loaded by clientBundle=" //$NON-NLS-1$
						+ bundle.getSymbolicName(), e);
				continue;
			} catch (IllegalStateException e) {
				logError(
						"loadInterfacesViaBundle", //$NON-NLS-1$
						"interface=" //$NON-NLS-1$
								+ interfaces[i]
								+ " cannot be loaded since clientBundle is in illegal state", //$NON-NLS-1$
						e);
				continue;
			}
		}
		return result;
	}

	class ProxyServiceFactory implements ServiceFactory {
		private IRemoteService remoteService;
		private Map<String, Version> interfaceVersions;

		public ProxyServiceFactory(Map<String, Version> interfaceVersions,
				IRemoteService remoteService) {
			this.interfaceVersions = interfaceVersions;
			this.remoteService = remoteService;
		}

		public Object getService(Bundle bundle, ServiceRegistration registration) {
			return createProxy(bundle, registration.getReference(),
					remoteService, interfaceVersions);
		}

		public void ungetService(Bundle bundle,
				ServiceRegistration registration, Object service) {
			ungetProxyClassLoader(bundle);
		}
	}

	private Object createProxy(Bundle requestingBundle,
			ServiceReference serviceReference, IRemoteService remoteService,
			Map<String, Version> interfaceVersions) {
		// Get symbolicName once for possible use below
		String bundleSymbolicName = requestingBundle.getSymbolicName();
		// Get String[] via OBJECTCLASS constant property
		String[] serviceClassnames = (String[]) serviceReference
				.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
		// Load as many of the serviceInterface classes as possible
		Collection<Class> serviceInterfaceClasses = loadServiceInterfacesViaBundle(
				requestingBundle, serviceClassnames);
		// There has to be at least one serviceInterface that the clientBundle
		// can
		// load...otherwise the service can't be accessed
		if (serviceInterfaceClasses.size() < 1)
			throw new RuntimeException(
					"ProxyServiceFactory cannot load any serviceInterfaces=" //$NON-NLS-1$
							+ serviceInterfaceClasses
							+ " for serviceReference=" + serviceReference //$NON-NLS-1$
							+ " via clientBundle=" + bundleSymbolicName); //$NON-NLS-1$

		// Now verify that the classes are of valid versions
		if (!verifyServiceInterfaceVersionsForProxy(requestingBundle,
				serviceInterfaceClasses, interfaceVersions))
			return null;

		// Now create/get class loader for proxy. This will typically
		// be an instance of ProxyClassLoader
		ClassLoader cl = getProxyClassLoader(requestingBundle);
		try {
			return remoteService.getProxy(cl, (Class[]) serviceInterfaceClasses
					.toArray(new Class[serviceInterfaceClasses.size()]));
		} catch (ECFException e) {
			throw new ServiceException(
					"ProxyServiceFactory cannot create proxy for clientBundle=" //$NON-NLS-1$
							+ bundleSymbolicName + " from serviceReference=" //$NON-NLS-1$
							+ serviceReference, e);
		}

	}

	private Map<Bundle, ProxyClassLoader> proxyClassLoaders = new HashMap<Bundle, ProxyClassLoader>();

	private void closeProxyClassLoaderCache() {
		synchronized (proxyClassLoaders) {
			proxyClassLoaders.clear();
		}
	}

	private ClassLoader getProxyClassLoader(Bundle bundle) {
		ProxyClassLoader proxyClassLoaderForBundle = null;
		synchronized (proxyClassLoaders) {
			proxyClassLoaderForBundle = proxyClassLoaders.get(bundle);
			if (proxyClassLoaderForBundle == null) {
				proxyClassLoaderForBundle = new ProxyClassLoader(bundle);
				proxyClassLoaders.put(bundle, proxyClassLoaderForBundle);
			} else
				proxyClassLoaderForBundle.addServiceUseCount();
		}
		return proxyClassLoaderForBundle;
	}

	private void ungetProxyClassLoader(Bundle bundle) {
		synchronized (proxyClassLoaders) {
			ProxyClassLoader proxyClassLoaderForBundle = proxyClassLoaders
					.get(bundle);
			if (proxyClassLoaderForBundle != null) {
				int useCount = proxyClassLoaderForBundle.getServiceUseCount();
				if (useCount == 0)
					proxyClassLoaders.remove(bundle);
				else
					proxyClassLoaderForBundle.removeServiceUseCount();
			}
		}
	}

	protected class ProxyClassLoader extends ClassLoader {
		private Bundle loadingBundle;
		private int serviceUseCount = 0;

		public ProxyClassLoader(Bundle loadingBundle) {
			this.loadingBundle = loadingBundle;
		}

		public Class loadClass(String name) throws ClassNotFoundException {
			return loadingBundle.loadClass(name);
		}

		public int getServiceUseCount() {
			return serviceUseCount;
		}

		public void addServiceUseCount() {
			serviceUseCount++;
		}

		public void removeServiceUseCount() {
			serviceUseCount--;
		}
	}

	private PackageAdmin getPackageAdmin() {
		synchronized (packageAdminTrackerLock) {
			if (packageAdminTracker == null) {
				packageAdminTracker = new ServiceTracker(
						getClientBundleContext(), PackageAdmin.class.getName(),
						null);
				packageAdminTracker.open();
			}
		}
		return (PackageAdmin) packageAdminTracker.getService();
	}

	private ExportedPackage getExportedPackageForClass(
			PackageAdmin packageAdmin, Class clazz) {
		String packageName = getPackageName(clazz.getName());
		// Get all exported packages with given package name
		ExportedPackage[] exportedPackagesWithName = packageAdmin
				.getExportedPackages(packageName);
		// If none then we return null
		if (exportedPackagesWithName == null)
			return null;
		// Get the clientBundle for the previously loaded interface class
		Bundle classBundle = packageAdmin.getBundle(clazz);
		if (classBundle == null)
			return null;
		for (int i = 0; i < exportedPackagesWithName.length; i++) {
			Bundle packageBundle = exportedPackagesWithName[i]
					.getExportingBundle();
			if (packageBundle == null)
				continue;
			if (packageBundle.equals(classBundle))
				return exportedPackagesWithName[i];
		}
		return null;
	}

	private String getPackageName(String className) {
		int lastDotIndex = className.lastIndexOf("."); //$NON-NLS-1$
		if (lastDotIndex == -1)
			return ""; //$NON-NLS-1$
		return className.substring(0, lastDotIndex);
	}

	private boolean comparePackageVersions(String packageName,
			Version remoteVersion, Version localVersion)
			throws RuntimeException {

		if (remoteVersion == null)
			throw new NullPointerException("Remote package=" + packageName //$NON-NLS-1$
					+ " has no Version"); //$NON-NLS-1$
		if (localVersion == null)
			throw new NullPointerException("Local package=" + packageName //$NON-NLS-1$
					+ " has no Version"); //$NON-NLS-1$

		LogUtility.trace(
				"comparePackageVersions", //$NON-NLS-1$
				DebugOptions.PACKAGE_VERSION_COMPARATOR, this.getClass(),
				"packageName=" + packageName + ",remoteVersion=" //$NON-NLS-1$ //$NON-NLS-2$
						+ remoteVersion + ",localVersion=" + localVersion); //$NON-NLS-1$
		// By default we do strict comparison of remote with local...they must
		// be exactly the same, or we thrown a runtime exception
		int compareResult = localVersion.compareTo(remoteVersion);
		// Now check compare result, and throw exception to fail compare
		return (compareResult != 0);
	}

	private boolean verifyServiceInterfaceVersionsForProxy(Bundle bundle,
			Collection<Class> classes, Map<String, Version> interfaceVersions) {
		// For all service interface classes
		boolean result = true;
		for (Class clazz : classes) {
			String className = clazz.getName();
			String packageName = getPackageName(className);
			ExportedPackage exportedPackage = getExportedPackageForClass(
					getPackageAdmin(), clazz);
			if (exportedPackage == null)
				throw new NullPointerException(
						"No exported package found for class=" + className); //$NON-NLS-1$
			// Now do compare via package version comparator service
			Version remoteVersion = interfaceVersions.get(className);
			Version localVersion = exportedPackage.getVersion();
			if (comparePackageVersions(packageName, remoteVersion, localVersion)) {
				logError("verifyServiceInterfaceVersionsForProxy", //$NON-NLS-1$
						"Failed version check for proxy creation.  clientBundle=" //$NON-NLS-1$
								+ clientBundle + " interfaceType=" + className //$NON-NLS-1$
								+ " remoteVersion=" + remoteVersion //$NON-NLS-1$
								+ " localVersion=" + localVersion); //$NON-NLS-1$
				result = false;
			}
		}
		return result;
	}

	private IRemoteServiceReference selectRemoteServiceReference(
			Collection<IRemoteServiceReference> rsRefs, ID targetID,
			ID[] idFilter, Collection<String> interfaces, String rsFilter,
			IRemoteServiceContainer rsContainer) {
		if (rsRefs.size() == 0)
			return null;
		if (rsRefs.size() > 1) {
			logWarning("selectRemoteServiceReference", "rsRefs=" + rsRefs //$NON-NLS-1$ //$NON-NLS-2$
					+ ",targetID=" + targetID + ",idFilter=" + idFilter //$NON-NLS-1$ //$NON-NLS-2$
					+ ",interfaces=" + interfaces + ",rsFilter=" + rsFilter //$NON-NLS-1$ //$NON-NLS-2$
					+ ",rsContainer=" + rsContainer.getContainer().getID() //$NON-NLS-1$
					+ " has " + rsRefs.size() //$NON-NLS-1$
					+ " values.  Selecting the first element"); //$NON-NLS-1$
		}
		return rsRefs.iterator().next();
	}

	private Map createProxyProperties(EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer,
			IRemoteServiceReference rsReference, IRemoteService remoteService) {

		Map resultProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);
		PropertiesUtil.copyNonReservedProperties(rsReference, resultProperties);
		// remove OBJECTCLASS
		resultProperties
				.remove(org.eclipse.ecf.remoteservice.Constants.OBJECTCLASS);
		// remove remote service id
		resultProperties
				.remove(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		// Set intents if there are intents
		Object intentsValue = PropertiesUtil
				.convertToStringPlusValue(endpointDescription.getIntents());
		if (intentsValue != null)
			resultProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
							intentsValue);

		// Set service.imported to IRemoteService unless
		// SERVICE_IMPORTED_VALUETYPE is
		// set
		String serviceImportedType = (String) endpointDescription
				.getProperties()
				.get(org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_VALUETYPE);
		if (serviceImportedType == null
				|| serviceImportedType.equals(IRemoteService.class.getName()))
			resultProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED,
							remoteService);
		else
			resultProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED,
							new Boolean(true));

		String[] exporterSupportedConfigs = (String[]) endpointDescription
				.getProperties()
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
		String[] importedConfigs = getImportedConfigs(
				rsContainer.getContainer(), exporterSupportedConfigs);
		// Set service.imported.configs
		resultProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
						importedConfigs);

		// Set endpoint.id and endpoint.service.id
		resultProperties
				.put(org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_ENDPOINT_ID,
						endpointDescription
								.getProperties()
								.get(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID));
		resultProperties
				.put(org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_ENDPOINT_SERVICE_ID,
						endpointDescription
								.getProperties()
								.get(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID));

		return resultProperties;
	}

	private ExportRegistration exportService(ServiceReference serviceReference,
			Map<String, Object> overridingProperties,
			String[] exportedInterfaces, IRemoteServiceContainer rsContainer,
			Map<String, Object> endpointDescriptionProperties) throws Exception {

		// Create remote service properties
		Map remoteServiceProperties = copyNonReservedProperties(
				serviceReference, overridingProperties,
				new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER));

		IRemoteServiceContainerAdapter containerAdapter = rsContainer
				.getContainerAdapter();

		// Register remote service via ECF container adapter to create
		// remote service registration
		IRemoteServiceRegistration remoteRegistration = null;
		if (containerAdapter instanceof IOSGiRemoteServiceContainerAdapter) {
			IOSGiRemoteServiceContainerAdapter osgiContainerAdapter = (IOSGiRemoteServiceContainerAdapter) containerAdapter;
			remoteRegistration = osgiContainerAdapter.registerRemoteService(
					exportedInterfaces, serviceReference, PropertiesUtil
							.createDictionaryFromMap(remoteServiceProperties));
		} else
			remoteRegistration = containerAdapter.registerRemoteService(
					exportedInterfaces,
					getClientBundleContext().getService(serviceReference),
					PropertiesUtil
							.createDictionaryFromMap(remoteServiceProperties));
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
						remoteRegistration
								.getProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID));
		EndpointDescription endpointDescription = new EndpointDescription(
				serviceReference, endpointDescriptionProperties);
		// Create ExportEndpoint/ExportRegistration
		return new ExportRegistration(new ExportEndpoint(serviceReference,
				endpointDescription, remoteRegistration));
	}

	private ImportRegistration importService(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer) {
		trace("doImportService", "endpointDescription=" + endpointDescription //$NON-NLS-1$ //$NON-NLS-2$
				+ ",rsContainerID=" + rsContainer.getContainer().getID()); //$NON-NLS-1$
		// Get interfaces from endpoint description
		Collection<String> interfaces = endpointDescription.getInterfaces();
		Assert.isNotNull(interfaces);
		Assert.isTrue(interfaces.size() > 0);
		// Get ECF endpoint ID...if this throws IDCreateException (because the
		// local system does not have
		// namespace for creating ID, or no namespace is present in
		// endpointDescription or endpoint id,
		// then it will be caught by the caller
		ID endpointContainerID = endpointDescription.getContainerID();
		Assert.isNotNull(endpointContainerID);
		// Get connect target ID. May be null
		ID targetID = endpointDescription.getConnectTargetID();
		if (targetID == null)
			targetID = endpointContainerID;
		// Get idFilter...also may be null
		ID[] idFilter = getIDFilter(endpointDescription, endpointContainerID);
		// Get remote service filter
		String rsFilter = getRemoteServiceFilter(endpointDescription);
		// IRemoteServiceReferences from query
		Collection<IRemoteServiceReference> rsRefs = new ArrayList<IRemoteServiceReference>();
		// Get IRemoteServiceContainerAdapter
		IRemoteServiceContainerAdapter containerAdapter = rsContainer
				.getContainerAdapter();
		// rsContainerID
		ID rsContainerID = rsContainer.getContainer().getID();
		try {
			// Get first interface name for service reference
			// lookup
			String intf = interfaces.iterator().next();
			// Get/lookup remote service references
			IRemoteServiceReference[] refs = containerAdapter
					.getRemoteServiceReferences(targetID, idFilter, intf,
							rsFilter);
			if (refs == null) {
				logWarning("doImportService", //$NON-NLS-1$
						"getRemoteServiceReferences return null for targetID=" //$NON-NLS-1$
								+ targetID + ",idFilter=" + idFilter //$NON-NLS-1$
								+ ",intf=" + intf + ",rsFilter=" + rsFilter //$NON-NLS-1$ //$NON-NLS-2$
								+ " on rsContainerID=" + rsContainerID); //$NON-NLS-1$
			} else
				for (int i = 0; i < refs.length; i++)
					rsRefs.add(refs[i]);
			// If there are several refs resulting (should not be)
			// we select the one to use
			IRemoteServiceReference selectedRsReference = selectRemoteServiceReference(
					rsRefs, targetID, idFilter, interfaces, rsFilter,
					rsContainer);
			// If none found, we obviously can't continue
			if (selectedRsReference == null)
				throw new RemoteReferenceNotFoundException(targetID, idFilter,
						interfaces, rsFilter);

			return new ImportRegistration(createAndRegisterProxy(
					endpointDescription, rsContainer, selectedRsReference));
		} catch (Exception e) {
			logError(
					"importService", "selectRemoteServiceReference returned null for rsRefs=" //$NON-NLS-1$ //$NON-NLS-2$
							+ rsRefs + ",targetID=" + targetID //$NON-NLS-1$
							+ ",idFilter=" + idFilter + ",interfaces=" //$NON-NLS-1$ //$NON-NLS-2$
							+ interfaces + ",rsFilter=" + rsFilter //$NON-NLS-1$
							+ ",rsContainerID=" + rsContainerID, e); //$NON-NLS-1$
			return new ImportRegistration(endpointDescription, e);
		}
	}

	private void closeExportRegistrations() {
		List<ExportRegistration> toClose = null;
		synchronized (exportedRegistrations) {
			toClose = new ArrayList<ExportRegistration>(exportedRegistrations);
			exportedRegistrations.clear();
		}
		for (ExportRegistration reg : toClose)
			reg.close();
	}

	private void closeImportRegistrations() {
		List<ImportRegistration> toClose = null;
		synchronized (importedRegistrations) {
			toClose = new ArrayList<ImportRegistration>(importedRegistrations);
			importedRegistrations.clear();
		}
		for (ImportRegistration reg : toClose)
			reg.close();
	}

	public void close() {
		trace("close", "closing importedRegistrations=" + importedRegistrations //$NON-NLS-1$ //$NON-NLS-2$
				+ " exportedRegistrations=" + exportedRegistrations); //$NON-NLS-1$
		closeRemoteServiceAdminListenerTracker();
		closeEventAdminTracker();
		closePackageAdminTracker();
		closeProxyClassLoaderCache();
		closeConsumerContainerSelectorTracker();
		closeHostContainerSelectorTracker();
		closeDefaultContainerSelectors();
		closeImportRegistrations();
		closeExportRegistrations();
		this.clientBundle = null;
	}

	private ImportEndpoint findImportEndpoint(EndpointDescription ed) {
		for (ImportRegistration reg : importedRegistrations) {
			ImportEndpoint endpoint = reg.getImportEndpoint(ed);
			if (endpoint != null)
				return endpoint;
		}
		return null;
	}

	private void unimportService(IRemoteServiceID remoteServiceID) {
		List<ImportRegistration> removedRegistrations = new ArrayList<ImportRegistration>();
		synchronized (importedRegistrations) {
			for (Iterator<ImportRegistration> i = importedRegistrations
					.iterator(); i.hasNext();) {
				ImportRegistration importRegistration = i.next();
				if (importRegistration != null
						&& importRegistration.match(remoteServiceID))
					removedRegistrations.add(importRegistration);
			}
		}
		// Now close all of them
		for (ImportRegistration removedReg : removedRegistrations) {
			trace("unimportService", "closing importRegistration=" + removedReg); //$NON-NLS-1$ //$NON-NLS-2$
			removedReg.close();
		}
	}

	class RemoteServiceListener implements IRemoteServiceListener {
		public void handleServiceEvent(IRemoteServiceEvent event) {
			if (event instanceof IRemoteServiceUnregisteredEvent)
				unimportService(event.getReference().getID());
		}
	}

	private void trace(String methodName, String message) {
		LogUtility.trace(methodName, DebugOptions.REMOTE_SERVICE_ADMIN,
				this.getClass(), message);
	}

	private void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.REMOTE_SERVICE_ADMIN,
				this.getClass(), message);
	}

	private void logError(String methodName, String message, Throwable t) {
		LogUtility.logError(methodName, DebugOptions.REMOTE_SERVICE_ADMIN,
				this.getClass(), message, t);
	}

	private void logError(String methodName, String message) {
		logError(methodName, message, (Throwable) null);
	}

	public class RemoteServiceAdminEvent extends
			org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent {

		private ID containerID;

		public RemoteServiceAdminEvent(
				ID containerID,
				int type,
				Bundle source,
				org.osgi.service.remoteserviceadmin.ExportReference exportReference,
				Throwable exception) {
			super(type, source, exportReference, exception);
			this.containerID = containerID;
		}

		public RemoteServiceAdminEvent(
				ID containerID,
				int type,
				Bundle source,
				org.osgi.service.remoteserviceadmin.ImportReference importReference,
				Throwable exception) {
			super(type, source, importReference, exception);
			this.containerID = containerID;
		}

		public ID getContainerID() {
			return containerID;
		}

		public String toString() {
			return "RemoteServiceAdminEvent[containerID=" + containerID //$NON-NLS-1$
					+ ", getType()=" + getType() + ", getSource()=" + getSource() //$NON-NLS-1$ //$NON-NLS-2$
					+ ", getException()=" + getException() //$NON-NLS-1$
					+ ", getImportReference()=" + getImportReference() //$NON-NLS-1$
					+ ", getExportReference()=" + getExportReference() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

}
