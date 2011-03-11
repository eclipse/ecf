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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Abstract superclass for topology managers. This abstract superclass provides
 * basic functionality for topology managers to reuse. New topology managers can
 * extend this class to get or customize desired functionality. Alternatively,
 * they can use this class as a guide to implementing desired topology manager
 * behavior. For description of the role of topology managers see the <a
 * href="http://www.osgi.org/download/r4v42/r4.enterprise.pdf">OSGI 4.2 Remote
 * Service Admin specification (chap 122)</a>.
 * 
 */
public abstract class AbstractTopologyManager {

	public static final String SERVICE_EXPORTED_INTERFACES_WILDCARD = "*"; //$NON-NLS-1$

	private BundleContext context;

	private ServiceTracker endpointDescriptionAdvertiserTracker;
	private Object endpointDescriptionAdvertiserTrackerLock = new Object();

	private ServiceTracker remoteServiceAdminTracker;
	private Object remoteServiceAdminTrackerLock = new Object();

	protected Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> exportedRegistrations = new ArrayList<org.osgi.service.remoteserviceadmin.ExportRegistration>();

	protected Collection<org.osgi.service.remoteserviceadmin.ImportRegistration> importedRegistrations = new ArrayList<org.osgi.service.remoteserviceadmin.ImportRegistration>();

	public AbstractTopologyManager(BundleContext context) {
		this.context = context;
	}

	protected BundleContext getContext() {
		return context;
	}

	protected String getFrameworkUUID() {
		Activator a = Activator.getDefault();
		if (a == null)
			return null;
		return a.getFrameworkUUID();
	}

	protected IEndpointDescriptionAdvertiser getEndpointDescriptionAdvertiser(
			EndpointDescription endpointDescription) {
		synchronized (endpointDescriptionAdvertiserTrackerLock) {
			if (endpointDescriptionAdvertiserTracker == null) {
				endpointDescriptionAdvertiserTracker = new ServiceTracker(
						getContext(),
						IEndpointDescriptionAdvertiser.class.getName(), null);
				endpointDescriptionAdvertiserTracker.open();
			}
		}
		return (IEndpointDescriptionAdvertiser) endpointDescriptionAdvertiserTracker
				.getService();
	}

	public void close() {
		synchronized (endpointDescriptionAdvertiserTrackerLock) {
			if (endpointDescriptionAdvertiserTracker != null) {
				endpointDescriptionAdvertiserTracker.close();
				endpointDescriptionAdvertiserTracker = null;
			}
		}
		synchronized (remoteServiceAdminTrackerLock) {
			if (remoteServiceAdminTracker != null) {
				remoteServiceAdminTracker.close();
				remoteServiceAdminTracker = null;
			}
		}
		synchronized (exportedRegistrations) {
			exportedRegistrations.clear();
		}
		synchronized (importedRegistrations) {
			importedRegistrations.clear();
		}
		context = null;
	}

