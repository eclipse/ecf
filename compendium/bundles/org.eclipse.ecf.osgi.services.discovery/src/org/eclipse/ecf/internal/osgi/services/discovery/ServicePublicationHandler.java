/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.discovery;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ECFRuntimeException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceTracker;
import org.eclipse.ecf.osgi.services.discovery.IHostDiscoveryListener;
import org.eclipse.ecf.osgi.services.discovery.IProxyDiscoveryListener;
import org.eclipse.ecf.osgi.services.discovery.RemoteServicePublication;
import org.eclipse.ecf.osgi.services.discovery.ServicePublication;
import org.eclipse.ecf.remoteservice.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServicePublicationHandler implements ServiceTrackerCustomizer,
		IServiceListener {

	private static final String DISCOVERY_IDENTIFIER = "org.eclipse.ecf.internal.discovery.id"; //$NON-NLS-1$
	private static final boolean allowLoopbackReferences = Boolean
			.getBoolean("org.eclipse.ecf.osgi.services.discovery.allowLoopbackReference"); //$NON-NLS-1$
	private final byte[] guid;

	private IDiscoveryAdvertiser advertiser;
	private final Map serviceInfos = Collections.synchronizedMap(new HashMap());

	public ServicePublicationHandler(byte[] aGuid) {
		guid = aGuid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.discovery.IServiceListener#serviceDiscovered(org.eclipse
	 * .ecf.discovery.IServiceEvent)
	 */
	public void serviceDiscovered(IServiceEvent event) {
		final IServiceInfo serviceInfo = event.getServiceInfo();
		final IServiceID serviceID = serviceInfo.getServiceID();
		trace("handleOSGIServiceDiscovered", " serviceInfo=" + serviceInfo); //$NON-NLS-1$ //$NON-NLS-2$
		if (matchServiceID(serviceID)) {
			if (isLoopbackReference(serviceInfo)) {
				logInfo("serviceDiscovered", //$NON-NLS-1$
						"ignoring loopback ServiceReference: " //$NON-NLS-1$
								+ serviceID, null);
				return;
			}
			fireProxyDiscoveredUndiscovered(serviceInfo, true);
			trace("handleOSGIServiceDiscovered matched", " serviceInfo=" + serviceInfo); //$NON-NLS-1$ //$NON-NLS-2$
			DiscoveredServiceTracker[] discoveredTrackers = findMatchingDiscoveredServiceTrackers(serviceInfo);
			notifyDiscoveredServiceTrackers(discoveredTrackers, serviceInfo,
					true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.discovery.IServiceListener#serviceUndiscovered(org.eclipse
	 * .ecf.discovery.IServiceEvent)
	 */
	public void serviceUndiscovered(IServiceEvent event) {
		final IServiceInfo serviceInfo = event.getServiceInfo();
		final IServiceID serviceID = serviceInfo.getServiceID();
		if (matchServiceID(serviceID)) {
			if (isLoopbackReference(serviceInfo)) {
				logInfo("serviceUndiscovered", //$NON-NLS-1$
						"ignoring loopback ServiceReference: " //$NON-NLS-1$
								+ serviceID, null);
				return;
			}
			fireProxyDiscoveredUndiscovered(serviceInfo, false);
			trace("handleOSGIServiceUndiscovered", " serviceInfo=" + serviceInfo); //$NON-NLS-1$ //$NON-NLS-2$
			DiscoveredServiceTracker[] discoveredTrackers = findMatchingDiscoveredServiceTrackers(serviceInfo);
			notifyDiscoveredServiceTrackers(discoveredTrackers, serviceInfo,
					false);
		}
	}

	private void notifyDiscoveredServiceTrackers(
			DiscoveredServiceTracker[] discoveredTrackers,
			IServiceInfo serviceInfo, boolean available) {
		if (discoveredTrackers != null) {
			for (int i = 0; i < discoveredTrackers.length; i++) {
				discoveredTrackers[i]
						.serviceChanged(new DiscoveredServiceNotificationImpl(
								(available ? DiscoveredServiceNotification.AVAILABLE
										: DiscoveredServiceNotification.UNAVAILABLE),
								serviceInfo));
			}
		}
	}

	private DiscoveredServiceTracker[] findMatchingDiscoveredServiceTrackers(
			IServiceInfo serviceInfo) {
		ServiceReference[] sourceTrackers = Activator.getDefault()
				.getDiscoveredServiceTrackerReferences();
		if (sourceTrackers == null)
			return null;
		List matchingTrackers = new ArrayList();
		for (int i = 0; i < sourceTrackers.length; i++) {
			if (matchWithDiscoveredServiceInfo(sourceTrackers[i], serviceInfo))
				matchingTrackers.add(Activator.getDefault().getContext()
						.getService(sourceTrackers[i]));
		}
		return (DiscoveredServiceTracker[]) matchingTrackers
				.toArray(new DiscoveredServiceTracker[] {});
	}

	private boolean matchWithDiscoveredServiceInfo(
			ServiceReference serviceReference, IServiceInfo serviceInfo) {
		// XXX for now match everything. See RFC119
		return true;
	}

	private boolean matchServiceID(IServiceID serviceId) {
		if (Arrays.asList(serviceId.getServiceTypeID().getServices()).contains(
				RemoteServicePublication.SERVICE_TYPE))
			return true;
		return false;
	}

	// returns true if the given service has been registered by this very
	// ServicePublicationHandler (same instance)
	private boolean isLoopbackReference(final IServiceInfo serviceInfo) {
		if (allowLoopbackReferences) {
			return false;
		}
		final byte[] aGuid = serviceInfo.getServiceProperties()
				.getPropertyBytes(DISCOVERY_IDENTIFIER);
		// backward compatibility
		if (aGuid == null) {
			return false;
		}
		return Arrays.equals(guid, aGuid);
	}

	private IServiceInfo addServiceInfo(ServiceReference sr, IServiceInfo si) {
		return (IServiceInfo) serviceInfos.put(sr, si);
	}

	private IServiceInfo removeServiceInfo(ServiceReference sr) {
		return (IServiceInfo) serviceInfos.remove(sr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi
	 * .framework.ServiceReference)
	 */
	public Object addingService(ServiceReference reference) {
		handleServicePublication(reference);
		return Activator.getDefault().getContext().getService(reference);
	}

	/**
	 * @param reference
	 */
	private void handleServicePublication(ServiceReference reference) {

		// Get required service RFC 119 property "service.interface", which
		// should be a
		// Collection of Strings
		Collection svcInterfaces = ServicePropertyUtils.getCollectionProperty(
				reference, ServicePublication.SERVICE_INTERFACE_NAME);
		// If it's not there, then we ignore this ServicePublication and return
		if (svcInterfaces == null) {
			logError(
					"handleServicePublication", //$NON-NLS-1$
					"ignoring " //$NON-NLS-1$
							+ reference
							+ ". ServicePublication.SERVICE_INTERFACE_NAME not set", //$NON-NLS-1$
					null);
			return;
		}
		IServiceProperties discoveryServiceProperties = new ServiceProperties();

		// set this identifier to filter out loobacks
		discoveryServiceProperties.setPropertyBytes(DISCOVERY_IDENTIFIER, guid);

		discoveryServiceProperties.setPropertyString(
				ServicePublication.SERVICE_INTERFACE_NAME,
				ServicePropertyUtils.createStringFromCollection(svcInterfaces));

		Collection configTypes = ServicePropertyUtils.getCollectionProperty(
				reference, RemoteServicePublication.ENDPOINT_SUPPORTED_CONFIGS);
		if (configTypes != null) {
			discoveryServiceProperties.setPropertyString(
					RemoteServicePublication.ENDPOINT_SUPPORTED_CONFIGS,
					ServicePropertyUtils
							.createStringFromCollection(configTypes));
		}

		Collection serviceIntents = ServicePropertyUtils.getCollectionProperty(
				reference, RemoteServicePublication.ENDPOINT_SERVICE_INTENTS);
		if (serviceIntents != null) {
			discoveryServiceProperties.setPropertyString(
					RemoteServicePublication.ENDPOINT_SERVICE_INTENTS,
					ServicePropertyUtils
							.createStringFromCollection(serviceIntents));
		}

		// We also use the optional RFC 119 property PROP_KEY_SERVICE_PROPERTIES
		Map servicePublicationServiceProperties = ServicePropertyUtils
				.getMapProperty(reference,
						ServicePublication.SERVICE_PROPERTIES);
		// Add them
		if (servicePublicationServiceProperties != null)
			addPropertiesToDiscoveryServiceProperties(
					discoveryServiceProperties,
					servicePublicationServiceProperties);

		// See EventHookImpl.getServicePublicationProperties()
		// Get and then serialize and set
		// RemoteServicePublication.ENDPOINT_CONTAINERID
		ID endpointContainerID = (ID) reference
				.getProperty(RemoteServicePublication.ENDPOINT_CONTAINERID);
		// This is required for ecf endpoints so if it's not found then it's an
		// error
		if (endpointContainerID == null) {
			logError(
					"handleServicePublication", //$NON-NLS-1$
					"ignoring " //$NON-NLS-1$
							+ reference
							+ ". RemoteServicePublication.ENDPOINT_CONTAINERID not set", //$NON-NLS-1$
					null);
			return;
		}
		// Add endpoint container id.toExternalForm().getBytes...so AS byte []
		discoveryServiceProperties.setPropertyBytes(
				RemoteServicePublication.ENDPOINT_CONTAINERID,
				endpointContainerID.toExternalForm().getBytes());
		// Add endpoint container id namespace name
		String endpointNamespace = endpointContainerID.getNamespace().getName();
		discoveryServiceProperties.setPropertyString(
				RemoteServicePublication.ENDPOINT_CONTAINERID_NAMESPACE,
				endpointNamespace);

		// See EventHookImpl.getServicePublicationProperties()
		// Get and then serialize and set
		// RemoteServicePublication.TARGET_CONTAINERID
		ID targetContainerID = (ID) reference
				.getProperty(RemoteServicePublication.TARGET_CONTAINERID);
		// the target ID is optional, so only add it if it's been added
		// via the EventHookImpl
		if (targetContainerID != null) {
			// Add endpoint container id.toExternalForm().getBytes...so AS byte
			// []
			discoveryServiceProperties.setPropertyBytes(
					RemoteServicePublication.TARGET_CONTAINERID,
					targetContainerID.toExternalForm().getBytes());
			String targetNamespace = targetContainerID.getNamespace().getName();
			discoveryServiceProperties.setPropertyString(
					RemoteServicePublication.TARGET_CONTAINERID_NAMESPACE,
					targetNamespace);
		}

		// add remote service namespace
		String rsnamespace = ServicePropertyUtils.getStringProperty(reference,
				Constants.SERVICE_NAMESPACE);
		if (rsnamespace != null)
			discoveryServiceProperties.setPropertyString(
					Constants.SERVICE_NAMESPACE, rsnamespace);

		// and remote service id
		final byte[] remoteServiceIDAsBytes = (byte[]) reference
				.getProperty(Constants.SERVICE_ID);
		if (remoteServiceIDAsBytes == null) {
			logInfo("handleServicePublication", //$NON-NLS-1$
					"ignoring " //$NON-NLS-1$
							+ reference
							+ ". No " + Constants.SERVICE_ID + " property set on ServiceReference", //$NON-NLS-1$ //$NON-NLS-2$
					null);
			return;
		}
		discoveryServiceProperties.setPropertyBytes(Constants.SERVICE_ID,
				remoteServiceIDAsBytes);

		IDiscoveryAdvertiser advertiser2 = getAdvertiser();
		if (advertiser2 == null) {
			logInfo("handleServicePublication", //$NON-NLS-1$
					"ignoring " //$NON-NLS-1$
							+ reference
							+ ". No IDiscoveryAdvertiser available to handle this publication", //$NON-NLS-1$
					null);
			return;
		}
		Namespace advertiserNamespace = advertiser2.getServicesNamespace();
		IServiceInfo svcInfo = null;
		try {
			// Create service type ID
			IServiceTypeID serviceTypeID = createServiceTypeID(
					servicePublicationServiceProperties, advertiserNamespace);
			// First create unique default name
			String defaultServiceName = createUniqueDefaultServiceName();
			// Allow value of service property 'ecf.sp.svcname' to override
			// default
			String serviceName = getPropertyWithDefault(
					servicePublicationServiceProperties,
					RemoteServicePublication.SERVICE_NAME, defaultServiceName);
			// Create service URI
			URI uri = createURI(endpointContainerID, "/" + serviceName); //$NON-NLS-1$
			// Create service info...in preparation for publication
			svcInfo = new ServiceInfo(uri, serviceName, serviceTypeID,
					discoveryServiceProperties);

		} catch (IDCreateException e) {
			logError("handleServicePublication", //$NON-NLS-1$
					"Exception creating serviceID", e); //$NON-NLS-1$
			return;
		} catch (URISyntaxException e) {
			logError("handleServicePublication", "Exception creating URI", e); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		fireHostPublishUnpublish(reference, svcInfo, true);

		synchronized (serviceInfos) {
			try {
				trace("publishService", "publishing serviceReference=" //$NON-NLS-1$ //$NON-NLS-2$
						+ reference + ", svcInfo=" + svcInfo); //$NON-NLS-1$
				advertiser2.registerService(svcInfo);
				addServiceInfo(reference, svcInfo);
			} catch (ECFRuntimeException e) {
				logError("publishService", "cannot register service", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	private String createUniqueDefaultServiceName() {
		return RemoteServicePublication.DEFAULT_SERVICE_NAME_PREFIX
				+ IDFactory.getDefault().createGUID().getName();
	}

	private void fireHostPublishUnpublish(final ServiceReference reference,
			final IServiceInfo serviceInfo, final boolean publish) {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			IHostDiscoveryListener[] listeners = activator
					.getHostPublicationListeners();
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					final IHostDiscoveryListener l = listeners[i];
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							logError(
									"fireHostPublishUnpublish", //$NON-NLS-1$
									"Exception calling host discovery listener", //$NON-NLS-1$
									exception);
						}

						public void run() throws Exception {
							if (publish)
								l.publish(reference, serviceInfo);
							else
								l.unpublish(reference, serviceInfo);
						}
					});
				}
			}
		}
	}

	private void fireProxyDiscoveredUndiscovered(
			final IServiceInfo serviceInfo, final boolean discovered) {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			IProxyDiscoveryListener[] listeners = activator
					.getProxyDiscoveredListeners();
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					final IProxyDiscoveryListener l = listeners[i];
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							logError(
									"fireProxyDiscoveredUndiscovered", //$NON-NLS-1$
									"Exception calling proxy discovery listener", //$NON-NLS-1$
									exception);
						}

						public void run() throws Exception {
							if (discovered)
								l.discovered(serviceInfo);
							else
								l.undiscovered(serviceInfo);
						}
					});
				}
			}
		}
	}

	private void logError(String method, String message, Throwable exception) {
		LogUtility.logError(method, message, this.getClass(), exception);
	}

	private void logInfo(String method, String message, Throwable exception) {
		LogUtility.logInfo(method, message, this.getClass(), exception);
	}

	private URI createURI(ID endpointContainerID, String path)
			throws URISyntaxException {
		URI uri = null;
		String str = endpointContainerID.getName();
		while (true) {
			try {
				uri = new URI(str);
				if (uri.getHost() != null) {
					break;
				} else {
					final String rawSchemeSpecificPart = uri
							.getRawSchemeSpecificPart();
					// make sure we break eventually
					if (str.equals(rawSchemeSpecificPart)) {
						uri = null;
						break;
					} else {
						str = rawSchemeSpecificPart;
					}
				}
			} catch (URISyntaxException e) {
				uri = null;
				break;
			}
		}
		String scheme = RemoteServicePublication.SERVICE_TYPE;
		int port = 32565;
		if (uri != null) {
			port = uri.getPort();
			if (port == -1)
				port = 32565;
		}
		String host = null;
		if (uri != null) {
			host = uri.getHost();
		} else {
			try {
				host = InetAddress.getLocalHost().getHostAddress();
			} catch (Exception e) {
				logInfo("createURI", //$NON-NLS-1$
						"failed to get local host adress, falling back to \'localhost\'.", e); //$NON-NLS-1$
				host = "localhost"; //$NON-NLS-1$
			}
		}
		return new URI(scheme, null, host, port, path, null, null);
	}

	private void addPropertiesToDiscoveryServiceProperties(
			IServiceProperties discoveryServiceProperties,
			Map servicePublicationServiceProperties) {
		for (Iterator i = servicePublicationServiceProperties.keySet()
				.iterator(); i.hasNext();) {
			Object key = i.next();
			if (!(key instanceof String)) {
				trace("addPropertiesToDiscoveryServiceProperties", //$NON-NLS-1$
						"skipping non-string key " + key); //$NON-NLS-1$
				continue;
			}
			String keyStr = (String) key;
			Object val = servicePublicationServiceProperties.get(keyStr);
			if (val instanceof String) {
				discoveryServiceProperties.setPropertyString(keyStr,
						(String) val);
			} else if (val instanceof byte[]) {
				discoveryServiceProperties.setPropertyBytes(keyStr,
						(byte[]) val);
			} else if (val instanceof Serializable) {
				discoveryServiceProperties.setProperty(keyStr, val);
			}
		}
	}

	private synchronized IDiscoveryAdvertiser getAdvertiser() {
		try {
			if (advertiser == null) {
				advertiser = Activator.getDefault().getAdvertiser();
			}
		} catch (InterruptedException e) {
			logError("getAdvertiser", "Cannot get IDiscoveryAdvertiser", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return advertiser;
	}

	private String getPropertyWithDefault(Map properties, String key, String def) {
		if (properties == null)
			return def;
		String val = (String) properties.get(key);
		return (val == null) ? def : val;
	}

	private String[] getStringArrayPropertyWithDefault(Map properties,
			String key, String def) {
		if (properties == null)
			return new String[] { def };
		Object o = properties.get(key);
		if (o instanceof String) {
			return new String[] { (String) o };
		} else if (o instanceof String[]) {
			return (String[]) o;
		} else
			return new String[] { def };
	}

	protected IServiceTypeID createServiceTypeID(
			Map servicePublicationProperties, Namespace aNamespace)
			throws IDCreateException {
		String namingAuthority = getPropertyWithDefault(
				servicePublicationProperties,
				RemoteServicePublication.NAMING_AUTHORITY,
				IServiceTypeID.DEFAULT_NA);
		String[] scopes = getStringArrayPropertyWithDefault(
				servicePublicationProperties, RemoteServicePublication.SCOPE,
				IServiceTypeID.DEFAULT_SCOPE[0]);
		String[] protocols = getStringArrayPropertyWithDefault(
				servicePublicationProperties,
				RemoteServicePublication.SERVICE_PROTOCOL,
				IServiceTypeID.DEFAULT_PROTO[0]);

		return ServiceIDFactory.getDefault().createServiceTypeID(aNamespace,
				new String[] { RemoteServicePublication.SERVICE_TYPE }, scopes,
				protocols, namingAuthority);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi
	 * .framework.ServiceReference, java.lang.Object)
	 */
	public void modifiedService(ServiceReference reference, Object service) {
		unpublishService(reference);
		handleServicePublication(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi
	 * .framework.ServiceReference, java.lang.Object)
	 */
	public void removedService(ServiceReference reference, Object service) {
		unpublishService(reference);
	}

	private void unpublishService(ServiceReference reference) {
		IServiceInfo svcInfo = null;
		synchronized (serviceInfos) {
			try {
				svcInfo = removeServiceInfo(reference);
				if (svcInfo != null)
					getAdvertiser().unregisterService(svcInfo);
			} catch (ECFRuntimeException e) {
				logError("publishService", "Cannot unregister serviceInfo=" //$NON-NLS-1$ //$NON-NLS-2$
						+ svcInfo, e);
			}
		}
		if (svcInfo != null)
			fireHostPublishUnpublish(reference, svcInfo, false);
	}

	protected void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.SVCPUBHANDLERDEBUG,
				this.getClass(), methodName, message);
	}

	public void dispose() {
		if (advertiser != null) {
			for (Iterator i = serviceInfos.keySet().iterator(); i.hasNext();) {
				ServiceReference sr = (ServiceReference) i.next();
				unpublishService(sr);
			}
			serviceInfos.clear();
			advertiser = null;
		}
	}
}
