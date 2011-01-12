/* 
 * Copyright (c) 2009 Siemens Enterprise Communications GmbH & Co. KG, 
 * Germany. All rights reserved.
 *
 * Siemens Enterprise Communications GmbH & Co. KG is a Trademark Licensee 
 * of Siemens AG.
 *
 * This material, including documentation and any related computer programs,
 * is protected by copyright controlled by Siemens Enterprise Communications 
 * GmbH & Co. KG and its licensors. All rights are reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ecf.osgi.services.discovery.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceTracker;
import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescription;
import org.eclipse.ecf.osgi.services.discovery.ServicePublication;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;

public class FileBasedDiscoveryImpl {
	/**
	 * ServiceRegistration property identifying Discovery's default strategy for
	 * distribution of published service information. It's up to the Discovery
	 * service to provide and support this property. Value of this property is
	 * of type String.
	 * 
	 * TODO do we support this property?
	 */
	public static final String PROP_KEY_PUBLISH_STRATEGY = "osgi.discovery.strategy.publication";

	/**
	 * Constant for a "push" publication strategy: published service information
	 * is actively pushed to the network for discovery.
	 */
	public static final String PROP_VAL_PUBLISH_STRATEGY_PUSH = "push";

	/**
	 * Constant for a "pull" publication strategy: published service information
	 * is available just upon lookup requests.
	 */
	public static final String PROP_VAL_PUBLISH_STRATEGY_PULL = "pull";

	private static BundleContext context;

	private static LogService logService;

	private static Map /* <SLPServiceDescriptionAdapter> */inMemoryCache = Collections
			.synchronizedMap(new HashMap());

	private ServiceTracker spTracker = null;

	private DSTTracker discoTrackerCustomizer = null;

	private ServiceTracker discoTracker = null;

	private BundleTracker bt = null;

	/**
	 * Constructor.
	 * 
	 * @param bc
	 *            the BundleContext of the containing bundle.
	 * @param logger
	 *            a LogService instance
	 */
	public FileBasedDiscoveryImpl(final BundleContext bc,
			final LogService logger) {
		logService = logger;
		context = bc;
	}

	/**
	 * Initialization method called by Activator.
	 */
	public void init() {
		log(LogService.LOG_DEBUG, "init");
		discoTrackerCustomizer = new DSTTracker(context);
		discoTracker = new ServiceTracker(context,
				DiscoveredServiceTracker.class.getName(),
				discoTrackerCustomizer);
		discoTracker.open();
		spTracker = new ServiceTracker(context,
				ServicePublication.class.getName(),
				new ServicePublicationTracker(context, this));
		spTracker.open();
		// We track active and starting bundles as per Remote Services Admin spec
		bt = new BundleTracker(context, Bundle.ACTIVE | Bundle.STARTING, new BundleTrackerImpl(this));
		bt.open();
	}

	/**
	 * Shutdown method called by Activator.
	 */
	public void destroy() {
		log(LogService.LOG_DEBUG, "destroy");
		bt.close();
		discoTracker.close();
		spTracker.close();
	}

	/**
	 * Publishes a service.
	 * 
	 * TODO: publish also for every endpoint interface TODO: use
	 * endpointLocation as service URL if given
	 * 
	 * @param javaInterfaces
	 *            collection of java interface names
	 * @param javaInterfacesAndVersions
	 *            collection of versions, where the order of the version must
	 *            match the order of the java interfaces
	 * @param javaInterfacesAndEndpointInterfaces
	 *            optional collection of endpointinterface names, where the
	 *            order must match the order of the java interfaces
	 * @param properties
	 *            map of properties; keys must be Strings, values are of type
	 *            object
	 * @param strategy
	 *            optional string that defines the publish strategy
	 * @return a ServiceEndpointDescription or null, if an error occurred during
	 *         creation of the ServiceDescription
	 */
	protected ServiceEndpointDescription publishService(
			Collection/* <String> */javaInterfaces,
			Collection/* <String> */javaInterfacesAndVersions,
			Collection/* <String> */javaInterfacesAndEndpointInterfaces,
			Map/* <String, Object> */properties, String strategy,
			String endpointID) {
		ServiceEndpointDescription svcDescr = new ServiceEndpointDescriptionImpl(
				javaInterfaces, javaInterfacesAndVersions,
				javaInterfacesAndEndpointInterfaces, properties, endpointID);
		storeAndNotify(svcDescr);
		return svcDescr;
	}

	private void storeAndNotify(final ServiceEndpointDescription svcDescr) {
		// add it to the available Services
		inMemoryCache.put(svcDescr.getEndpointID(), svcDescr);
		// inform the listener about the new available service
		notifyListenersOnNewServiceDescription(svcDescr);
	}

	protected void publishService(final ServiceEndpointDescription svcDescr) {
		storeAndNotify(svcDescr);
	}

	/**
	 * Unpublishes a given service description.
	 * 
	 * @param serviceDescription
	 *            the service to unpublish
	 * @throws IllegalArgumentException
	 *             if serviceDescription is null or does not contain at least
	 *             one java interface
	 */
	protected void unpublishService(
			final ServiceEndpointDescription serviceDescription) {
		validateServiceDescription(serviceDescription);
		log(LogService.LOG_DEBUG,
				"unpublish service " + serviceDescription.toString());
		inMemoryCache.remove(serviceDescription.getEndpointID());
		notifyListenersOnRemovedServiceDescription(serviceDescription);
	}

	/**
	 * 
	 */
	protected static synchronized void log(int logLevel, String msg) {
		if (logService != null) {
			logService.log(logLevel, msg);
		}
	}

	/**
	 * 
	 */
	protected static synchronized void log(int logLevel, String msg, Exception e) {
		if (logService != null) {
			logService.log(logLevel, msg, e);
		}
	}

	/**
	 * @param logger
	 *            the reference to the LogService which get called for logging
	 * 
	 */
	public static void setLogService(final LogService logger) {
		logService = logger;
	}

	/**
	 * This method checks if a given ServiceEndpointDescrioption follows the
	 * minimal requirements.
	 * 
	 * @param serviceDescription
	 *            the given ServiceEndpointDescription
	 * @throws IllegalArgumentException
	 *             if serviceDescription is null or does not contain at least
	 *             one java interface
	 */
	protected void validateServiceDescription(
			ServiceEndpointDescription serviceDescription) {
		if (serviceDescription == null)
			throw new IllegalArgumentException(
					"serviceDescription must not be null.");
		if (serviceDescription.getProvidedInterfaces() == null) {
			throw new IllegalArgumentException(
					"Given set of Java interfaces must not be null");
		}
		String ifName = (String) serviceDescription.getProvidedInterfaces()
				.iterator().next();
		if (serviceDescription.getProvidedInterfaces() == null
				|| serviceDescription.getProvidedInterfaces().size() <= 0
				|| ifName == null || ifName.length() <= 0) {
			throw new IllegalArgumentException(
					"serviceDescription must contain at least one service interface name.");
		}
	}

	/**
	 * Returns a Map of all registered DiscoveredServiceTracker trackers.
	 * 
	 * TODO should it be a copy of the map for thread safety? This could lead to
	 * failures during usage of the copy. But we do not block the Tracker,
	 * registrations and deregistrations of DSTs.
	 * 
	 * @return a copied Map of all registered DiscoveredServiceTracker trackers.
	 */
	protected Map getRegisteredServiceTracker() {
		if (discoTrackerCustomizer == null) {
			return new HashMap();
		}
		return new HashMap(discoTrackerCustomizer.getDsTrackers());
	}

	/**
	 * This method informs a just registered or modified service tracker if a
	 * service matches its properties.
	 * 
	 * TODO: add suppression of informing trackers twice for the same SED
	 * 
	 * @param tracker
	 *            the just registered or modified DiscoveredServiceTracker
	 */
	public static void notifyOnAvailableSEDs(
			final DiscoveredServiceTracker tracker, final Map matchingCriteria) {
		List cachedServices = new ArrayList(inMemoryCache.values());
		if (cachedServices != null) {
			Collection matchingInterfaces = new ArrayList();
			Collection matchingFilters = new ArrayList();
			Iterator it = cachedServices.iterator();
			while (it.hasNext()) {
				ServiceEndpointDescription svcDescr = (ServiceEndpointDescription) it
						.next();
				matchingInterfaces.clear();
				matchingFilters.clear();
				if (isTrackerInterestedInSED(svcDescr, matchingCriteria,
						matchingInterfaces, matchingFilters)) {
					tracker.serviceChanged(new DiscoveredServiceNotificationImpl(
							svcDescr, DiscoveredServiceNotification.AVAILABLE,
							matchingInterfaces, matchingFilters));
				}
			}
		}
	}

	/**
	 * 
	 * @param svcDescr
	 */
	protected void notifyListenersOnNewServiceDescription(
			ServiceEndpointDescription svcDescr) {
		Collection matchingInterfaces = new ArrayList();
		Collection matchingFilters = new ArrayList();
		Map discoveredSTs = getRegisteredServiceTracker();
		Iterator it = discoveredSTs.keySet().iterator();
		while (it.hasNext()) {
			DiscoveredServiceTracker st = (DiscoveredServiceTracker) it.next();
			Map trackerProps = (Map) discoveredSTs.get(st);
			matchingInterfaces.clear();
			matchingFilters.clear();
			if (isTrackerInterestedInSED(svcDescr, trackerProps,
					matchingInterfaces, matchingFilters)) {
				try {
					st.serviceChanged(new DiscoveredServiceNotificationImpl(
							svcDescr, DiscoveredServiceNotification.AVAILABLE,
							matchingInterfaces, matchingFilters));
				} catch (Exception e) {
					log(LogService.LOG_ERROR,
							"Exceptions where thrown while notifying about a new remote service.",
							e);
				}

			}
		}
	}

	/**
	 * Notifies all DSTTrackers about an unpublished service.
	 * 
	 * @param svcDescr
	 *            the unpublished ServiceEndpointDescription
	 */
	protected void notifyListenersOnRemovedServiceDescription(
			ServiceEndpointDescription svcDescr) {
		Collection matchingInterfaces = new ArrayList();
		Collection matchingFilters = new ArrayList();
		Map discoveredSTs = getRegisteredServiceTracker();
		Iterator it = discoveredSTs.keySet().iterator();
		while (it.hasNext()) {
			DiscoveredServiceTracker st = (DiscoveredServiceTracker) it.next();
			Map trackerProps = (Map) discoveredSTs.get(st);
			matchingInterfaces.clear();
			matchingFilters.clear();
			// inform it if the listener has no Filter set
			// or the filter matches the criteria
			if (isTrackerInterestedInSED(svcDescr, trackerProps,
					matchingInterfaces, matchingFilters)) {
				try {
					st.serviceChanged(new DiscoveredServiceNotificationImpl(
							svcDescr,
							DiscoveredServiceNotification.UNAVAILABLE,
							matchingInterfaces, matchingFilters));
				} catch (Exception e) {
					log(LogService.LOG_ERROR,
							"Exceptions where thrown while notifying about removal of a remote service.",
							e);
				}
			}
		}
	}

	/**
	 * 
	 * @param svcDescr
	 */
	protected void notifyListenersOnModifiedServiceDescription(
			ServiceEndpointDescription svcDescr) {
		Collection matchingInterfaces = new ArrayList();
		Collection matchingFilters = new ArrayList();
		Map discoveredSTs = getRegisteredServiceTracker();
		Iterator it = discoveredSTs.keySet().iterator();
		while (it.hasNext()) {
			DiscoveredServiceTracker st = (DiscoveredServiceTracker) it.next();
			Map trackerProps = (Map) discoveredSTs.get(st);
			// inform it if the listener has no Filter set
			// or the filter matches the criteria
			matchingInterfaces.clear();
			matchingFilters.clear();
			if (isTrackerInterestedInSED(svcDescr, trackerProps,
					matchingInterfaces, matchingFilters)) {
				try {
					st.serviceChanged(new DiscoveredServiceNotificationImpl(
							svcDescr, DiscoveredServiceNotification.MODIFIED,
							matchingInterfaces, matchingFilters));
				} catch (Exception e) {
					log(LogService.LOG_ERROR,
							"Exceptions where thrown while notifying about modification of a remote service.",
							e);
				}
			}
		}
	}

	/**
	 * Compares the properties of a registered DiscoveredServiceTracker with the
	 * SED properties. IF they match, it returns true.
	 * 
	 * @param svcDescr
	 * @param trackerProperties
	 * @param matchingInterfaces
	 *            (an out-argument) a collection which will contain all
	 *            tracker's interface criteria matching with given
	 *            ServiceEndpointDescription object
	 * @param matchingFilters
	 *            (an out-argument) a collection which will contain all
	 *            tracker's filter criteria matching with given
	 *            ServiceEndpointDescription object
	 * @return true if the service tracker properties match the SEDs properties,
	 *         else false
	 */
	public static boolean isTrackerInterestedInSED(
			ServiceEndpointDescription svcDescr,
			Map/* String, Object */trackerProperties,
			Collection matchingInterfaces, Collection matchingFilters) {
		Collection interfaceCriteria = (Collection) trackerProperties
				.get(DiscoveredServiceTracker.INTERFACE_MATCH_CRITERIA);
		Collection filter = (Collection) trackerProperties
				.get(DiscoveredServiceTracker.FILTER_MATCH_CRITERIA);
		boolean notify = false;
		if (interfaceCriteria == null && filter == null) {
			notify = true;
		} else {
			// if interface-criteria are defined on tracker
			if (interfaceCriteria != null && !interfaceCriteria.isEmpty()) {
				// then check whether tracker's interface-list contains one of
				// SED's interfaces
				Collection svcInterfaces = svcDescr.getProvidedInterfaces();
				if (svcInterfaces == null) {
					throw new RuntimeException("no interfaces provided");
				}
				Collection intersectionResult = new HashSet(interfaceCriteria);
				intersectionResult.retainAll(svcInterfaces);
				if (intersectionResult.size() > 0) {
					notify = true;
					if (matchingInterfaces != null) {
						matchingInterfaces.addAll(intersectionResult);
					}
				}
			}

			// if filter-criteria are defined on tracker
			if (filter != null && !filter.isEmpty()) {
				// check whether one filter of tracker's filter-list matches to
				// SED's properties
				Iterator it = filter.iterator();
				while (it.hasNext()) {
					String currentFilter = (String) it.next();
					try {
						Filter f = context.createFilter(currentFilter);
						if (f.match(new Hashtable(svcDescr.getProperties()))) {
							notify = true;
							if (matchingFilters != null) {
								matchingFilters.add(currentFilter);
							}
						}
					} catch (InvalidSyntaxException e) {
						throw new RuntimeException(e.getMessage());
					} catch (IllegalStateException isex) {
						// TODO: check whether this catch block is needed.
						isex.printStackTrace();
						// ignore it
					}
				}
			}
		}
		return notify;
	}

	/**
	 * Returns the list of known services without the locally published ones.
	 * 
	 * @return a list of all remote services
	 */
	public Map getCachedServices() {
		return new HashMap(inMemoryCache);
	}
}
