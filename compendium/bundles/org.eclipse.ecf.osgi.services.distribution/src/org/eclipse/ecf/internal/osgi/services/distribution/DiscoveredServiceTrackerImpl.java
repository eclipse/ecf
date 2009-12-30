/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;

public class DiscoveredServiceTrackerImpl implements DiscoveredServiceTracker {

	private DistributionProviderImpl distributionProvider;
	private IExecutor executor;
	private List serviceLocations = new ArrayList();
	// <Map<containerID><RemoteServiceRegistration>
	private Map discoveredRemoteServiceRegistrations = Collections
			.synchronizedMap(new HashMap());
	private List ecfRemoteServiceProperties = Arrays.asList(new String[] {
			Constants.SERVICE_ID, Constants.OBJECTCLASS,
			org.eclipse.ecf.remoteservice.Constants.SERVICE_ID,
			org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_ID,
			org.eclipse.ecf.remoteservice.Constants.SERVICE_RANKING,
			RemoteServicePublication.ENDPOINT_ID,
			RemoteServicePublication.ENDPOINT_INTERFACE_NAME,
			RemoteServicePublication.ENDPOINT_LOCATION,
			RemoteServicePublication.SERVICE_INTERFACE_NAME,
			RemoteServicePublication.SERVICE_INTERFACE_VERSION,
			RemoteServicePublication.SERVICE_PROPERTIES, "service.uri" }); // set

