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
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.remoteserviceadmin.EndpointListener;

public class TopologyManager extends AbstractTopologyManager implements
		EventHook, EndpointListener {

	private ServiceRegistration endpointListenerRegistration;

	private ServiceRegistration eventHookRegistration;

	protected Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> exportedRegistrations = new ArrayList<org.osgi.service.remoteserviceadmin.ExportRegistration>();
	protected Collection<org.osgi.service.remoteserviceadmin.ImportRegistration> importedRegistrations = new ArrayList<org.osgi.service.remoteserviceadmin.ImportRegistration>();

	public TopologyManager(BundleContext context) {
		super(context);
	}

	public void start() throws Exception {
		// Register as EndpointListener, so that it gets notified when Endpoints
		// are discovered
		Properties props = new Properties();
		props.put(
				org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE,
				"("
						+ org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID
						+ "=*)");
		endpointListenerRegistration = getContext().registerService(
				EndpointListener.class.getName(), this, (Dictionary) props);

		// Register as EventHook, so that we get notified when remote services
		// are registered
		eventHookRegistration = getContext().registerService(
				EventHook.class.getName(), this, null);
	}

	public void close() {
		if (eventHookRegistration != null) {
			eventHookRegistration.unregister();
			eventHookRegistration = null;
		}
		if (endpointListenerRegistration != null) {
			endpointListenerRegistration.unregister();
			endpointListenerRegistration = null;
		}
		super.close();
	}

	public void endpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (endpoint instanceof EndpointDescription) {
			handleEndpointAdded((EndpointDescription) endpoint);
		} else
			logWarning("endpointAdded",
					"ECF Topology Manager:  Ignoring Non-ECF endpointAdded="
							+ endpoint + ",matchedFilter=" + matchedFilter);
	}

	public void endpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (endpoint instanceof EndpointDescription) {
			handleEndpointRemoved((EndpointDescription) endpoint);
		} else
			logWarning("endpointRemoved",
					"ECF Topology Manager:  Ignoring Non-ECF endpointRemoved="
							+ endpoint + ",matchedFilter=" + matchedFilter);
	}

	private void handleEndpointAdded(EndpointDescription endpointDescription) {
		trace("handleEndpointAdded", "endpointDescription="
				+ endpointDescription);
		// First, select importing remote service admin
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa = getRemoteServiceAdmin();

		if (rsa == null) {
			logError("handleEndpointAdded",
					"RemoteServiceAdmin not found for importing endpointDescription="
							+ endpointDescription);
			return;
		}
		// now call rsa.import
		synchronized (importedRegistrations) {
			org.osgi.service.remoteserviceadmin.ImportRegistration importRegistration = rsa
					.importService(endpointDescription);
			if (importRegistration == null) {
				logError("handleEndpointAdded",
						"Import registration is null for endpointDescription="
								+ endpointDescription + " and rsa=" + rsa);
			} else
				importedRegistrations.add(importRegistration);
		}

	}

	private void handleEndpointRemoved(EndpointDescription endpointDescription) {
		trace("handleEndpointRemoved", "endpointDescription="
				+ endpointDescription);
		// First, select importing remote service admin
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa = getRemoteServiceAdmin();
		if (rsa == null) {
			logError("handleEndpointRemoved",
					"RemoteServiceAdmin not found for importing endpointDescription="
							+ endpointDescription);
			return;
		}
		Collection<RemoteServiceAdmin.ImportRegistration> unimportRegistrations = unimportService(endpointDescription);
		trace("handleEndpointRemoved", "importRegistration="
				+ unimportRegistrations + " removed for endpointDescription="
				+ endpointDescription);
	}

	public void event(ServiceEvent event, Collection contexts) {
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

	private void handleServiceRegistering(ServiceReference serviceReference) {
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
			logError("handleServiceRegistered",
					"No RemoteServiceAdmin found for serviceReference="
							+ serviceReference
							+ ".  Remote service NOT EXPORTED");
			return;
		}

		// prepare export properties
		Map<String, Object> exportProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);
		exportProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
						exportedInterfaces);
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> registrations = null;
		synchronized (exportedRegistrations) {
			// Export the remote service using the selected remote service admin
			registrations = rsa.exportService(serviceReference,
					exportProperties);

			if (registrations.size() == 0) {
				logError("handleServiceRegistered",
						"No export registrations created by RemoteServiceAdmin="
								+ rsa + ".  ServiceReference="
								+ serviceReference + " NOT EXPORTED");
				return;
			}
			// add them all
			exportedRegistrations.addAll(registrations);
		}
		// publish exported registrations
		for (org.osgi.service.remoteserviceadmin.ExportRegistration reg : registrations) {
			advertiseEndpointDescription((EndpointDescription) reg
					.getExportReference().getExportedEndpoint());
		}

	}

	private void handleServiceModifying(ServiceReference serviceReference) {
		handleServiceUnregistering(serviceReference);
		handleServiceRegistering(serviceReference);
	}

	private void handleServiceUnregistering(ServiceReference serviceReference) {
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa = getRemoteServiceAdmin();
		if (rsa == null) {
			logError("handleServiceUnregistering",
					"No RemoteServiceAdmin found for serviceReference="
							+ serviceReference
							+ ".  Remote service NOT UNEXPORTED");
			return;
		}
		EndpointDescription[] endpointDescriptions = unexportService(serviceReference);
		if (endpointDescriptions != null) {
			for (int i = 0; i < endpointDescriptions.length; i++) {
				unadvertiseEndpointDescription(endpointDescriptions[i]);
			}
		}
	}

	protected RemoteServiceAdmin.ExportRegistration[] findExportRegistrations(
			ServiceReference serviceReference) {
		List<RemoteServiceAdmin.ExportRegistration> results = new ArrayList<RemoteServiceAdmin.ExportRegistration>();
		for (org.osgi.service.remoteserviceadmin.ExportRegistration reg : exportedRegistrations) {
			RemoteServiceAdmin.ExportRegistration exportReg = (RemoteServiceAdmin.ExportRegistration) reg;
			if (exportReg.match(serviceReference))
				results.add(exportReg);
		}
		return results
				.toArray(new RemoteServiceAdmin.ExportRegistration[results
						.size()]);
	}

	protected EndpointDescription[] unexportService(
			ServiceReference serviceReference) {
		List<EndpointDescription> endpointDescriptions = new ArrayList<EndpointDescription>();
		synchronized (exportedRegistrations) {
			RemoteServiceAdmin.ExportRegistration[] exportRegs = findExportRegistrations(serviceReference);
			if (exportRegs != null) {
				for (int i = 0; i < exportRegs.length; i++) {
					org.osgi.service.remoteserviceadmin.ExportReference exportRef = null;
					try {
						exportRef = exportRegs[i].getExportReference();
						if (exportRef != null) {
							org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription = exportRef
									.getExportedEndpoint();
							if (endpointDescription != null
									&& endpointDescription instanceof EndpointDescription) {
								endpointDescriptions
										.add((EndpointDescription) endpointDescription);
							}
							exportRegs[i].close();
							exportedRegistrations.remove(exportRegs[i]);
						}
					} catch (IllegalStateException e) {
						// no export ref because ExportRegistration not
						// initialized properly
						logWarning("unexportService",
								"IllegalStateException accessing export reference for exportRegistration="
										+ exportRegs[i]);
					}
				}
			}
		}
		return endpointDescriptions
				.toArray(new EndpointDescription[endpointDescriptions.size()]);
	}

	protected Collection<RemoteServiceAdmin.ImportRegistration> unimportService(
			EndpointDescription endpointDescription) {
		trace("unimportService", "endpointDescription=" + endpointDescription);
		List<RemoteServiceAdmin.ImportRegistration> removedRegistrations = new ArrayList<RemoteServiceAdmin.ImportRegistration>();
		synchronized (importedRegistrations) {
			for (Iterator<org.osgi.service.remoteserviceadmin.ImportRegistration> i = importedRegistrations
					.iterator(); i.hasNext();) {
				RemoteServiceAdmin.ImportRegistration reg = (RemoteServiceAdmin.ImportRegistration) i
						.next();
				org.osgi.service.remoteserviceadmin.ImportReference importReference = null;
				try {
					importReference = reg.getImportReference();
					if (importReference != null) {
						org.osgi.service.remoteserviceadmin.EndpointDescription importedDescription = importReference
								.getImportedEndpoint();
						if (importedDescription != null
								&& importedDescription
										.equals(endpointDescription)) {
							removedRegistrations.add(reg);
							i.remove();
						}
					}
				} catch (IllegalStateException e) {
					// no export ref because ExportRegistration not
					// initialized properly
					logWarning("unimportService",
							"IllegalStateException accessing export reference for importRegistration="
									+ reg);
				}
			}
		}
		// Now close all of them
		for (RemoteServiceAdmin.ImportRegistration removedReg : removedRegistrations)
			removedReg.close();
		return removedRegistrations;
	}

}
