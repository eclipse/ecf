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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceUnregisteredEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RemoteServiceAdmin extends AbstractRemoteServiceAdmin implements
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin {

	private Collection<ExportRegistration> exportedRegistrations = new ArrayList<ExportRegistration>();

	private Collection<ImportRegistration> importedRegistrations = new ArrayList<ImportRegistration>();

	public RemoteServiceAdmin(BundleContext context) {
		super(context);
	}

	public Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> exportService(
			ServiceReference serviceReference, Map<String, Object> properties) {
		trace("exportService", "serviceReference=" + serviceReference
				+ ",properties=" + properties);
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
					rsRegistration = new ExportRegistration(serviceReference,e);
				}
				results.add(rsRegistration);
				exportedRegistrations.add(rsRegistration);
			}
		}
		return results;
	}

	public org.osgi.service.remoteserviceadmin.ImportRegistration importService(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		trace("importService", "endpointDescription=" + endpointDescription);
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
					logError("importService",
							"Exception importing endpointDescription=" + ed
									+ rsContainer.getContainer().getID(), e);
					importRegistration = new ImportRegistration(rsContainer, e);
				} catch (NoClassDefFoundError e) {
					logError("importService",
							"NoClassDefFoundError importing endpointDescription="
									+ ed + rsContainer.getContainer().getID(),
							e);
					importRegistration = new ImportRegistration(rsContainer, e);
				}
				// If we actually created an importRegistration...whether
				// successful or not, add it to the
				// set of imported registrations
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

	public Collection<ImportRegistration> unimportService(
			IRemoteServiceID remoteServiceID) {
		trace("unimport", "remoteServiceID=" + remoteServiceID);
		List<ImportRegistration> removedRegistrations = new ArrayList<ImportRegistration>();
		synchronized (importedRegistrations) {
			for (Iterator<ImportRegistration> i = importedRegistrations
					.iterator(); i.hasNext();) {
				ImportRegistration importRegistration = i.next();
				IRemoteServiceReference rsReference = importRegistration
						.getRemoteServiceReference();
				if (rsReference != null) {
					IRemoteServiceID regID = rsReference.getID();
					if (regID.equals(remoteServiceID)) {
						removedRegistrations.add(importRegistration);
						i.remove();
					}
				}
			}
		}
		// Now close all of them
		for (ImportRegistration removedReg : removedRegistrations)
			removedReg.close();
		return removedRegistrations;
	}

	protected IRemoteServiceListener createRemoteServiceListener() {
		return new RemoteServiceListener();
	}

	class RemoteServiceListener implements IRemoteServiceListener {

		public void handleServiceEvent(IRemoteServiceEvent event) {
			if (event instanceof IRemoteServiceUnregisteredEvent) {
				Collection<ImportRegistration> removedRegistrations = unimportService(event
						.getReference().getID());
				trace("RemoteServiceListener.handleServiceEvent",
						"Removed importRegistrations=" + removedRegistrations
								+ " via event=" + event);
			}
		}
	}

	public Collection<ImportRegistration> unimportService(
			EndpointDescription endpointDescription) {
		trace("unimportService", "endpointDescription=" + endpointDescription);
		List<ImportRegistration> removedRegistrations = new ArrayList<ImportRegistration>();
		synchronized (importedRegistrations) {
			for (Iterator<ImportRegistration> i = importedRegistrations
					.iterator(); i.hasNext();) {
				ImportRegistration reg = i.next();
				ImportReference importReference = null;
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
					// Import Registration not properly initialized
				}
			}
		}
		// Now close all of them
		for (ImportRegistration removedReg : removedRegistrations)
			removedReg.close();
		return removedRegistrations;
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
						}
					} catch (IllegalStateException e) {
						// no export ref because ExportRegistration not
						// initialized properly
					}
					// close and remove the export registration no matter what
					exportRegs[i].close();
					exportedRegistrations.remove(exportRegs[i]);
				}
			}
		}
		return endpointDescriptions
				.toArray(new EndpointDescription[endpointDescriptions.size()]);
	}
}