	// by
	// r-osgi

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
				logError("serviceChanged.AVAILABLE",
						"Error creating ECF endpoint description", e);
				return;
			}
			// If it's not for us then return
			if (adesc == null)
				return;

			if (!isValidDescription(adesc)) {
				trace("serviceChanged.AVAILABLE",
						"Duplicate or invalid description=" + adesc);
				return;
			}
			final RemoteServiceEndpointDescription rsEndpointDescription = adesc;
			// Otherwise execute with executor
			this.executor.execute(new IProgressRunnable() {
				public Object run(IProgressMonitor monitor) throws Exception {
					try {
						handleDiscoveredServiceAvailable(rsEndpointDescription,
								monitor);
					} catch (Exception e) {
						logError("handleDiscoveredServiceAvailble",
								"Unexpected exception with rsEndpointDescription="
										+ rsEndpointDescription, e);
						throw e;
					}
					return null;
				}
			}, new NullProgressMonitor());
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
							trace("handleDiscoveredServiceUnavailable",
									"proxyServiceRegistrations="
											+ proxyServiceRegistrations[i]
											+ ",serviceEndpointDesc=" + udesc);
							unregisterProxyServiceRegistration(udesc,
									proxyServiceRegistrations[i]);
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

	private void handleDiscoveredServiceAvailable(
			RemoteServiceEndpointDescription endpointDescription,
			IProgressMonitor monitor) {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		// Find IRemoteServiceContainers for the given
		// RemoteServiceEndpointDescription via registered services
		IRemoteServiceContainer[] rsContainers = findProxyRSContainers(endpointDescription);
		if (rsContainers == null || rsContainers.length == 0) {
			logWarning("handleDiscoveredServiceAvailable",
					"No local RemoteServiceContainers found for endpoint description="
							+ endpointDescription);
			return;
		}
		// Get endpoint ID
		ID endpointID = endpointDescription.getEndpointAsID();
		// Get remote service filter from the service endpoint description
		// if it exists.
		String remoteServiceFilter = endpointDescription
				.getRemoteServicesFilter();
		// For all remote service container adapters
		// Get futureRemoteReferences...then create a thread
		// to process the future
		Collection providedInterfaces = endpointDescription
				.getProvidedInterfaces();
		for (int i = 0; i < rsContainers.length; i++) {
			for (Iterator j = providedInterfaces.iterator(); j.hasNext();) {
				String providedInterface = (String) j.next();
				IRemoteServiceReference[] remoteReferences = null;
				firePreGetRemoteServiceReferences(endpointDescription,
						rsContainers[i]);
				try {
					remoteReferences = rsContainers[i].getContainerAdapter()
							.getRemoteServiceReferences(endpointID,
									providedInterface, remoteServiceFilter);
				} catch (ContainerConnectException e) {
					logError("handleDiscoveredServiceAvailable", "rsca="
							+ rsContainers[i] + ",endpointId=" + endpointID
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
									+ endpointID
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
					registerRemoteServiceReferences(endpointDescription,
							rsContainers[i], remoteReferences);
			}
		}
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
									"firePreGetRemoteServiceReferences",
									"Exception calling proxy distribution listener",
									exception);
						}

						public void run() throws Exception {
							l
									.retrievingRemoteServiceReferences(
											endpointDescription,
											remoteServiceContainer);
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
									"firePreRegister",
									"Exception calling proxy distribution listener",
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
									"firePreRegister",
									"Exception calling proxy distribution listener",
									exception);
						}

						public void run() throws Exception {
							l
									.registered(endpointDescription,
											remoteServiceContainer,
											remoteServiceReference,
											serviceRegistration);
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
									"fireUnregister",
									"Exception calling proxy distribution listener",
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
			ecfSED = (RemoteServiceEndpointDescription) Activator.getDefault()
					.getAdapterManager().loadAdapter(aServiceEndpointDesc,
							RemoteServiceEndpointDescription.class.getName());
		} else
			ecfSED = (RemoteServiceEndpointDescription) aServiceEndpointDesc;
		return ecfSED;
	}

	private boolean findProxyServiceRegistration(
			RemoteServiceEndpointDescription sed) {
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
						unregisterProxyServiceRegistration(null,
								proxyServiceRegistrations[i]);
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
			logWarning("unregisterProxyServiceRegistration",
					"Exception unregistering serviceRegistration=" + reg);
		} catch (Exception e) {
			logError("unregisterProxyServiceRegistration",
					"Exception unregistering serviceRegistration=" + reg, e);
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
				IRemoteService remoteService = remoteServiceContainer
						.getContainerAdapter().getRemoteService(
								remoteReferences[i]);
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
				Dictionary properties = getPropertiesForRemoteService(sed,
						remoteServiceContainer, remoteReferences[i],
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
					firePreRegister(sed, remoteServiceContainer,
							remoteReferences[i]);
					// Finally register
					trace("registerRemoteServiceReferences", "rsca="
							+ remoteServiceContainer + ",remoteReference="
							+ remoteReferences[i]);
					ServiceRegistration registration = Activator.getDefault()
							.getContext().registerService(clazzes, proxy,
									properties);
					IRemoteServiceReference ref = remoteReferences[i];
					ID containerID = remoteServiceContainer.getContainer()
							.getID();
					RemoteServiceRegistration reg = (RemoteServiceRegistration) discoveredRemoteServiceRegistrations
							.get(containerID);
					if (reg == null) {
						reg = new RemoteServiceRegistration(
								sed,
								remoteServiceContainer,
								new RemoteServiceReferenceUnregisteredListener());
						discoveredRemoteServiceRegistrations.put(containerID,
								reg);
					}
					reg.addServiceRegistration(ref, registration);
					// And add to distribution provider
					distributionProvider.addRemoteService(registration
							.getReference());
					trace("addLocalServiceRegistration.COMPLETE",
							"containerHelper=" + remoteServiceContainer
									+ ",remoteServiceReference=" + ref
									+ ",localServiceRegistration="
									+ registration);
					firePostRegister(sed, remoteServiceContainer,
							remoteReferences[i], registration);
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
			RemoteServiceEndpointDescription rsEndpointDescription,
			IRemoteServiceContainer rsContainer,
			IRemoteServiceReference rsReference, IRemoteService remoteService) {

		Properties result = new Properties();
		// Add the required 'service.imported' property, which for ECF rs
		// providers
		// exposes the IRemoteService
		result.put(IDistributionConstants.SERVICE_IMPORTED, remoteService);

		// Add service intents...if not null (optional property)
		String[] serviceIntents = rsEndpointDescription.getServiceIntents();
		if (serviceIntents != null)
			result.put(IDistributionConstants.SERVICE_INTENTS, serviceIntents);

		// Then add all other service properties
		String[] propKeys = rsReference.getPropertyKeys();
		for (int i = 0; i < propKeys.length; i++) {
			if (!isRemoteServiceProperty(propKeys[i])) {
				result.put(propKeys[i], rsReference.getProperty(propKeys[i]));
			}
		}
		// finally add service.imported.configs
		addImportedConfigsProperties(getContainerTypeDescription(rsContainer
				.getContainer()), rsEndpointDescription.getSupportedConfigs(),
				result);

		return result;
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
						exportedProperties.put(key, localConfigProperties
								.get(key));
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

	private IRemoteServiceContainer[] findProxyRSContainers(
			final RemoteServiceEndpointDescription rsEndpointDescription) {
		// Get activator
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		// Get finder (as service)
		IProxyContainerFinder finder = activator
				.getProxyRemoteServiceContainerFinder();
		if (finder == null) {
			logError("findRemoteServiceContainersViaService",
					"No container finders available");
			return null;
		}
		return finder.findProxyContainers(rsEndpointDescription.getServiceID(),
				rsEndpointDescription);
	}

}
