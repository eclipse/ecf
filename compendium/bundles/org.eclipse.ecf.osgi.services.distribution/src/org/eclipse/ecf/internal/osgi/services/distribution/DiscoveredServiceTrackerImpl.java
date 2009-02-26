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
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.TimeoutException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.discovery.*;

public class DiscoveredServiceTrackerImpl implements DiscoveredServiceTracker {

	DistributionProviderImpl distributionProvider;

	public DiscoveredServiceTrackerImpl(DistributionProviderImpl dp) {
		this.distributionProvider = dp;
	}

	// Map<ID(discovery
	// container)><Map<serviceName><RemoteServiceRegistration(IRemoteServiceRegistration,ServiceRegistration)>

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
		ServiceEndpointDescriptionHelper sedh = null;
		// Now create a ServiceEndpointDescriptionHelper from sed
		try {
			sedh = new ServiceEndpointDescriptionHelper(
					(ServiceEndpointDescriptionImpl) sed);
		} catch (NullPointerException e) {
			logError("Error getting data from ServiceEndpointDescription", e);
			return;
		}
		removeRemoteServiceRegistration(sedh);
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
			logError("Error getting data from ServiceEndpointDescription", e);
			return;
		}

		// Find RSCAs for the given description
		IRemoteServiceContainerAdapter[] rscas = findRSCAs(sedh);
		if (rscas == null || rscas.length == 0) {
			logError("No RemoteServiceContainerAdapters found for description "
					+ sedh.getDescription(), null);
			return;
		}
		// For all remote service container adapters
		// Get futureRemoteReferences...then create a thread
		// to process the future
		for (int i = 0; i < rscas.length; i++) {
			for (Iterator j = providedInterfaces.iterator(); j.hasNext();) {
				String providedInterface = (String) j.next();
				// Use async call to prevent blocking here
				trace("handleDiscoveredServiceAvailable", "rscas=" + rscas[i]
						+ ", calling asyncGetRemoteServiceReferences endpoint="
						+ endpointID + ",intf=" + providedInterface);
				IFuture futureRemoteReferences = rscas[i]
						.asyncGetRemoteServiceReferences(
								new ID[] { endpointID }, providedInterface,
								null);
				// And process the future returned in separate thread
				processFutureForRemoteServiceReferences(sedh,
						futureRemoteReferences, rscas[i]);
			}
		}
	}

	private void processFutureForRemoteServiceReferences(
			final ServiceEndpointDescriptionHelper sedh,
			final IFuture futureRemoteReferences,
			final IRemoteServiceContainerAdapter rsca) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					// First get remote service references
					trace("processFutureForRemoteServiceReferences", "future="
							+ futureRemoteReferences + " calling future.get");
					IRemoteServiceReference[] remoteReferences = (IRemoteServiceReference[]) futureRemoteReferences
							.get(sedh.getFutureTimeout());
					IStatus futureStatus = futureRemoteReferences.getStatus();
					trace("processFutureForRemoteServiceReferences", "future="
							+ futureRemoteReferences + " status="
							+ futureStatus + " remoteReferences="
							+ Arrays.asList(remoteReferences));
					if (futureStatus.isOK() && remoteReferences != null
							&& remoteReferences.length > 0) {
						registerRemoteServiceReferences(sedh, rsca,
								remoteReferences);
					} else {
						logFutureError(futureStatus);
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

	void logFutureError(IStatus futureStatus) {
		logError("Future error: " + futureStatus.getMessage(), futureStatus
				.getException());
	}

	private ServiceRegistration getRemoteServiceRegistration(
			ServiceEndpointDescriptionHelper sedh,
			IRemoteServiceReference reference) {
		ID discoveryContainerID = sedh.getDiscoveryContainerID();
		String serviceName = sedh.getServiceName();
		synchronized (discoveredRemoteServiceRegistrations) {
			Map m = (Map) discoveredRemoteServiceRegistrations
					.get(discoveryContainerID);
			if (m == null)
				return null;
			// Now look up serviceName
			RemoteServiceRegistration rsr = (RemoteServiceRegistration) m
					.get(serviceName);
			return (rsr == null) ? null : rsr.getServiceRegistration();
		}
	}

	private ServiceRegistration addRemoteServiceRegistration(
			ServiceEndpointDescriptionHelper sedh,
			IRemoteServiceContainerAdapter containerAdapter,
			IRemoteServiceReference ref, ServiceRegistration registration) {

		ID discoveryContainerID = sedh.getDiscoveryContainerID();
		String serviceName = sedh.getServiceName();
		synchronized (discoveredRemoteServiceRegistrations) {
			// Get Map for discoveryContainerID
			Map m = (Map) discoveredRemoteServiceRegistrations
					.get(discoveryContainerID);
			if (m == null) {
				m = new HashMap();
				discoveredRemoteServiceRegistrations.put(discoveryContainerID,
						m);
			}
			// Now look up serviceName
			RemoteServiceRegistration rsr = (RemoteServiceRegistration) m
					.get(serviceName);
			if (rsr == null) {
				rsr = new RemoteServiceRegistration(containerAdapter, ref,
						registration);
				m.put(serviceName, rsr);
				trace("addRemoteServiceRegistration", "adding discoveryID="
						+ discoveryContainerID + ",serviceName=" + serviceName);
				// And add to distribution provider
				distributionProvider.addRemoteService(registration
						.getReference());
				return registration;
			}
		}
		return null;
	}

	private RemoteServiceRegistration removeRemoteServiceRegistration(
			ServiceEndpointDescriptionHelper sedh) {
		synchronized (discoveredRemoteServiceRegistrations) {
			// Get Map for discoveryContainerID
			Map m = (Map) discoveredRemoteServiceRegistrations.get(sedh
					.getDiscoveryContainerID());
			if (m == null)
				return null;
			RemoteServiceRegistration rsr = (RemoteServiceRegistration) m
					.remove(sedh.getServiceName());
			if (rsr == null)
				return null;
			ServiceRegistration serviceRegistration = rsr
					.getServiceRegistration();
			IRemoteServiceReference remoteReference = rsr.getRemoteReference();
			if (rsr.getContainerAdapter().ungetRemoteService(remoteReference)) {
				trace("removeRemoteServiceRegistration", "remove discoveryID="
						+ sedh.getDiscoveryContainerID() + ",serviceName="
						+ sedh.getServiceName());
				distributionProvider.removeExposedService(serviceRegistration
						.getReference());
				serviceRegistration.unregister();
				return rsr;
			}
		}
		return null;
	}

	private void registerRemoteServiceReferences(
			ServiceEndpointDescriptionHelper sedh,
			IRemoteServiceContainerAdapter rsca,
			IRemoteServiceReference[] remoteReferences) {
		for (int i = 0; i < remoteReferences.length; i++) {
			trace("registerRemoteServiceReference", "rsca=" + rsca
					+ ", remoteReference=" + remoteReferences[i]);
			// Get IRemoteService, used to create the proxy below
			IRemoteService remoteService = rsca
					.getRemoteService(remoteReferences[i]);
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
			Dictionary properties = getPropertiesForRemoteService(sedh, rsca,
					remoteReferences[i], remoteService);

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

			// Get bundle context
			BundleContext bundleContext = Activator.getDefault().getContext();
			// Has to be synchronized on map so that additions do not occur
			// while this is going on
			synchronized (discoveredRemoteServiceRegistrations) {
				// First check to see if remote reference is already registered
				ServiceRegistration reg = getRemoteServiceRegistration(sedh,
						remoteReferences[i]);
				if (reg != null) {
					// log the fact that it's already registered
					logError("remote reference " + remoteReferences[i]
							+ " already registered locally", null);
					continue;
				}
				ServiceRegistration registration = null;
				try {
					// Finally register
					trace("registerRemoteServiceReferences",
							"registering classes=" + Arrays.asList(clazzes)
									+ ",properties=" + properties);
					registration = bundleContext.registerService(clazzes,
							proxy, properties);
					addRemoteServiceRegistration(sedh, rsca,
							remoteReferences[i], registration);
				} catch (Exception e) {
					logError("Error registering for remote reference "
							+ remoteReferences[i], e);
					removeRemoteServiceRegistration(sedh);
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
		System.out.println("properties for remote service proxy=" + results);
		return results;
	}

	private IRemoteServiceContainerAdapter[] findRSCAs(
			ServiceEndpointDescriptionHelper sedh) {
		IContainerManager containerManager = getContainerManager();
		if (containerManager == null)
			return null;
		IContainer[] containers = containerManager.getAllContainers();
		if (containers == null) {
			// log this?
			logWarning("findRSCAs", "No containers found for container manager");
			return new IRemoteServiceContainerAdapter[0];
		}
		List results = new ArrayList();
		for (int i = 0; i < containers.length; i++) {
			IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) containers[i]
					.getAdapter(IRemoteServiceContainerAdapter.class);
			if (adapter != null
					&& includeRCSAForDescription(containers[i], adapter, sedh
							.getDescription())) {
				results.add(adapter);
			}
		}
		return (IRemoteServiceContainerAdapter[]) results
				.toArray(new IRemoteServiceContainerAdapter[] {});
	}

	private boolean includeRCSAForDescription(IContainer container,
			IRemoteServiceContainerAdapter adapter,
			ServiceEndpointDescription description) {
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
