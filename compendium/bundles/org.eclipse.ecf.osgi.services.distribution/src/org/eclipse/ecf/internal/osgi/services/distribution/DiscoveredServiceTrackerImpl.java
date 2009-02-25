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
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.identity.IServiceID;
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

	private void handleDiscoveredServiceModifiedEndmatch(
			ServiceEndpointDescription description) {
		// TODO Auto-generated method stub

	}

	private void handleDiscoveredServiceModified(
			ServiceEndpointDescription description) {
		// TODO Auto-generated method stub

	}

	private void handleDiscoveredServiceUnavailable(
			ServiceEndpointDescription sed) {
		// If the service endpoint description is not ECF's then we
		// don't process it
		if (!(sed instanceof ServiceEndpointDescriptionImpl)) {
			return;
		}
		ServiceEndpointDescriptionImpl description = (ServiceEndpointDescriptionImpl) sed;

		// Get ECF discovery container ID...if not found there is a problem
		ID discoveryContainerID = description.getDiscoveryContainerID();
		if (discoveryContainerID == null) {
			logError("ServiceEndpointDescription discoveryContainerID is null",
					null);
			return;
		}
		// Get serviceName from description
		IServiceID serviceID = description.getServiceID();
		if (serviceID == null) {
			logError("ServiceEndpointDescription serviceID is null", null);
			return;
		}
		String serviceName = serviceID.getName();
		if (serviceName == null) {
			logError("ServiceEndpointDescription serviceName is null", null);
			return;
		}

		removeRemoteServiceRegistration(discoveryContainerID, serviceName);
	}

	private void handleDiscoveredServiceAvailable(ServiceEndpointDescription sed) {

		// If the service endpoint description is not ECF's then we
		// don't process it
		if (!(sed instanceof ServiceEndpointDescriptionImpl)) {
			return;
		}
		ServiceEndpointDescriptionImpl description = (ServiceEndpointDescriptionImpl) sed;

		// Get ECF discovery container ID...if not found there is a problem
		ID discoveryContainerID = description.getDiscoveryContainerID();
		if (discoveryContainerID == null) {
			logError("ServiceEndpointDescription discoveryContainerID is null",
					null);
			return;
		}
		// Get serviceName from description
		IServiceID serviceID = description.getServiceID();
		if (serviceID == null) {
			logError("ServiceEndpointDescription serviceID is null", null);
			return;
		}
		String serviceName = serviceID.getName();
		if (serviceName == null) {
			logError("ServiceEndpointDescription serviceName is null", null);
			return;
		}
		// Check that the description exposes a collection of interfaces
		Collection providedInterfaces = description.getProvidedInterfaces();
		if (providedInterfaces == null) {
			logError("ServiceEndpointDescription providedInterfaces is null",
					null);
			return;
		}
		// Find RSCAs for the given description
		IRemoteServiceContainerAdapter[] rscas = findRSCAs(description);
		if (rscas == null || rscas.length == 0) {
			logError("No RemoteServiceContainerAdapters found for description "
					+ description, null);
			return;
		}
		// Create endpointID
		ID endpointID = null;
		try {
			endpointID = createEndpointID(description);
		} catch (IDCreateException e) {
			logError("No endpoint ID created for description " + description, e);
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
				processFutureForRemoteServiceReferences(discoveryContainerID,
						serviceName, futureRemoteReferences, rscas[i],
						description, getTimeout(description));
			}
		}
	}

	long getTimeout(ServiceEndpointDescription description) {
		// for now return constant of 30s
		return 30000;
	}

	private void processFutureForRemoteServiceReferences(
			final ID discoveryContainerID, final String serviceName,
			final IFuture futureRemoteReferences,
			final IRemoteServiceContainerAdapter rsca,
			final ServiceEndpointDescription description, final long timeout) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					// First get remote service references
					trace("processFutureForRemoteServiceReferences", "future="
							+ futureRemoteReferences + " calling future.get");
					IRemoteServiceReference[] remoteReferences = (IRemoteServiceReference[]) futureRemoteReferences
							.get(timeout);
					IStatus futureStatus = futureRemoteReferences.getStatus();
					trace("processFutureForRemoteServiceReferences", "future="
							+ futureRemoteReferences + " status="
							+ futureStatus + " remoteReferences="
							+ Arrays.asList(remoteReferences));
					if (futureStatus.isOK() && remoteReferences != null
							&& remoteReferences.length > 0) {
						registerRemoteServiceReferences(discoveryContainerID,
								serviceName, rsca, remoteReferences,
								description);
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
			ID discoveryContainerID, String serviceName,
			IRemoteServiceReference reference) {
		// XXX TODO...consult serviceRegistrationMap
		return null;
	}

	private ServiceRegistration addRemoteServiceRegistration(
			ID discoveryContainerID, String serviceName,
			IRemoteServiceContainerAdapter containerAdapter,
			IRemoteServiceReference ref, ServiceRegistration registration) {

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
			ID discoveryContainerID, String serviceName) {
		synchronized (discoveredRemoteServiceRegistrations) {
			// Get Map for discoveryContainerID
			Map m = (Map) discoveredRemoteServiceRegistrations
					.get(discoveryContainerID);
			if (m == null)
				return null;
			RemoteServiceRegistration rsr = (RemoteServiceRegistration) m
					.remove(serviceName);
			if (rsr == null)
				return null;
			ServiceRegistration serviceRegistration = rsr
					.getServiceRegistration();
			IRemoteServiceReference remoteReference = rsr.getRemoteReference();
			if (rsr.getContainerAdapter().ungetRemoteService(remoteReference)) {
				trace("removeRemoteServiceRegistration", "remove discoveryID="
						+ discoveryContainerID + ",serviceName=" + serviceName);
				distributionProvider.removeExposedService(serviceRegistration
						.getReference());
				serviceRegistration.unregister();
				return rsr;
			}
		}
		return null;
	}

	private void registerRemoteServiceReferences(ID discoveryContainerID,
			String serviceName, IRemoteServiceContainerAdapter rsca,
			IRemoteServiceReference[] remoteReferences,
			ServiceEndpointDescription description) {
		for (int i = 0; i < remoteReferences.length; i++) {
			trace("registerRemoteServiceReference", "rsca=" + rsca
					+ ", remoteReference=" + remoteReferences[i]);
			// Get IRemoteService, used to create the proxy below
			IRemoteService remoteService = rsca
					.getRemoteService(remoteReferences[i]);
			if (remoteService == null) {
				logError("remote service is null for remote reference "
						+ remoteReferences[i], null);
				continue;
			}

			// Get classes to register for remote service
			String[] clazzes = (String[]) remoteReferences[i]
					.getProperty(Constants.OBJECTCLASS);
			if (clazzes == null) {
				logError("no classes specified for remote service reference "
						+ remoteReferences[i], null);
				continue;
			}

			// Get service properties for the proxy
			Dictionary properties = getPropertiesForRemoteServiceReference(
					remoteService, description);

			// Create proxy
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
				ServiceRegistration reg = getRemoteServiceRegistration(
						discoveryContainerID, serviceName, remoteReferences[i]);
				if (reg != null) {
					// log the fact that it's already registered
					logError("remote reference " + remoteReferences
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
					addRemoteServiceRegistration(discoveryContainerID,
							serviceName, rsca, remoteReferences[i],
							registration);
				} catch (Exception e) {
					logError("Error registering for remote reference "
							+ remoteReferences[i], e);
					removeRemoteServiceRegistration(discoveryContainerID,
							serviceName);
					continue;
				}
			}
		}
	}

	private Dictionary getPropertiesForRemoteServiceReference(
			IRemoteService remoteService, ServiceEndpointDescription description) {
		// TODO Auto-generated method stub
		Properties results = new Properties();
		// XXX Fill in properties from ECF
		results.put(ECFServiceConstants.OSGI_REMOTE, remoteService);
		return results;
	}

	private ID createEndpointID(ServiceEndpointDescription description)
			throws IDCreateException {
		String endpointID = description.getEndpointID();
		if (endpointID == null)
			return null;
		// Get idfilter namespace name
		String idfilterNamespaceName = (String) description
				.getProperty(Constants.SERVICE_IDFILTER_NAMESPACE);
		if (idfilterNamespaceName == null)
			throw new IDCreateException(
					"IDfilter Namespace name is not set in description "
							+ description);
		return IDFactory.getDefault().createID(idfilterNamespaceName,
				endpointID);
	}

	private IRemoteServiceContainerAdapter[] findRSCAs(
			ServiceEndpointDescription description) {
		IContainerManager containerManager = getContainerManager();
		if (containerManager == null)
			return null;
		IContainer[] containers = containerManager.getAllContainers();
		if (containers == null) {
			// log this?
			return null;
		}
		List results = new ArrayList();
		for (int i = 0; i < containers.length; i++) {
			IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) containers[i]
					.getAdapter(IRemoteServiceContainerAdapter.class);
			if (adapter != null
					&& includeRCSAForDescription(containers[i], adapter,
							description)) {
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

	private void logError(String string, Throwable t) {
		// XXX TODO
		System.err.println(string);
		if (t != null)
			t.printStackTrace(System.err);
	}

	protected void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.DISCOVEREDSERVICETRACKER,
				this.getClass(), methodName, message);
	}

	protected void traceException(String string, Throwable e) {
		Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), string, e);
	}

}
