/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceTracker;
import org.eclipse.ecf.osgi.services.discovery.IRemoteServiceEndpointDescription;
import org.eclipse.ecf.osgi.services.discovery.RemoteServiceEndpointDescription;
import org.eclipse.ecf.osgi.services.discovery.RemoteServicePublication;
import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescription;
import org.eclipse.ecf.osgi.services.discovery.ServicePublication;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.osgi.services.distribution.IProxyContainerFinder;
import org.eclipse.ecf.osgi.services.distribution.IProxyDistributionListener;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceUnregisteredEvent;
import org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap;
import org.eclipse.osgi.framework.eventmgr.EventDispatcher;
import org.eclipse.osgi.framework.eventmgr.EventManager;
import org.eclipse.osgi.framework.eventmgr.ListenerQueue;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;

public class DiscoveredServiceTrackerImpl implements DiscoveredServiceTracker {

	private DistributionProviderImpl distributionProvider;
	private List serviceLocations = new ArrayList();
	// <Map<containerID><RemoteServiceRegistration>
	private Map discoveredRemoteServiceRegistrations = new HashMap();
	private List ecfRemoteServiceProperties = Arrays.asList(new String[] {
			Constants.SERVICE_ID, Constants.OBJECTCLASS,
			org.eclipse.ecf.remoteservice.Constants.SERVICE_ID,
			org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_ID,
			org.eclipse.ecf.remoteservice.Constants.SERVICE_RANKING,
			IDistributionConstants.SERVICE_EXPORTED_CONFIGS,
			RemoteServicePublication.ENDPOINT_ID,
			RemoteServicePublication.ENDPOINT_INTERFACE_NAME,
			RemoteServicePublication.ENDPOINT_LOCATION,
			RemoteServicePublication.SERVICE_INTERFACE_NAME,
			RemoteServicePublication.SERVICE_INTERFACE_VERSION,
			RemoteServicePublication.SERVICE_PROPERTIES, "service.uri" }); //$NON-NLS-1$
	// queue for incoming remote service available events
	private ListenerQueue queue;
	private EventManager eventManager;

	// This class is to hold the discovered endpoint available events
	class DiscoveredEndpointEvent {
		private RemoteServiceEndpointDescription rsEndpointDescription;

		public DiscoveredEndpointEvent(
				RemoteServiceEndpointDescription rsEndpointDescription) {
			this.rsEndpointDescription = rsEndpointDescription;
		}

		public RemoteServiceEndpointDescription getEndpointDescription() {
			return rsEndpointDescription;
		}
	}

	public DiscoveredServiceTrackerImpl(DistributionProviderImpl dp) {
		this.distributionProvider = dp;
		ThreadGroup eventGroup = new ThreadGroup("Remote Service Dispatcher"); //$NON-NLS-1$
		eventGroup.setDaemon(true);
		eventManager = new EventManager("Remote Service Dispatcher", eventGroup); //$NON-NLS-1$
		queue = new ListenerQueue(eventManager);
		CopyOnWriteIdentityMap listeners = new CopyOnWriteIdentityMap();
		listeners.put(this, this);
		queue.queueListeners(listeners.entrySet(), new EventDispatcher() {
			public void dispatchEvent(Object eventListener,
					Object listenerObject, int eventAction, Object eventObject) {
				RemoteServiceEndpointDescription rsEndpointDescription = ((DiscoveredEndpointEvent) eventObject)
						.getEndpointDescription();
				try {
					handleDiscoveredServiceAvailable(rsEndpointDescription);
				} catch (Exception e) {
					logError("handleDiscoveredServiceAvailble", //$NON-NLS-1$
							"Unexpected exception with rsEndpointDescription=" //$NON-NLS-1$
									+ rsEndpointDescription, e);
					throw new RuntimeException(
							"Unexpected exception with rsEndpointDescription=" //$NON-NLS-1$
									+ rsEndpointDescription, e);
				}
			}
		});
	}

