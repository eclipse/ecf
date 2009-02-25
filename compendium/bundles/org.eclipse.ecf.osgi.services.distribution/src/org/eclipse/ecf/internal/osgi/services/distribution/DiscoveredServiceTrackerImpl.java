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

	// IRemoteServiceReference -> ServiceRegistration
	Map serviceRegistrationMap = Collections.synchronizedMap(new HashMap());

	public void serviceChanged(DiscoveredServiceNotification notification) {
		if (notification == null) {
			logError("DiscoveredServiceNotification is null", null);
			return;
		}
		trace("serviceChanged", "notification=");
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
			ServiceEndpointDescription description) {
		// TODO Auto-generated method stub

	}

	private void handleDiscoveredServiceAvailable(
			ServiceEndpointDescription description) {
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
		ID endpointID = null;
		try {
			endpointID = createEndpointID(description);
		} catch (IDCreateException e) {
			logError("No endpoint ID created for description " + description, e);
			return;
		}
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
				processFutureForRemoteServiceReferences(futureRemoteReferences,
						rscas[i], description, getTimeout(description));
			}
		}
	}

	long getTimeout(ServiceEndpointDescription description) {
		// for now return constant of 30s
		return 30000;
	}

	private void processFutureForRemoteServiceReferences(
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
						registerRemoteServiceReferences(rsca, remoteReferences,
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
			IRemoteServiceReference reference) {
		// XXX TODO...consult serviceRegistrationMap
		return null;
	}

	private ServiceRegistration addRemoteServiceRegistration(
			IRemoteServiceReference ref, ServiceRegistration registration) {
		// XXX TODO...add to serviceRegistrationMap
		return null;
	}

	private void registerRemoteServiceReferences(
			IRemoteServiceContainerAdapter rsca,
			IRemoteServiceReference[] remoteReferences,
			ServiceEndpointDescription description) {
		// First, get IRemoteService instances for remote references
		for (int i = 0; i < remoteReferences.length; i++) {
			trace("registerRemoteServiceReference", "rsca=" + rsca
					+ ", remoteReference=" + remoteReferences[i]);
			// Otherwise we register it.
			IRemoteService remoteService = rsca
					.getRemoteService(remoteReferences[i]);
			if (remoteService == null) {
				logError("remote service is null for remote reference "
						+ remoteReferences[i], null);
				continue;
			}
			String[] clazzes = (String[]) remoteReferences[i]
					.getProperty(Constants.OBJECTCLASS);
			if (clazzes == null) {
				logError("no classes specified for remote service reference "
						+ remoteReferences[i], null);
				continue;
			}
			Dictionary properties = getPropertiesForRemoteServiceReference(
					remoteService, description);
			Object proxy = null;
			try {
				proxy = remoteService.getProxy();
			} catch (ECFException e) {
				logError(
						"Exception creating proxy for remote service reference "
								+ remoteReferences[i], e);
				continue;
			}
			BundleContext bundleContext = Activator.getDefault().getContext();
			// Has to be synchronized on map so that additions do not occur
			// while this is going on
			synchronized (serviceRegistrationMap) {
				// First check to see if remote reference is already registered
				ServiceRegistration reg = getRemoteServiceRegistration(remoteReferences[i]);
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
					addRemoteServiceRegistration(remoteReferences[i],
							registration);
				} catch (Exception e) {
					logError("Error registering for remote reference "
							+ remoteReferences[i], e);
					removeRemoteServiceRegistration(remoteReferences[i]);
					continue;
				}
			}
		}
	}

	private void removeRemoteServiceRegistration(
			IRemoteServiceReference iRemoteServiceReference) {
		// TODO Auto-generated method stub

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
