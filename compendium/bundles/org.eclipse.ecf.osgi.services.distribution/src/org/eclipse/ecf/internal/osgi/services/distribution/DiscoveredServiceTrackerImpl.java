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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.osgi.services.discovery.ECFServicePublication;
import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescriptionImpl;
import org.eclipse.ecf.osgi.services.distribution.ECFServiceConstants;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceUnregisteredEvent;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.TimeoutException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.discovery.*;

public class DiscoveredServiceTrackerImpl implements DiscoveredServiceTracker {

	DistributionProviderImpl distributionProvider;

	public DiscoveredServiceTrackerImpl(DistributionProviderImpl dp) {
		this.distributionProvider = dp;
	}

	// <Map<containerID><RemoteServiceRegistration>

	Map discoveredRemoteServiceRegistrations = Collections
			.synchronizedMap(new HashMap());

	public void serviceChanged(DiscoveredServiceNotification notification) {
		if (notification == null) {
			logError("DiscoveredServiceNotification is null", null);
			return;
		}
		int notificationType = notification.getType();
		switch (notificationType) {
		case DiscoveredServiceNotification.AVAILABLE:
			handleDiscoveredServiceAvailable(notification
					.getServiceEndpointDescription());
			break;
		case DiscoveredServiceNotification.UNAVAILABLE:
			handleDiscoveredServiceUnavailable(notification
					.getServiceEndpointDescription());
			break;
		case DiscoveredServiceNotification.MODIFIED:
			handleDiscoveredServiceModified(notification
					.getServiceEndpointDescription());
			break;
		case DiscoveredServiceNotification.MODIFIED_ENDMATCH:
			handleDiscoveredServiceModifiedEndmatch(notification
					.getServiceEndpointDescription());
			break;
		default:
			logError("DiscoveredServiceNotification type=" + notificationType
					+ " not found", null);
			break;
		}
	}

	private IContainerManager getContainerManager() {
		return Activator.getDefault().getContainerManager();
	}

	private void handleDiscoveredServiceUnavailable(
			ServiceEndpointDescription sed) {
		// If the service endpoint description is not ECF's then we
		// don't process it
		if (!(sed instanceof ServiceEndpointDescriptionImpl)) {
			return;
		}
	}

	private void handleDiscoveredServiceAvailable(ServiceEndpointDescription sed) {

		// If the service endpoint description is not ECF's then we
		// don't process it
		if (!(sed instanceof ServiceEndpointDescriptionImpl)) {
			return;
		}
		ServiceEndpointDescriptionHelper sedh = null;
		// Now create a ServiceEndpointDescriptionHelper from sed
		Collection providedInterfaces = null;
		ID endpointID = null;
		try {
			sedh = new ServiceEndpointDescriptionHelper(
					(ServiceEndpointDescriptionImpl) sed);
			providedInterfaces = sedh.getProvidedInterfaces();
			endpointID = sedh.getEndpointID();
		} catch (Exception e) {
			logError("Error getting data from ServiceEndpointDescription="
					+ sedh, e);
			return;
		}

		// Find RSCAs for the given description
		ContainerHelper[] chs = findRSCAs(endpointID, sedh);
		if (chs == null || chs.length == 0) {
			logError("No RemoteServiceContainerAdapters found for description="
					+ sedh.getDescription(), null);
			return;
		}
		// For all remote service container adapters
		// Get futureRemoteReferences...then create a thread
		// to process the future
		for (int i = 0; i < chs.length; i++) {
			for (Iterator j = providedInterfaces.iterator(); j.hasNext();) {
				String providedInterface = (String) j.next();
				// Use async call to prevent blocking here
				trace("handleDiscoveredServiceAvailable", "rsca=" + chs[i]
						+ ",intf=" + providedInterface);
				IFuture futureRemoteReferences = chs[i].getRSCA()
						.asyncGetRemoteServiceReferences(
								new ID[] { endpointID }, providedInterface,
								null);
				// And process the future returned in separate thread
				processFutureForRemoteServiceReferences(sedh,
						futureRemoteReferences, chs[i]);
			}
		}
	}