	protected void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message);
	}

	protected Filter createRSAFilter() {
		String filterString = "(&(" //$NON-NLS-1$
				+ org.osgi.framework.Constants.OBJECTCLASS
				+ "=" //$NON-NLS-1$
				+ org.osgi.service.remoteserviceadmin.RemoteServiceAdmin.class
						.getName()
				+ ")(" //$NON-NLS-1$
				+ org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.SERVICE_PROP
				+ "=*))"; //$NON-NLS-1$
		try {
			return getContext().createFilter(filterString);
		} catch (InvalidSyntaxException e) {
			// Should never happen
			return null;
		}
	}

	protected org.osgi.service.remoteserviceadmin.RemoteServiceAdmin getRemoteServiceAdmin() {
		synchronized (remoteServiceAdminTrackerLock) {
			if (remoteServiceAdminTracker == null) {
				remoteServiceAdminTracker = new ServiceTracker(getContext(),
						createRSAFilter(), null);
				remoteServiceAdminTracker.open();
			}
		}
		return (org.osgi.service.remoteserviceadmin.RemoteServiceAdmin) remoteServiceAdminTracker
				.getService();
	}

	protected void advertiseEndpointDescription(
			EndpointDescription endpointDescription) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser(endpointDescription);
		if (advertiser == null) {
			logWarning("advertiseExportedRegistration", //$NON-NLS-1$
					"No endpoint description advertiser available for endpointDescription=" //$NON-NLS-1$
							+ endpointDescription);
			return;
		}
		// Now advertise endpoint description using endpoint description
		// advertiser
		trace("advertiseEndpointDescription", //$NON-NLS-1$
				"advertising endpointDescription=" + endpointDescription //$NON-NLS-1$
						+ " with advertiser=" + advertiser); //$NON-NLS-1$
		IStatus result = advertiser.advertise(endpointDescription);
		if (!result.isOK())
			logError("advertiseExportedRegistration", //$NON-NLS-1$
					"Advertise of endpointDescription=" + endpointDescription //$NON-NLS-1$
							+ " FAILED", result); //$NON-NLS-1$
	}

	protected void unadvertiseEndpointDescription(
			EndpointDescription endpointDescription) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser(endpointDescription);
		if (advertiser == null) {
			logError(
					"unadvertiseEndpointDescription", //$NON-NLS-1$
					"No endpoint description advertiser available to unadvertise endpointDescription=" //$NON-NLS-1$
							+ endpointDescription);
			return;
		}
		// Now unadvertise endpoint description using endpoint description
		// advertiser
		IStatus result = advertiser.unadvertise(endpointDescription);
		if (!result.isOK())
			logError("unadvertiseEndpointDescription", //$NON-NLS-1$
					"Unadvertise of endpointDescription=" + endpointDescription //$NON-NLS-1$
							+ " FAILED", result); //$NON-NLS-1$
	}

	protected void logError(String methodName, String message,
			Throwable exception) {
		LogUtility.logError(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message, exception);
	}

	protected void logError(String methodName, String message, IStatus result) {
		LogUtility.logError(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), result);
	}

	protected void trace(String methodName, String message) {
		LogUtility.trace(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message);
	}

	protected void logError(String methodName, String message) {
		LogUtility.logError(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message);
	}

	protected void handleEndpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			String matchedFilter) {

		// First, select importing remote service admin
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa = getRemoteServiceAdmin();

		if (rsa == null) {
			logError("handleEndpointAdded", //$NON-NLS-1$
					"RemoteServiceAdmin not found for importing endpointDescription=" //$NON-NLS-1$
							+ endpointDescription);
			return;
		}

		trace("handleEndpointAdded", "endpointDescription=" //$NON-NLS-1$ //$NON-NLS-2$
				+ endpointDescription + " rsa=" + rsa); //$NON-NLS-1$

		// now call rsa.import
		org.osgi.service.remoteserviceadmin.ImportRegistration importRegistration = rsa
				.importService(endpointDescription);

		if (importRegistration == null) {
			logError("handleEndpointAdded", //$NON-NLS-1$
					"Import registration is null for endpointDescription=" //$NON-NLS-1$
							+ endpointDescription + " and rsa=" + rsa); //$NON-NLS-1$
		} else {
			Throwable t = importRegistration.getException();
			if (t != null)
				handleInvalidImportRegistration(importRegistration, t);
			else {
				synchronized (importedRegistrations) {
					importedRegistrations.add(importRegistration);
				}
			}
		}
	}

	protected void handleInvalidImportRegistration(
			ImportRegistration importRegistration, Throwable t) {
		logError("handleInvalidImportRegistration", "importRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
				+ importRegistration, t);
	}

	protected void handleEvent(ServiceEvent event, Collection contexts) {
		switch (event.getType()) {
		case ServiceEvent.MODIFIED:
			handleServiceModifying(event.getServiceReference());
			break;
		case ServiceEvent.MODIFIED_ENDMATCH:
			break;
		case ServiceEvent.REGISTERED:
			handleServiceRegistering(event.getServiceReference());
			break;
		case ServiceEvent.UNREGISTERING:
			handleServiceUnregistering(event.getServiceReference());
			break;
		default:
			break;
		}
	}

	protected void handleEndpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			String matchedFilter) {
		trace("handleEndpointRemoved", "endpointDescription=" //$NON-NLS-1$ //$NON-NLS-2$
				+ endpointDescription);
		unimportService(endpointDescription);
	}

	protected void handleServiceRegistering(ServiceReference serviceReference) {
		// Using OSGI 4.2 Chap 13 Remote Services spec, get the specified remote
		// interfaces for the given service reference
		String[] exportedInterfaces = PropertiesUtil
				.getExportedInterfaces(serviceReference);
		// If no remote interfaces set, then we don't do anything with it
		if (exportedInterfaces == null)
			return;

		// Select remote service admin
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa = getRemoteServiceAdmin();

		// if no remote service admin available, then log error and return
		if (rsa == null) {
			logError("handleServiceRegistered", //$NON-NLS-1$
					"No RemoteServiceAdmin found for serviceReference=" //$NON-NLS-1$
							+ serviceReference
							+ ".  Remote service NOT EXPORTED"); //$NON-NLS-1$
			return;
		}

		// prepare export properties
		Map<String, Object> exportProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);
		exportProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
						exportedInterfaces);
		trace("handleServiceRegistering", "serviceReference=" //$NON-NLS-1$ //$NON-NLS-2$
				+ serviceReference + " exportProperties=" + exportProperties); //$NON-NLS-1$
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> registrations = rsa
				.exportService(serviceReference, exportProperties);

		if (registrations == null || registrations.size() == 0) {
			logError("handleServiceRegistered", //$NON-NLS-1$
					"No export registrations created by RemoteServiceAdmin=" //$NON-NLS-1$
							+ rsa + ".  ServiceReference=" + serviceReference //$NON-NLS-1$
							+ " NOT EXPORTED"); //$NON-NLS-1$
			return;
		}

		List<EndpointDescription> endpointDescriptions = new ArrayList<EndpointDescription>();

		for (org.osgi.service.remoteserviceadmin.ExportRegistration exportRegistration : registrations) {
			// If they are invalid report as such
			Throwable t = exportRegistration.getException();
			if (t != null)
				handleInvalidExportRegistration(exportRegistration, t);
			else {
				endpointDescriptions
						.add((EndpointDescription) exportRegistration
								.getExportReference().getExportedEndpoint());
				synchronized (exportedRegistrations) {
					exportedRegistrations.add(exportRegistration);
				}
			}
		}
		// advertise valid exported registrations
		advertiseEndpointDescriptions(endpointDescriptions);
	}

	protected void advertiseEndpointDescriptions(
			List<EndpointDescription> endpointDescriptions) {
		for (EndpointDescription ed : endpointDescriptions)
			advertiseEndpointDescription(ed);
	}

	protected void handleInvalidExportRegistration(
			ExportRegistration exportRegistration, Throwable t) {
		logError("handleInvalidExportRegistration", "exportRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
				+ exportRegistration, t);
	}

	protected void handleServiceModifying(ServiceReference serviceReference) {
	}

	protected void handleServiceUnregistering(ServiceReference serviceReference) {
		Collection<EndpointDescription> endpointDescriptions = unexportService(serviceReference);
		if (endpointDescriptions != null)
			for (EndpointDescription ed : endpointDescriptions)
				unadvertiseEndpointDescription(ed);
	}

	protected Collection<EndpointDescription> unexportService(
			ServiceReference serviceReference) {
		Map<org.osgi.service.remoteserviceadmin.ExportRegistration, EndpointDescription> matchingExportRegistrations = null;
		synchronized (exportedRegistrations) {
			for (Iterator<org.osgi.service.remoteserviceadmin.ExportRegistration> i = exportedRegistrations
					.iterator(); i.hasNext();) {
				if (matchingExportRegistrations == null)
					matchingExportRegistrations = new HashMap<org.osgi.service.remoteserviceadmin.ExportRegistration, EndpointDescription>();
				org.osgi.service.remoteserviceadmin.ExportRegistration exportRegistration = i
						.next();
				// Only check valid registrations (no exceptions)
				if (exportRegistration.getException() == null) {
					org.osgi.service.remoteserviceadmin.ExportReference exportRef = exportRegistration
							.getExportReference();
					if (exportRef != null) {
						ServiceReference exportReference = exportRef
								.getExportedService();
						if (exportReference != null
								&& serviceReference.equals(exportReference)) {
							matchingExportRegistrations.put(exportRegistration,
									(EndpointDescription) exportRef
											.getExportedEndpoint());
							i.remove();
						}
					}
				}
			}
		}
		// If no matching export registrations then we return null and are done
		if (matchingExportRegistrations == null
				|| matchingExportRegistrations.size() == 0)
			return null;
		// We close all matching export registrations
		for (Iterator<org.osgi.service.remoteserviceadmin.ExportRegistration> i = matchingExportRegistrations
				.keySet().iterator(); i.hasNext();) {
			org.osgi.service.remoteserviceadmin.ExportRegistration exportRegistration = i
					.next();
			trace("unexportService", "closing exportRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
					+ exportRegistration);
			exportRegistration.close();
		}
		// And return endpointDescriptions for matching registrations
		return matchingExportRegistrations.values();
	}

	protected void unimportService(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		List<org.osgi.service.remoteserviceadmin.ImportRegistration> removedRegistrations = null;
		synchronized (importedRegistrations) {
			for (Iterator<org.osgi.service.remoteserviceadmin.ImportRegistration> i = importedRegistrations
					.iterator(); i.hasNext();) {
				if (removedRegistrations == null)
					removedRegistrations = new ArrayList<org.osgi.service.remoteserviceadmin.ImportRegistration>();
				org.osgi.service.remoteserviceadmin.ImportRegistration importRegistration = i
						.next();
				if (importRegistration.getException() == null) {
					org.osgi.service.remoteserviceadmin.ImportReference importRef = importRegistration
							.getImportReference();
					if (importRef != null) {
						org.osgi.service.remoteserviceadmin.EndpointDescription ed = importRef
								.getImportedEndpoint();
						if (ed != null && ed.isSameService(endpointDescription)) {
							removedRegistrations.add(importRegistration);
							i.remove();
						}
					}
				}
			}
		}
		// Now close all of them
		if (removedRegistrations != null)
			for (org.osgi.service.remoteserviceadmin.ImportRegistration removedReg : removedRegistrations) {
				trace("unimportService", "closing importRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
						+ removedReg);
				removedReg.close();
			}
	}

}
