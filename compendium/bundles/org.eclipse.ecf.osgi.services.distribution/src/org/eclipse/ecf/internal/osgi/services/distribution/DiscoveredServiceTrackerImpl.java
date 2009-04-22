/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.osgi.services.discovery.*;
import org.eclipse.ecf.osgi.services.distribution.IProxyContainerFinder;
import org.eclipse.ecf.osgi.services.distribution.IServiceConstants;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceUnregisteredEvent;
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.discovery.*;

public class DiscoveredServiceTrackerImpl implements DiscoveredServiceTracker {

	private DistributionProviderImpl distributionProvider;
	private IExecutor executor;
	private List serviceLocations = new ArrayList();
	// <Map<containerID><RemoteServiceRegistration>
	private Map discoveredRemoteServiceRegistrations = Collections
			.synchronizedMap(new HashMap());
	private List ecfRemoteServiceProperties = Arrays.asList(new String[] {
			Constants.SERVICE_ID, Constants.OBJECTCLASS,
			IServicePublication.PROP_KEY_ENDPOINT_ID,
			IServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME,
			IServicePublication.PROP_KEY_ENDPOINT_LOCATION,
			IServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
			IServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION,
			IServicePublication.PROP_KEY_SERVICE_PROPERTIES });

	public DiscoveredServiceTrackerImpl(DistributionProviderImpl dp,
			IExecutor executor) {
		this.distributionProvider = dp;
		this.executor = executor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.discovery.DiscoveredServiceTracker#serviceChanged(org
	 * .osgi.service.discovery.DiscoveredServiceNotification)
	 */
	public void serviceChanged(final DiscoveredServiceNotification notification) {
		if (notification == null) {
			logWarning("serviceChanged",
					"DiscoveredServiceNotification is null.  Ignoring");
			return;
		}
		int notificationType = notification.getType();
		switch (notificationType) {
		case DiscoveredServiceNotification.AVAILABLE:
			ECFServiceEndpointDescription adesc = null;
			try {
				// If the service endpoint description is not ECF's then we
				// don't process it
				adesc = getECFserviceEndpointDescription(notification
						.getServiceEndpointDescription());
			} catch (Exception e) {
				logError("serviceChanged.AVAILABLE",
						"Error creating ECF endpoint description", e);
				return;
			}
			// If it's not for us then return
			if (adesc == null)
				return;

			if (!isValidService(adesc)) {
				logWarning("serviceChanged.AVAILABLE",
						"Duplicate or invalid description=" + adesc);
				return;
			}
			final ECFServiceEndpointDescription ecfASED = adesc;
			// Otherwise execute with executor
			this.executor.execute(new IProgressRunnable() {
				public Object run(IProgressMonitor monitor) throws Exception {
					try {
						handleDiscoveredServiceAvailable(ecfASED, monitor);
					} catch (Exception e) {
						logError("handleDiscoveredServiceAvailble",
								"Unexpected exception with ecfSED=" + ecfASED,
								e);
						throw e;
					}
					return null;
				}
			}, new NullProgressMonitor());
			break;
		case DiscoveredServiceNotification.UNAVAILABLE:
			try {
				ECFServiceEndpointDescription udesc = getECFserviceEndpointDescription(notification
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
							trace("handleDiscoveredServiceUnavailable",
									"proxyServiceRegistrations="
											+ proxyServiceRegistrations[i]
											+ ",serviceEndpointDesc=" + udesc);
							unregisterProxyServiceRegistration(proxyServiceRegistrations[i]);
						}
						removeDiscoveredServiceID(udesc);
					}
				}
			} catch (Exception e) {
				logError("serviceChanged", "UNAVAILABLE", e);
			}
			break;
		case DiscoveredServiceNotification.MODIFIED:
			// Do nothing for now
			break;
		case DiscoveredServiceNotification.MODIFIED_ENDMATCH:
			// Do nothing for now
			break;
		default:
			logWarning("serviceChanged", "DiscoveredServiceNotification type="
					+ notificationType + " not found.  Ignoring");
			break;
		}
	}

	private boolean isValidService(ECFServiceEndpointDescription desc) {
		if (desc == null)
			return false;
		synchronized (serviceLocations) {
			if (containsDiscoveredServiceID(desc)) {
				return false;
			} else {
				addDiscoveredServiceID(desc);
				return true;
			}
		}
	}

	private IRemoteServiceContainer[] findRemoteServiceContainers(
			IServiceID serviceID, IServiceEndpointDescription description,
			IProgressMonitor monitor) {
		Activator activator = Activator.getDefault();
		if (activator == null)
			return new IRemoteServiceContainer[0];
		IProxyContainerFinder finder = activator
				.getProxyRemoteServiceContainerFinder();
		if (finder == null) {
			logError("findRemoteServiceContainersViaService",
					"No container finders available");
			return new IRemoteServiceContainer[0];
		}
		return finder.findProxyContainers(serviceID, description, monitor);
	}

	private void handleDiscoveredServiceAvailable(
			ECFServiceEndpointDescription ecfSED, IProgressMonitor monitor) {
		// Find IRemoteServiceContainers for the given
		// ECFServiceEndpointDescription via registered services
		IRemoteServiceContainer[] rsContainers = findRemoteServiceContainers(
				ecfSED.getServiceID(), ecfSED, monitor);
		if (rsContainers == null || rsContainers.length == 0) {
			logError("handleDiscoveredServiceAvailable",
					"No RemoteServiceContainerAdapters found for description="
							+ ecfSED, null);
			return;
		}
		// Give warning if more than one ContainerAdapterHelper found
		if (rsContainers.length > 1) {
			logWarning("handleDiscoveredServiceAvailable",
					"Multiple remote service containers="
							+ Arrays.asList(rsContainers)
							+ " found for service endpoint description="
							+ ecfSED);
		}

		// Get endpoint ID
		ID ecfEndpointID = ecfSED.getECFEndpointID();
		// Get remote service filter from the service endpoint description
		// if it exists.
		String remoteServiceFilter = ecfSED.getRemoteServicesFilter();
		// For all remote service container adapters
		// Get futureRemoteReferences...then create a thread
		// to process the future
		Collection providedInterfaces = ecfSED.getProvidedInterfaces();
		for (int i = 0; i < rsContainers.length; i++) {
			for (Iterator j = providedInterfaces.iterator(); j.hasNext();) {
				String providedInterface = (String) j.next();
				IRemoteServiceReference[] remoteReferences = null;
				try {
					remoteReferences = rsContainers[i].getContainerAdapter()
							.getRemoteServiceReferences(ecfEndpointID,
									providedInterface, remoteServiceFilter);
				} catch (ContainerConnectException e) {
					logError("handleDiscoveredServiceAvailable", "rsca="
							+ rsContainers[i] + ",endpointId=" + ecfEndpointID
							+ ",intf=" + providedInterface
							+ ". Connect error in getRemoteServiceReferences",
							e);
					continue;
				} catch (InvalidSyntaxException e) {
					logError(
							"handleDiscoveredServiceAvailable",
							"rsca="
									+ rsContainers[i]
									+ ",endpointId="
									+ ecfEndpointID
									+ ",intf="
									+ providedInterface
									+ " Filter syntax error in getRemoteServiceReferences",
							e);
					continue;
				}
				if (remoteReferences == null || remoteReferences.length == 0) {
					logError("handleDiscoveredServiceAvailable",
							"getRemoteServiceReferences result is empty. "
									+ "containerHelper="
									+ rsContainers[i]
									+ "remoteReferences="
									+ ((remoteReferences == null) ? "null"
											: Arrays.asList(remoteReferences)
													.toString()), null);
					continue;
				} else
					registerRemoteServiceReferences(ecfSED, rsContainers[i],
							remoteReferences);
			}
		}
	}

	private ECFServiceEndpointDescription getECFserviceEndpointDescription(
			ServiceEndpointDescription aServiceEndpointDesc) {
		ECFServiceEndpointDescription ecfSED;
		if (!(aServiceEndpointDesc instanceof ECFServiceEndpointDescription)) {
			ecfSED = (ECFServiceEndpointDescription) Activator.getDefault()
					.getAdapterManager().loadAdapter(aServiceEndpointDesc,
							ECFServiceEndpointDescription.class.getName());
		} else
			ecfSED = (ECFServiceEndpointDescription) aServiceEndpointDesc;
		return ecfSED;
	}

	private boolean findProxyServiceRegistration(ServiceEndpointDescription sed) {
		for (Iterator i = discoveredRemoteServiceRegistrations.keySet()
				.iterator(); i.hasNext();) {
			ID containerID = (ID) i.next();
			RemoteServiceRegistration reg = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
					.get(containerID);
			if (sed.equals(reg.getServiceEndpointDescription()))
				return true;
		}
		return false;
	}

	private ServiceRegistration[] removeProxyServiceRegistrations(
			ServiceEndpointDescription sed) {
		List results = new ArrayList();
		for (Iterator i = discoveredRemoteServiceRegistrations.keySet()
				.iterator(); i.hasNext();) {
			ID containerID = (ID) i.next();
			RemoteServiceRegistration reg = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
					.get(containerID);
			// If the serviceID matches, then remove the
			// RemoteServiceRegistration
			// Get the service registrations and then dispose of the
			// RemoteServiceRegistration instance
			if (sed.equals(reg.getServiceEndpointDescription())) {
				i.remove();
				results.addAll(reg.removeAllServiceRegistrations());
				reg.dispose();
			}
		}
		// Then return all the ServiceRegistrations that were found
		// corresponding to this serviceID
		return (ServiceRegistration[]) results
				.toArray(new ServiceRegistration[] {});
	}

	class RemoteServiceReferenceUnregisteredListener implements
			IRemoteServiceListener {
		public void handleServiceEvent(IRemoteServiceEvent event) {
			if (event instanceof IRemoteServiceUnregisteredEvent) {
				trace("handleRemoteServiceUnregisteredEvent",
						"localContainerID=" + event.getLocalContainerID()
								+ ",containerID=" + event.getContainerID()
								+ ",remoteReference=" + event.getReference());
				// Synchronize on the map so no other changes happen while
				// this is going on...as it can be invoked by an arbitrary
				// thread
				ServiceRegistration[] proxyServiceRegistrations = null;
				synchronized (serviceLocations) {
					RemoteServiceRegistration rsRegs = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
							.get(event.getLocalContainerID());
					if (rsRegs != null) {
						proxyServiceRegistrations = rsRegs
								.removeServiceRegistration(event.getReference());
						if (rsRegs.isEmpty()) {
							rsRegs.dispose();
							discoveredRemoteServiceRegistrations.remove(event
									.getContainerID());
						}
					}
				}
				// Call this outside of synchronized block
				if (proxyServiceRegistrations != null) {
					for (int i = 0; i < proxyServiceRegistrations.length; i++) {
						trace(
								"handleRemoteServiceUnregisteredEvent.unregister",
								"localContainerID="
										+ event.getLocalContainerID()
										+ ",containerID="
										+ event.getContainerID()
										+ ",remoteReference="
										+ event.getReference()
										+ ",proxyServiceRegistrations="
										+ proxyServiceRegistrations[i]);
						unregisterProxyServiceRegistration(proxyServiceRegistrations[i]);
					}
				}
			}
		}
	}

	private void unregisterProxyServiceRegistration(ServiceRegistration reg) {
		try {
			distributionProvider.removeRemoteService(reg.getReference());
			reg.unregister();
		} catch (IllegalStateException e) {
			// Ignore
			logWarning("unregisterProxyServiceRegistration",
					"Exception unregistering serviceRegistration=" + reg);
		} catch (Exception e) {
			logError("unregisterProxyServiceRegistration",
					"Exception unregistering serviceRegistration=" + reg, e);
		}
	}

	private void registerRemoteServiceReferences(
			ECFServiceEndpointDescription sed, IRemoteServiceContainer ch,
			IRemoteServiceReference[] remoteReferences) {

		synchronized (serviceLocations) {
			// check to make sure that this serviceLocation
			// is still present
			if (!containsDiscoveredServiceID(sed)) {
				logError("registerRemoteServiceReferences", "serviceLocation="
						+ sed + " no longer present", null);
				return;
			}
			// check to make sure that the proxy service registry is not
			// already there
			if (findProxyServiceRegistration(sed)) {
				logError("registerRemoteServiceReferences",
						"serviceEndpointDesc=" + sed
								+ " previously registered locally...ignoring",
						null);
				return;
			}
			// Then setup remote service
			for (int i = 0; i < remoteReferences.length; i++) {
				// Get IRemoteService, used to create the proxy below
				IRemoteService remoteService = ch.getContainerAdapter()
						.getRemoteService(remoteReferences[i]);
				// If no remote service then give up
				if (remoteService == null) {
					logError("registerRemoteServiceReferences",
							"Remote service is null for remote reference "
									+ remoteReferences[i], null);
					continue;
				}

				// Get classes to register for remote service
				String[] clazzes = (String[]) remoteReferences[i]
						.getProperty(Constants.OBJECTCLASS);
				if (clazzes == null || clazzes.length == 0) {
					logError("registerRemoteServiceReferences",
							"No classes specified for remote service reference "
									+ remoteReferences[i], null);
					continue;
				}

				// Get service properties for the proxy
				Dictionary properties = getPropertiesForRemoteService(sed, ch
						.getContainerAdapter(), remoteReferences[i],
						remoteService);

				// Create proxy right here
				Object proxy = null;
				try {
					proxy = remoteService.getProxy();
					if (proxy == null) {
						logError("registerRemoteServiceReferences",
								"Remote service proxy is null", null);
						continue;
					}
					// Finally register
					trace("registerRemoteServiceReferences", "rsca=" + ch
							+ ",remoteReference=" + remoteReferences[i]);
					ServiceRegistration registration = Activator.getDefault()
							.getContext().registerService(clazzes, proxy,
									properties);
					IRemoteServiceReference ref = remoteReferences[i];
					ID containerID = ch.getContainer().getID();
					RemoteServiceRegistration reg = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
							.get(containerID);
					if (reg == null) {
						reg = new RemoteServiceRegistration(
								sed,
								ch,
								new RemoteServiceReferenceUnregisteredListener());
						discoveredRemoteServiceRegistrations.put(containerID,
								reg);
					}
					reg.addServiceRegistration(ref, registration);
					// And add to distribution provider
					distributionProvider.addRemoteService(registration
							.getReference());
					trace("addLocalServiceRegistration.COMPLETE",
							"containerHelper=" + ch
									+ ",remoteServiceReference=" + ref
									+ ",localServiceRegistration="
									+ registration);
				} catch (Exception e) {
					logError("registerRemoteServiceReferences",
							"Exception creating or registering remote reference "
									+ remoteReferences[i], e);
					continue;
				}
			}
		}
	}

	private boolean isRemoteServiceProperty(String propertyKey) {
		return ecfRemoteServiceProperties.contains(propertyKey);
	}

	private Dictionary getPropertiesForRemoteService(
			ServiceEndpointDescription description,
			IRemoteServiceContainerAdapter containerAdapter,
			IRemoteServiceReference remoteReference,
			IRemoteService remoteService) {
		Properties results = new Properties();
		String[] propKeys = remoteReference.getPropertyKeys();
		for (int i = 0; i < propKeys.length; i++) {
			if (!isRemoteServiceProperty(propKeys[i])) {
				results.put(propKeys[i], remoteReference
						.getProperty(propKeys[i]));
			}
		}
		results.put(IServiceConstants.OSGI_REMOTE, remoteService);
		return results;
	}

	private boolean addDiscoveredServiceID(ECFServiceEndpointDescription desc) {
		synchronized (serviceLocations) {
			return serviceLocations.add(new DiscoveredServiceID(desc
					.getServiceID().getLocation(), desc.getRemoteServiceId()));
		}
	}

	private boolean removeDiscoveredServiceID(ECFServiceEndpointDescription desc) {
		synchronized (serviceLocations) {
			return serviceLocations.remove(new DiscoveredServiceID(desc
					.getServiceID().getLocation(), desc.getRemoteServiceId()));
		}
	}

	private boolean containsDiscoveredServiceID(
			ECFServiceEndpointDescription desc) {
		synchronized (serviceLocations) {
			return serviceLocations.contains(new DiscoveredServiceID(desc
					.getServiceID().getLocation(), desc.getRemoteServiceId()));
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

}