	public void close() {
		if (eventManager != null) {
			eventManager.close();
			eventManager = null;
			queue = null;
		}
		serviceLocations.clear();
		discoveredRemoteServiceRegistrations.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.discovery.DiscoveredServiceTracker#serviceChanged(org
	 * .osgi.service.discovery.DiscoveredServiceNotification)
	 */
	public void serviceChanged(final DiscoveredServiceNotification notification) {
		if (notification == null)
			return;
		int notificationType = notification.getType();
		switch (notificationType) {
		case DiscoveredServiceNotification.AVAILABLE:
			RemoteServiceEndpointDescription adesc = null;
			try {
				// If the service endpoint description is not ECF's then we
				// don't process it
				adesc = getECFDescription(notification
						.getServiceEndpointDescription());
			} catch (Exception e) {
				logError("serviceChanged.AVAILABLE", //$NON-NLS-1$
						"Error creating ECF endpoint description", e); //$NON-NLS-1$
				return;
			}
			// If it's not for us then return
			if (adesc == null)
				return;

			if (!isValidDescription(adesc)) {
				trace("serviceChanged.AVAILABLE", //$NON-NLS-1$
						"Duplicate or invalid description=" + adesc); //$NON-NLS-1$
				return;
			}
			final RemoteServiceEndpointDescription rsEndpointDescription = adesc;

			// put in queue and execute asynchronously
			queue.dispatchEventAsynchronous(0, new DiscoveredEndpointEvent(
					rsEndpointDescription));
			break;
		case DiscoveredServiceNotification.UNAVAILABLE:
			try {
				RemoteServiceEndpointDescription udesc = getECFDescription(notification
						.getServiceEndpointDescription());
				// If it's not for us then return
				if (udesc == null)
					return;

				// Remove existing proxy service registrations that correspond
				// to the
				// given serviceID
				synchronized (serviceLocations) {
					ServiceRegistration[] proxyServiceRegistrations = removeProxyServiceRegistrations(udesc);
					// Then unregister them
					if (proxyServiceRegistrations != null) {
						for (int i = 0; i < proxyServiceRegistrations.length; i++) {
							trace("handleDiscoveredServiceUnavailable", //$NON-NLS-1$
									"proxyServiceRegistrations=" //$NON-NLS-1$
											+ proxyServiceRegistrations[i]
											+ ",serviceEndpointDesc=" + udesc); //$NON-NLS-1$
							unregisterProxyServiceRegistration(udesc,
									proxyServiceRegistrations[i]);
						}
						removeDiscoveredServiceID(udesc);
					}
				}
			} catch (Exception e) {
				logError("serviceChanged", "UNAVAILABLE", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			break;
		case DiscoveredServiceNotification.MODIFIED:
			// Do nothing for now
			break;
		case DiscoveredServiceNotification.MODIFIED_ENDMATCH:
			// Do nothing for now
			break;
		default:
			logWarning("serviceChanged", "DiscoveredServiceNotification type=" //$NON-NLS-1$ //$NON-NLS-2$
					+ notificationType + " not found.  Ignoring"); //$NON-NLS-1$
			break;
		}
	}

	private void handleDiscoveredServiceAvailable(
			RemoteServiceEndpointDescription endpointDescription) {
		// Find IRemoteServiceContainers for the given
		// RemoteServiceEndpointDescription via registered services
		IRemoteServiceContainer[] rsContainers = findProxyContainers(endpointDescription);
		// If none found, we have nothing to do
		if (rsContainers == null || rsContainers.length == 0) {
			logWarning("handleDiscoveredServiceAvailable", //$NON-NLS-1$
					"No local RemoteServiceContainers found for endpoint description=" //$NON-NLS-1$
							+ endpointDescription);
			return;
		}
		// Get endpoint ID
		ID endpointID = endpointDescription.getEndpointAsID();
		// Get remote service filter from the service endpoint description
		// if it exists.
		String remoteServiceFilter = getFullRemoteServicesFilter(
				endpointDescription.getRemoteServicesFilter(),
				endpointDescription.getRemoteServiceId());
		// Get provided interfaces as collection
		Collection providedInterfaces = endpointDescription
				.getProvidedInterfaces();
		// Now for all remote service containers
		for (int i = 0; i < rsContainers.length; i++) {
			for (Iterator j = providedInterfaces.iterator(); j.hasNext();) {
				String providedInterface = (String) j.next();
				IRemoteServiceReference[] remoteReferences = null;
				// fire IProxyDistributionListeners pre get references
				firePreGetRemoteServiceReferences(endpointDescription,
						rsContainers[i]);
				try {
					// Get remote remote references for each container
					remoteReferences = rsContainers[i].getContainerAdapter()
							.getRemoteServiceReferences(endpointID,
									new ID[] { endpointID }, providedInterface,
									remoteServiceFilter);
				} catch (ContainerConnectException e) {
					logError("handleDiscoveredServiceAvailable", "rsca=" //$NON-NLS-1$ //$NON-NLS-2$
							+ rsContainers[i] + ",endpointId=" + endpointID //$NON-NLS-1$
							+ ",intf=" + providedInterface //$NON-NLS-1$
							+ ". Connect error in getRemoteServiceReferences", //$NON-NLS-1$
							e);
					continue;
				} catch (InvalidSyntaxException e) {
					logError(
							"handleDiscoveredServiceAvailable", //$NON-NLS-1$
							"rsca=" //$NON-NLS-1$
									+ rsContainers[i]
									+ ",endpointId=" //$NON-NLS-1$
									+ endpointID
									+ ",intf=" //$NON-NLS-1$
									+ providedInterface
									+ " Filter syntax error in getRemoteServiceReferences", //$NON-NLS-1$
							e);
					continue;
				}
				if (remoteReferences == null || remoteReferences.length == 0) {
					logError("handleDiscoveredServiceAvailable", //$NON-NLS-1$
							"getRemoteServiceReferences result is empty. " //$NON-NLS-1$
									+ "containerHelper=" //$NON-NLS-1$
									+ rsContainers[i]
									+ "remoteReferences=" //$NON-NLS-1$
									+ ((remoteReferences == null) ? "null" //$NON-NLS-1$
											: Arrays.asList(remoteReferences)
													.toString()), null);
					continue;
				} else {
					registerRemoteServiceReferences(endpointDescription,
							rsContainers[i], remoteReferences);
				}
			}
		}
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

	private void firePreGetRemoteServiceReferences(
			final IRemoteServiceEndpointDescription endpointDescription,
			final IRemoteServiceContainer remoteServiceContainer) {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			IProxyDistributionListener[] listeners = activator
					.getProxyDistributionListeners();
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					final IProxyDistributionListener l = listeners[i];
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							logError(
									"firePreGetRemoteServiceReferences", //$NON-NLS-1$
									"Exception calling proxy distribution listener", //$NON-NLS-1$
									exception);
						}

						public void run() throws Exception {
							l.retrievingRemoteServiceReferences(
									endpointDescription, remoteServiceContainer);
						}
					});
				}
			}
		}
	}

	private void firePreRegister(
			final IRemoteServiceEndpointDescription endpointDescription,
			final IRemoteServiceContainer remoteServiceContainer,
			final IRemoteServiceReference remoteServiceReference) {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			IProxyDistributionListener[] listeners = activator
					.getProxyDistributionListeners();
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					final IProxyDistributionListener l = listeners[i];
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							logError(
									"firePreRegister", //$NON-NLS-1$
									"Exception calling proxy distribution listener", //$NON-NLS-1$
									exception);
						}

						public void run() throws Exception {
							l.registering(endpointDescription,
									remoteServiceContainer,
									remoteServiceReference);
						}
					});
				}
			}
		}
	}

	private void firePostRegister(
			final IRemoteServiceEndpointDescription endpointDescription,
			final IRemoteServiceContainer remoteServiceContainer,
			final IRemoteServiceReference remoteServiceReference,
			final ServiceRegistration serviceRegistration) {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			IProxyDistributionListener[] listeners = activator
					.getProxyDistributionListeners();
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					final IProxyDistributionListener l = listeners[i];
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							logError(
									"firePreRegister", //$NON-NLS-1$
									"Exception calling proxy distribution listener", //$NON-NLS-1$
									exception);
						}

						public void run() throws Exception {
							l.registered(endpointDescription,
									remoteServiceContainer,
									remoteServiceReference, serviceRegistration);
						}
					});
				}
			}
		}
	}

	private void fireUnregister(
			final IRemoteServiceEndpointDescription endpointDescription,
			final ServiceRegistration registration) {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			IProxyDistributionListener[] listeners = activator
					.getProxyDistributionListeners();
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					final IProxyDistributionListener l = listeners[i];
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							logError(
									"fireUnregister", //$NON-NLS-1$
									"Exception calling proxy distribution listener", //$NON-NLS-1$
									exception);
						}

						public void run() throws Exception {
							l.unregistered(endpointDescription, registration);
						}
					});
				}
			}
		}
	}

	private RemoteServiceEndpointDescription getECFDescription(
			ServiceEndpointDescription aServiceEndpointDesc) {
		RemoteServiceEndpointDescription ecfSED;
		if (!(aServiceEndpointDesc instanceof RemoteServiceEndpointDescription)) {
			ecfSED = (RemoteServiceEndpointDescription) Activator
					.getDefault()
					.getAdapterManager()
					.loadAdapter(aServiceEndpointDesc,
							RemoteServiceEndpointDescription.class.getName());
		} else
			ecfSED = (RemoteServiceEndpointDescription) aServiceEndpointDesc;
		return ecfSED;
	}

	private boolean findProxyServiceRegistration(
			RemoteServiceEndpointDescription sed) {
		synchronized (discoveredRemoteServiceRegistrations) {
			for (Iterator i = discoveredRemoteServiceRegistrations.keySet()
					.iterator(); i.hasNext();) {
				ID containerID = (ID) i.next();
				RemoteServiceRegistration reg = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
						.get(containerID);
				if (reg.hasRSED(sed))
					return true;
			}
			return false;
		}
	}

	private ServiceRegistration[] removeProxyServiceRegistrations(
			RemoteServiceEndpointDescription sed) {
		List results = new ArrayList();
		synchronized (discoveredRemoteServiceRegistrations) {
			final List containerIDsToRemove = new ArrayList();
			for (Iterator i = discoveredRemoteServiceRegistrations.keySet()
					.iterator(); i.hasNext();) {
				ID containerID = (ID) i.next();
				RemoteServiceRegistration reg = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
						.get(containerID);
				if (reg != null) {
					ServiceRegistration sr = reg.removeServiceRegistration(sed);
					if (sr != null)
						results.add(sr);
					if (reg.isEmpty()) {
						reg.dispose();
						containerIDsToRemove.add(containerID);
					}
				}
			}
			// Outside of the iterator, now remove any containerID found to
			// match
			for (Iterator i = containerIDsToRemove.iterator(); i.hasNext();) {
				discoveredRemoteServiceRegistrations.remove(i.next());
			}
			return (ServiceRegistration[]) results
					.toArray(new ServiceRegistration[] {});
		}
	}

	class RemoteServiceReferenceUnregisteredListener implements
			IRemoteServiceListener {
		public void handleServiceEvent(IRemoteServiceEvent event) {
			if (event instanceof IRemoteServiceUnregisteredEvent) {
				ID containerID = event.getContainerID();
				ID localContainerID = event.getLocalContainerID();
				IRemoteServiceReference reference = event.getReference();
				trace("handleRemoteServiceUnregisteredEvent", //$NON-NLS-1$
						"localContainerID=" + localContainerID //$NON-NLS-1$
								+ ",containerID=" + containerID //$NON-NLS-1$
								+ ",remoteReference=" + reference); //$NON-NLS-1$
				// Synchronize on serviceLocations so no other changes happen
				// while
				// this is going on...as it can be invoked by an arbitrary
				RemoteServiceRegistration.RSEDAndSRAssoc[] assocs = null;
				synchronized (serviceLocations) {
					synchronized (discoveredRemoteServiceRegistrations) {
						List containerIDsToRemove = new ArrayList();
						RemoteServiceRegistration rsRegs = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
								.get(localContainerID);
						// If we've got any remote service registrations for the
						// containerID
						if (rsRegs != null) {
							assocs = rsRegs
									.removeServiceRegistration(reference);
							// If this removes *all* references for this
							// registration
							if (rsRegs.isEmpty()) {
								rsRegs.dispose();
								containerIDsToRemove.add(localContainerID);
							}
							if (assocs != null) {
								for (int i = 0; i < assocs.length; i++) {
									removeDiscoveredServiceID(assocs[i]
											.getRSED());
								}
							}
						}
						for (Iterator i = containerIDsToRemove.iterator(); i
								.hasNext();) {
							discoveredRemoteServiceRegistrations.remove(i
									.next());
						}
					}

				}
				// Call this outside of synchronized block
				if (assocs != null) {
					for (int i = 0; i < assocs.length; i++) {
						ServiceRegistration sr = assocs[i].getSR();
						trace("handleRemoteServiceUnregisteredEvent.unregister", //$NON-NLS-1$
						"localContainerID=" //$NON-NLS-1$
								+ localContainerID + ",containerID=" //$NON-NLS-1$
								+ containerID + ",remoteReference=" //$NON-NLS-1$
								+ reference + ",proxyServiceRegistrations=" //$NON-NLS-1$
								+ sr);
						unregisterProxyServiceRegistration(assocs[i].getRSED(),
								sr);
					}
				}
			}
		}
	}

	private void unregisterProxyServiceRegistration(
			IRemoteServiceEndpointDescription endpointDescription,
			ServiceRegistration reg) {
		try {
			distributionProvider.removeRemoteService(reg.getReference());
			reg.unregister();
		} catch (IllegalStateException e) {
			// Ignore
			logWarning("unregisterProxyServiceRegistration", //$NON-NLS-1$
					"Exception unregistering serviceRegistration=" + reg); //$NON-NLS-1$
		} catch (Exception e) {
			logError("unregisterProxyServiceRegistration", //$NON-NLS-1$
					"Exception unregistering serviceRegistration=" + reg, e); //$NON-NLS-1$
		}
		fireUnregister(endpointDescription, reg);
	}

	private void registerRemoteServiceReferences(
			RemoteServiceEndpointDescription sed,
			IRemoteServiceContainer remoteServiceContainer,
			IRemoteServiceReference[] remoteReferences) {

		synchronized (serviceLocations) {
			// check to make sure that this serviceLocation
			// is still present
			if (!containsDiscoveredServiceID(sed)) {
				logError("registerRemoteServiceReferences", "serviceLocation=" //$NON-NLS-1$ //$NON-NLS-2$
						+ sed + " no longer present", null); //$NON-NLS-1$
				return;
			}
			// check to make sure that the proxy service registry is not
			// already there
			if (findProxyServiceRegistration(sed)) {
				logError("registerRemoteServiceReferences", //$NON-NLS-1$
						"serviceEndpointDesc=" + sed //$NON-NLS-1$
								+ " previously registered locally...ignoring", //$NON-NLS-1$
						null);
				return;
			}
			// Then get/setup remote service
			for (int i = 0; i < remoteReferences.length; i++) {
				// Get IRemoteService, used to create the proxy
				IRemoteService remoteService = remoteServiceContainer
						.getContainerAdapter().getRemoteService(
								remoteReferences[i]);
				// If no remote service then give up
				if (remoteService == null) {
					logError("registerRemoteServiceReferences", //$NON-NLS-1$
							"Remote service is null for remote reference " //$NON-NLS-1$
									+ remoteReferences[i], null);
					continue;
				}

				// Get classes to register for remote service
				String[] clazzes = (String[]) remoteReferences[i]
						.getProperty(Constants.OBJECTCLASS);
				if (clazzes == null || clazzes.length == 0) {
					logError("registerRemoteServiceReferences", //$NON-NLS-1$
							"No classes specified for remote service reference " //$NON-NLS-1$
									+ remoteReferences[i], null);
					continue;
				}

				// Get service properties for the proxy registration
				Dictionary properties = getPropertiesForRemoteService(sed,
						remoteServiceContainer, remoteReferences[i],
						remoteService);

				// Create proxy right here
				Object proxy = null;
				try {
					proxy = remoteService.getProxy();
					if (proxy == null) {
						logError("registerRemoteServiceReferences", //$NON-NLS-1$
								"Remote service proxy is null", null); //$NON-NLS-1$
						continue;
					}
					// Fire pre register notification fir
					// IProxyDistributionListener
					firePreRegister(sed, remoteServiceContainer,
							remoteReferences[i]);
					trace("registerRemoteServiceReferences", "rsca=" //$NON-NLS-1$ //$NON-NLS-2$
							+ remoteServiceContainer + ",remoteReference=" //$NON-NLS-1$
							+ remoteReferences[i]);
					// Actually register proxy here
					ServiceRegistration registration = Activator.getDefault()
							.getContext()
							.registerService(clazzes, proxy, properties);

					RemoteServiceRegistration reg = getProxyServiceRegistration(remoteServiceContainer);
					reg.addServiceRegistration(remoteReferences[i], sed,
							registration);
					// And add to distribution provider
					distributionProvider.addRemoteService(registration
							.getReference());
					trace("addLocalServiceRegistration.COMPLETE", //$NON-NLS-1$
							"containerHelper=" + remoteServiceContainer //$NON-NLS-1$
									+ ",remoteServiceReference=" //$NON-NLS-1$
									+ remoteReferences[i]
									+ ",localServiceRegistration=" //$NON-NLS-1$
									+ registration);
					// Fire IProxyDistributionListener to notify we're done
					firePostRegister(sed, remoteServiceContainer,
							remoteReferences[i], registration);
				} catch (Exception e) {
					logError("registerRemoteServiceReferences", //$NON-NLS-1$
							"Exception creating or registering remote reference " //$NON-NLS-1$
									+ remoteReferences[i], e);
					continue;
				}
			}
		}
	}

	private RemoteServiceRegistration getProxyServiceRegistration(
			IRemoteServiceContainer rsContainer) {
		ID localContainerID = rsContainer.getContainer().getID();
		synchronized (discoveredRemoteServiceRegistrations) {
			RemoteServiceRegistration reg = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
					.get(localContainerID);
			// If there is none, then create one
			if (reg == null) {
				reg = new RemoteServiceRegistration(rsContainer,
						new RemoteServiceReferenceUnregisteredListener());
				discoveredRemoteServiceRegistrations.put(localContainerID, reg);
			}
			return reg;
		}
	}

	private boolean isRemoteServiceProperty(String propertyKey) {
		return ecfRemoteServiceProperties.contains(propertyKey);
	}

	private Dictionary getPropertiesForRemoteService(
			RemoteServiceEndpointDescription rsEndpointDescription,
			IRemoteServiceContainer rsContainer,
			IRemoteServiceReference rsReference, IRemoteService remoteService) {

		Properties props = new Properties();
		// Add the required 'service.imported' property, which for ECF rs
		// providers
		// exposes the IRemoteService
		props.put(IDistributionConstants.SERVICE_IMPORTED, remoteService);

		// Add service intents...if not null (optional property)
		String[] serviceIntents = rsEndpointDescription.getServiceIntents();
		if (serviceIntents != null)
			props.put(IDistributionConstants.SERVICE_INTENTS, serviceIntents);

		// Then add all other service properties
		String[] propKeys = rsReference.getPropertyKeys();
		for (int i = 0; i < propKeys.length; i++) {
			if (!isRemoteServiceProperty(propKeys[i])) {
				props.put(propKeys[i], rsReference.getProperty(propKeys[i]));
			}
		}

		// make the service identifiable by consumers
		// especially org.eclipse.ecf.remoteservice.ui.dosgi
		ID endpointId = (ID) rsReference
				.getProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_ID);
		if (endpointId == null) {
			endpointId = rsEndpointDescription.getEndpointAsID();
		}
		final Long serviceId = (Long) rsReference
				.getProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		props.put(ServicePublication.ENDPOINT_ID, endpointId.toExternalForm()
				+ "#" + serviceId); //$NON-NLS-1$

		// finally add service.imported.configs
		addImportedConfigsProperties(
				getContainerTypeDescription(rsContainer.getContainer()),
				rsEndpointDescription.getSupportedConfigs(), props);

		return props;
	}

	private void addImportedConfigsProperties(
			ContainerTypeDescription containerTypeDescription,
			String[] remoteExportedConfigs, Dictionary exportedProperties) {
		if (containerTypeDescription == null)
			return;
		if (remoteExportedConfigs != null) {
			String[] importedConfigs = containerTypeDescription
					.getImportedConfigs(remoteExportedConfigs);
			if (importedConfigs != null) {
				// Add the service.imported.configs property
				exportedProperties.put(
						IDistributionConstants.SERVICE_IMPORTED_CONFIGS,
						importedConfigs);
				// First get any/all properties to add
				Dictionary localConfigProperties = containerTypeDescription
						.getPropertiesForImportedConfigs(importedConfigs,
								exportedProperties);
				if (localConfigProperties != null) {
					for (Enumeration e = localConfigProperties.keys(); e
							.hasMoreElements();) {
						String key = (String) e.nextElement();
						exportedProperties.put(key,
								localConfigProperties.get(key));
					}
				}
			}
		}
	}

	protected ContainerTypeDescription getContainerTypeDescription(
			IContainer container) {
		IContainerManager containerManager = getContainerManager();
		if (containerManager == null)
			return null;
		return containerManager.getContainerTypeDescription(container.getID());
	}

	protected IContainerManager getContainerManager() {
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		return activator.getContainerManager();
	}

	private boolean addDiscoveredServiceID(RemoteServiceEndpointDescription desc) {
		synchronized (serviceLocations) {
			return serviceLocations.add(desc);
		}
	}

	private boolean removeDiscoveredServiceID(
			RemoteServiceEndpointDescription desc) {
		synchronized (serviceLocations) {
			return serviceLocations.remove(desc);
		}
	}

	private boolean containsDiscoveredServiceID(
			RemoteServiceEndpointDescription desc) {
		synchronized (serviceLocations) {
			return serviceLocations.contains(desc);
		}
	}

	protected void trace(String methodName, String message) {
		LogUtility.trace(methodName, DebugOptions.DISCOVEREDSERVICETRACKER,
				this.getClass(), message);
	}

	protected void traceException(String methodName, String message, Throwable t) {
		LogUtility.traceException(methodName, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), message, t);
	}

	protected void logError(String methodName, String message, Throwable t) {
		LogUtility.logError(methodName, DebugOptions.DISCOVEREDSERVICETRACKER,
				this.getClass(), message, t);
	}

	protected void logError(String methodName, String message) {
		LogUtility.logError(methodName, DebugOptions.DISCOVEREDSERVICETRACKER,
				this.getClass(), message);
	}

	protected void logWarning(String methodName, String message) {
		LogUtility
				.logWarning(methodName, DebugOptions.DISCOVEREDSERVICETRACKER,
						this.getClass(), message);
	}

	private boolean isValidDescription(
			RemoteServiceEndpointDescription rsEndpointDescription) {
		if (rsEndpointDescription == null)
			return false;
		synchronized (serviceLocations) {
			if (containsDiscoveredServiceID(rsEndpointDescription)) {
				return false;
			} else {
				addDiscoveredServiceID(rsEndpointDescription);
				return true;
			}
		}
	}

	private IRemoteServiceContainer[] findProxyContainers(
			final RemoteServiceEndpointDescription rsEndpointDescription) {
		// Get activator
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		// Get finder (as service)
		IProxyContainerFinder finder = activator
				.getProxyRemoteServiceContainerFinder();
		if (finder == null) {
			logError("findRemoteServiceContainersViaService", //$NON-NLS-1$
					"No container finders available"); //$NON-NLS-1$
			return null;
		}
		return finder.findProxyContainers(rsEndpointDescription.getServiceID(),
				rsEndpointDescription);
	}

}