	private void processFutureForRemoteServiceReferences(
			final ServiceEndpointDescriptionHelper sedh,
			final IFuture futureRemoteReferences, final ContainerHelper ch) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					IRemoteServiceReference[] remoteReferences = (IRemoteServiceReference[]) futureRemoteReferences
							.get(sedh.getFutureTimeout());
					IStatus futureStatus = futureRemoteReferences.getStatus();
					if (futureStatus.isOK()) {
						trace(
								"processFutureForRemoteServiceReferences.run",
								"containerHelper="
										+ ch
										+ "remoteReferences="
										+ ((remoteReferences == null) ? "null"
												: Arrays
														.asList(remoteReferences)));
						if (remoteReferences != null) {
							registerRemoteServiceReferences(sedh, ch,
									remoteReferences);
						}
					} else {
						logError("Future status not ok: "
								+ futureStatus.getMessage(), futureStatus
								.getException());
					}
				} catch (InterruptedException e) {
					logError("Retrieval of remote references interrupted", e);
				} catch (OperationCanceledException e) {
					logError("Retrieval of remote references cancelled", e);
				} catch (TimeoutException e) {
					logError("Retrieval of remote references timedout after "
							+ e.getDuration(), e);
				}
			}
		});
		t.start();
	}

	private void addLocalServiceRegistration(ContainerHelper ch,
			IRemoteServiceReference ref, ServiceRegistration registration) {

		synchronized (discoveredRemoteServiceRegistrations) {
			ID containerID = ch.getContainer().getID();
			RemoteServiceRegistrations reg = (RemoteServiceRegistrations) discoveredRemoteServiceRegistrations
					.get(containerID);
			if (reg == null) {
				reg = new RemoteServiceRegistrations(ch.getContainer(), ch
						.getRSCA(),
						new RemoteServiceReferenceUnregisteredListener());
				discoveredRemoteServiceRegistrations.put(containerID, reg);
			}
			reg.addServiceRegistration(ref, registration);
			trace("addLocalServiceRegistration", "containerHelper=" + ch
					+ ",remoteServiceReference=" + ref
					+ ",localServiceRegistration=" + registration);
			// And add to distribution provider
			distributionProvider.addRemoteService(registration.getReference());
		}
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
				ServiceRegistration[] registrations = null;
				synchronized (discoveredRemoteServiceRegistrations) {
					RemoteServiceRegistrations reg = (RemoteServiceRegistrations) discoveredRemoteServiceRegistrations
							.get(event.getLocalContainerID());
					if (reg != null) {
						registrations = reg.removeServiceRegistration(event
								.getReference());
						if (reg.isEmpty()) {
							reg.dispose();
							discoveredRemoteServiceRegistrations.remove(event
									.getContainerID());
						}
					}
				}
				// Call this outside of synchronized block
				if (registrations != null) {
					for (int i = 0; i < registrations.length; i++) {
						try {
							trace(
									"handleRemoteServiceUnregisteredEvent.unregister",
									"localContainerID="
											+ event.getLocalContainerID()
											+ ",containerID="
											+ event.getContainerID()
											+ ",remoteReference="
											+ event.getReference()
											+ ",registration="
											+ registrations[i]);
							registrations[i].unregister();
						} catch (Exception e) {
							logError(
									"Exception unregistering service registration="
											+ registrations[i], e);
						}
					}
				}
			}
		}
	}

	private void registerRemoteServiceReferences(
			ServiceEndpointDescriptionHelper sedh, ContainerHelper ch,
			IRemoteServiceReference[] remoteReferences) {
		for (int i = 0; i < remoteReferences.length; i++) {
			// Get IRemoteService, used to create the proxy below
			IRemoteService remoteService = ch.getRSCA().getRemoteService(
					remoteReferences[i]);
			if (remoteService == null) {
				logError("Remote service is null for remote reference "
						+ remoteReferences[i], null);
				continue;
			}

			// Get classes to register for remote service
			String[] clazzes = (String[]) remoteReferences[i]
					.getProperty(Constants.OBJECTCLASS);
			if (clazzes == null) {
				logError("No classes specified for remote service reference "
						+ remoteReferences[i], null);
				continue;
			}

			// Get service properties for the proxy
			Dictionary properties = getPropertiesForRemoteService(sedh, ch
					.getRSCA(), remoteReferences[i], remoteService);

			// Create proxy right here
			Object proxy = null;
			try {
				proxy = remoteService.getProxy();
			} catch (ECFException e) {
				logError(
						"Exception creating proxy for remote service reference "
								+ remoteReferences[i], e);
				continue;
			}

			// Has to be synchronized on map so that additions do not occur
			// while this is going on
			synchronized (discoveredRemoteServiceRegistrations) {
				try {
					// Finally register
					trace("registerRemoteServiceReferences", "rsca=" + ch
							+ ",remoteReference=" + remoteReferences[i]);
					ServiceRegistration registration = Activator.getDefault()
							.getContext().registerService(clazzes, proxy,
									properties);
					addLocalServiceRegistration(ch, remoteReferences[i],
							registration);
				} catch (Exception e) {
					logError("Error registering for remote reference "
							+ remoteReferences[i], e);
					continue;
				}
			}
		}
	}

	List ecfRemoteServiceProperties = Arrays.asList(new String[] {
			Constants.SERVICE_ID, Constants.OBJECTCLASS,
			ECFServicePublication.PROP_KEY_ENDPOINT_ID,
			ECFServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME,
			ECFServicePublication.PROP_KEY_ENDPOINT_LOCATION,
			ECFServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
			ECFServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION,
			ECFServicePublication.PROP_KEY_SERVICE_PROPERTIES });

	private boolean isRemoteServiceProperty(String propertyKey) {
		return ecfRemoteServiceProperties.contains(propertyKey);
	}

	private Dictionary getPropertiesForRemoteService(
			ServiceEndpointDescriptionHelper description,
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
		results.put(ECFServiceConstants.OSGI_REMOTE, remoteService);
		return results;
	}

	class ContainerHelper {
		private IContainer container;
		private IRemoteServiceContainerAdapter containerAdapter;

		public ContainerHelper(IContainer c, IRemoteServiceContainerAdapter rsca) {
			this.container = c;
			this.containerAdapter = rsca;
		}

		public IContainer getContainer() {
			return container;
		}

		public IRemoteServiceContainerAdapter getRSCA() {
			return containerAdapter;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer("ContainerHelper[");
			buf.append("containerID=").append(getContainer().getID());
			buf.append(";rsca=").append(getRSCA()).append("]");
			return buf.toString();
		}
	}

	private ContainerHelper[] findRSCAs(ID endpointID,
			ServiceEndpointDescriptionHelper sedh) {
		IContainerManager containerManager = getContainerManager();
		if (containerManager == null)
			return null;
		IContainer[] containers = containerManager.getAllContainers();
		if (containers == null) {
			// log this?
			logWarning("findRSCAs", "No containers found for container manager");
			return new ContainerHelper[0];
		}
		List results = new ArrayList();
		for (int i = 0; i < containers.length; i++) {
			IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) containers[i]
					.getAdapter(IRemoteServiceContainerAdapter.class);
			if (adapter != null
					&& includeRCSAForDescription(containers[i], adapter,
							endpointID, sedh.getDescription())) {
				results.add(new ContainerHelper(containers[i], adapter));
			}
		}
		return (ContainerHelper[]) results.toArray(new ContainerHelper[] {});
	}

	private boolean includeRCSAForDescription(IContainer container,
			IRemoteServiceContainerAdapter adapter, ID endpointID,
			ServiceEndpointDescription description) {
		// First we exclude the container where the discovered service is from
		if (endpointID.equals(container.getID()))
			return false;
		// Then we check the connect ID namespace. If it's the same as the
		// container's namespace
		// then we've found one
		String connectNamespaceName = (String) description
				.getProperty(Constants.SERVICE_CONNECT_ID_NAMESPACE);
		if (connectNamespaceName != null) {
			Namespace namespace = container.getConnectNamespace();
			if (namespace != null
					&& namespace.getName().equals(connectNamespaceName))
				return true;
		}
		return true;
	}

	private void handleDiscoveredServiceModifiedEndmatch(
			ServiceEndpointDescription description) {
		// TODO Auto-generated method stub
	}

	private void handleDiscoveredServiceModified(
			ServiceEndpointDescription description) {
		// TODO Auto-generated method stub
	}

	private void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.DISCOVEREDSERVICETRACKER,
				this.getClass(), methodName, message);
	}

	private void logWarning(String method, String message) {
		// XXX TODO log proper warning
	}

	private void logError(String string, Throwable t) {
		// XXX TODO log proper error
		System.err.println(string);
		if (t != null)
			t.printStackTrace(System.err);
	}

}
