/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultDiscoveredEndpointDescriptionFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultEndpointDescriptionPublisher;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultServiceInfoFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IDiscoveredEndpointDescriptionFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionPublisher;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IServiceInfoFactory;
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;
import org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap;
import org.eclipse.osgi.framework.eventmgr.EventDispatcher;
import org.eclipse.osgi.framework.eventmgr.EventManager;
import org.eclipse.osgi.framework.eventmgr.ListenerQueue;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.remoteserviceadmin";

	private static final boolean DEBUG = false;
	
	private static BundleContext context;
	private static Activator instance;

	static BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.instance = this;
		startExecutor();
		startServiceInfoFactory();
		startEndpointDescriptionFactory();
		startEndpointDescriptionPublisher();
		startLocators();
		startEndpointListenerTracker();
		startLocalEndpointDescriptionHandler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		stopLocalEndpointDescriptionHandler();
		stopEndpointListenerTracker();
		stopLocators();
		stopEndpointDescriptionPublisher();
		stopEndpointDescriptionFactory();
		stopServiceInfoFactory();
		stopDiscoveryAdvertiserTracker();
		stopSAXParserTracker();
		stopLogServiceTracker();
		executor = null;
		Activator.context = null;
		Activator.instance = null;
	}

	private EventManager eventManager;
	private ListenerQueue eventQueue;
	private LocatorServiceListener localLocatorServiceListener;

	private BundleTracker bundleTracker;
	private EndpointDescriptionBundleTrackerCustomizer bundleTrackerCustomizer;

	private void startLocalEndpointDescriptionHandler() {
		ThreadGroup eventGroup = new ThreadGroup(
				"EventAdmin EndpointListener Dispatcher"); //$NON-NLS-1$
		eventGroup.setDaemon(true);
		eventManager = new EventManager(
				"EventAdmin EndpointListener Dispatcher", eventGroup); //$NON-NLS-1$
		eventQueue = new ListenerQueue(eventManager);

		CopyOnWriteIdentityMap listeners = new CopyOnWriteIdentityMap();
		listeners.put(this, this);
		eventQueue.queueListeners(listeners.entrySet(), new EventDispatcher() {
			public void dispatchEvent(Object eventListener,
					Object listenerObject, int eventAction, Object eventObject) {

				final EndpointListenerEvent event = (EndpointListenerEvent) eventObject;
				final EndpointListener endpointListener = event
						.getEndpointListener();
				final EndpointDescription endpointDescription = event
						.getEndointDescription();
				final String matchingFilter = event.getMatchingFilter();
				// run with SafeRunner, so that any exceptions are logged by
				// our logger
				SafeRunner.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
						Activator a = Activator.getDefault();
						if (a != null)
							a.log(new Status(
									IStatus.ERROR,
									Activator.PLUGIN_ID,
									IStatus.ERROR,
									"Exception in EndpointListener listener=" + endpointListener + " description=" + endpointDescription + " matchingFilter=" + matchingFilter, exception)); //$NON-NLS-1$
					}

					public void run() throws Exception {
						// Call endpointAdded or endpointRemoved
						if (event.isDiscovered())
							endpointListener.endpointAdded(endpointDescription,
									matchingFilter);
						else
							endpointListener.endpointRemoved(
									endpointDescription, matchingFilter);
					}
				});
			}
		});

		localLocatorServiceListener = new LocatorServiceListener();
		bundleTrackerCustomizer = new EndpointDescriptionBundleTrackerCustomizer(
				localLocatorServiceListener);
		bundleTracker = new BundleTracker(context, Bundle.ACTIVE
				| Bundle.STARTING, bundleTrackerCustomizer);
		bundleTracker.open();
	}

	private void stopLocalEndpointDescriptionHandler() {
		if (bundleTracker != null) {
			bundleTracker.close();
			bundleTracker = null;
		}
		if (bundleTrackerCustomizer != null) {
			bundleTrackerCustomizer.close();
			bundleTrackerCustomizer = null;
		}
		if (localLocatorServiceListener != null) {
			localLocatorServiceListener.close();
			localLocatorServiceListener = null;
		}
		// Finally, shutdown event manager
		if (eventManager != null) {
			eventManager.close();
			eventManager = null;
		}
	}

	private ServiceTracker locatorServiceTracker;
	private Map<IDiscoveryLocator, LocatorServiceListener> locatorListeners;

	private void startLocators() {
		locatorListeners = new HashMap();
		// Create locator service tracker
		locatorServiceTracker = new ServiceTracker(context,
				IDiscoveryLocator.class.getName(),
				new LocatorTrackerCustomizer());
		locatorServiceTracker.open();
		Object[] locators = locatorServiceTracker.getServices();
		if (locators != null) {
			for (int i = 0; i < locators.length; i++) {
				// Add service listener to locator
				openLocator((IDiscoveryLocator) locators[i]);
			}
		}
	}

	private void stopLocators() {
		synchronized (locatorListeners) {
			for (IDiscoveryLocator l : locatorListeners.keySet()) {
				LocatorServiceListener locatorListener = locatorListeners
						.get(l);
				if (locatorListener != null) {
					l.removeServiceListener(locatorListener);
					locatorListener.close();
				}
			}
			locatorListeners.clear();
		}

		Object[] locators = locatorServiceTracker.getServices();
		if (locators != null) {
			for (int i = 0; i < locators.length; i++) {
				// Add service listener to locator
				shutdownLocator((IDiscoveryLocator) locators[i]);
			}
		}

		if (locatorServiceTracker != null) {
			locatorServiceTracker.close();
			locatorServiceTracker = null;
		}
	}

	public Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> getAllDiscoveredEndpointDescriptions() {
		Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> result = new ArrayList();
		// Get local first
		result.addAll(localLocatorServiceListener.getEndpointDescriptions());
		synchronized (locatorListeners) {
			for (IDiscoveryLocator l : locatorListeners.keySet()) {
				LocatorServiceListener locatorListener = locatorListeners
						.get(l);
				result.addAll(locatorListener.getEndpointDescriptions());
			}
		}
		return result;
	}

	class EndpointListenerEvent {

		private EndpointListener endpointListener;
		private org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription;
		private String matchingFilter;
		private boolean discovered;

		public EndpointListenerEvent(
				EndpointListener endpointListener,
				org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
				String matchingFilter, boolean discovered) {
			this.endpointListener = endpointListener;
			this.endpointDescription = endpointDescription;
			this.matchingFilter = matchingFilter;
			this.discovered = discovered;
		}

		public EndpointListener getEndpointListener() {
			return endpointListener;
		}

		public org.osgi.service.remoteserviceadmin.EndpointDescription getEndointDescription() {
			return endpointDescription;
		}

		public String getMatchingFilter() {
			return matchingFilter;
		}

		public boolean isDiscovered() {
			return discovered;
		}
	}

	public void queueEndpointDescription(
			EndpointListener listener,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			String matchingFilters, boolean discovered) {
		if (eventQueue == null)
			return;
		synchronized (eventQueue) {
			eventQueue
					.dispatchEventAsynchronous(0, new EndpointListenerEvent(
							listener, endpointDescription, matchingFilters,
							discovered));
		}
	}

	public void queueEndpointDescription(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			boolean discovered) {
		Activator.EndpointListenerHolder[] endpointListenerHolders = Activator
				.getDefault().getMatchingEndpointListenerHolders(
						endpointDescription);
		if (endpointListenerHolders != null) {
			for (int i = 0; i < endpointListenerHolders.length; i++) {
				queueEndpointDescription(
						endpointListenerHolders[i].getListener(),
						endpointListenerHolders[i].getDescription(),
						endpointListenerHolders[i].getMatchingFilter(),
						discovered);

			}
		} else {
			if (DEBUG) log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
					IStatus.INFO, "No matching EndpointListeners found for "
							+ (discovered ? "discovered" : "undiscovered")
							+ " endpointDescription=" + endpointDescription,
					null));
		}

	}

	void openLocator(IDiscoveryLocator locator) {
		if (locator == null || context == null)
			return;
		synchronized (locatorListeners) {
			LocatorServiceListener locatorListener = new LocatorServiceListener(
					locator);
			locatorListeners.put(locator, locatorListener);
			processInitialLocatorServices(locator, locatorListener);
		}
	}

	void shutdownLocator(IDiscoveryLocator locator) {
		if (locator == null || context == null)
			return;
		synchronized (locatorListeners) {
			LocatorServiceListener locatorListener = locatorListeners
					.remove(locator);
			if (locatorListener != null)
				locatorListener.close();
		}
	}

	private void processInitialLocatorServices(final IDiscoveryLocator locator,
			final LocatorServiceListener locatorListener) {
		IProgressRunnable runnable = new IProgressRunnable() {
			public Object run(IProgressMonitor arg0) throws Exception {
				if (context == null)
					return null;
				IServiceInfo[] serviceInfos = locator.getServices();
				for (int i = 0; i < serviceInfos.length; i++) {
					locatorListener.handleService(serviceInfos[i], true);
				}
				return null;
			}
		};
		executor.execute(runnable, null);
	}

	void shutdownLocators() {
		Object[] locators = locatorServiceTracker.getServices();
		if (locators != null) {
			for (int i = 0; i < locators.length; i++) {
				// Add service listener to locator
				shutdownLocator((IDiscoveryLocator) locators[i]);
			}
		}
	}

	private IExecutor executor;

	private void startExecutor() {
		executor = new ThreadsExecutor();
	}

	private EndpointListenerTrackerCustomizer endpointListenerServiceTrackerCustomizer;
	private ServiceTracker endpointListenerServiceTracker;
	private Object endpointListenerServiceTrackerLock = new Object();

	private EndpointListenerHolder[] getMatchingEndpointListenerHolders(
			EndpointDescription description) {
		synchronized (endpointListenerServiceTrackerLock) {
			if (context == null)
				return null;
			return getMatchingEndpointListenerHolders(
					endpointListenerServiceTracker.getServiceReferences(),
					description);
		}
	}

	private void startEndpointListenerTracker() {
		synchronized (endpointListenerServiceTrackerLock) {
			endpointListenerServiceTrackerCustomizer = new EndpointListenerTrackerCustomizer();
			endpointListenerServiceTracker = new ServiceTracker(context,
					EndpointListener.class.getName(),
					endpointListenerServiceTrackerCustomizer);
			endpointListenerServiceTracker.open();
		}
	}

	private void stopEndpointListenerTracker() {
		synchronized (endpointListenerServiceTrackerLock) {
			if (endpointListenerServiceTracker != null) {
				endpointListenerServiceTracker.close();
				endpointListenerServiceTracker = null;
			}
			if (endpointListenerServiceTrackerCustomizer != null) {
				endpointListenerServiceTrackerCustomizer.close();
				endpointListenerServiceTrackerCustomizer = null;
			}
		}
	}

	private class LocatorTrackerCustomizer implements ServiceTrackerCustomizer {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.
		 * osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			IDiscoveryLocator locator = (IDiscoveryLocator) context
					.getService(reference);
			openLocator(locator);
			return locator;
		}

		public void modifiedService(ServiceReference reference, Object service) {
		}

		public void removedService(ServiceReference reference, Object service) {
			shutdownLocator((IDiscoveryLocator) service);
		}
	}

	class EndpointListenerHolder {

		private EndpointListener listener;
		private EndpointDescription description;
		private String matchingFilter;

		public EndpointListenerHolder(EndpointListener l,
				EndpointDescription d, String f) {
			this.listener = l;
			this.description = d;
			this.matchingFilter = f;
		}

		public EndpointListener getListener() {
			return listener;
		}

		public EndpointDescription getDescription() {
			return description;
		}

		public String getMatchingFilter() {
			return matchingFilter;
		}
	}

	public EndpointListenerHolder[] getMatchingEndpointListenerHolders(
			ServiceReference[] refs, EndpointDescription description) {
		if (refs == null)
			return null;
		List results = new ArrayList();
		for (int i = 0; i < refs.length; i++) {
			EndpointListener listener = (EndpointListener) context
					.getService(refs[i]);
			if (listener == null)
				continue;
			List filters = getStringPlusProperty(getMapFromProperties(refs[i]),
					EndpointListener.ENDPOINT_LISTENER_SCOPE);
			String matchingFilter = isMatch(description, filters);
			if (matchingFilter != null)
				results.add(new EndpointListenerHolder(listener, description,
						matchingFilter));
		}
		return (EndpointListenerHolder[]) results
				.toArray(new EndpointListenerHolder[] {});
	}

	private String isMatch(EndpointDescription description, List filters) {
		for (Iterator j = filters.iterator(); j.hasNext();) {
			String filter = (String) j.next();
			if (description.matches(filter))
				return filter;
		}
		return null;
	}

	private Map getMapFromProperties(ServiceReference ref) {
		Map results = new HashMap();
		String[] keys = ref.getPropertyKeys();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				results.put(keys[i], ref.getProperty(keys[i]));
			}
		}
		return results;
	}

	public static List getStringPlusProperty(Map properties, String key) {
		Object value = properties.get(key);
		if (value == null) {
			return Collections.EMPTY_LIST;
		}

		if (value instanceof String) {
			return Collections.singletonList((String) value);
		}

		if (value instanceof String[]) {
			String[] values = (String[]) value;
			List result = new ArrayList(values.length);
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					result.add(values[i]);
				}
			}
			return Collections.unmodifiableList(result);
		}

		if (value instanceof Collection) {
			Collection values = (Collection) value;
			List result = new ArrayList(values.size());
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				Object v = iter.next();
				if (v instanceof String) {
					result.add((String) v);
				}
			}
			return Collections.unmodifiableList(result);
		}

		return Collections.EMPTY_LIST;
	}

	public String getFrameworkUUID() {
		if (context == null)
			return null;
		// code get and set the framework uuid property as specified in
		// r2.enterprise.pdf pg 297
		synchronized ("org.osgi.framework.uuid") {
			String result = context.getProperty("org.osgi.framework.uuid");
			if (result == null) {
				UUID newUUID = UUID.randomUUID();
				result = newUUID.toString();
				System.setProperty("org.osgi.framework.uuid",
						newUUID.toString());
			}
			return result;
		}
	}

	// Sax parser

	private Object saxParserFactoryTrackerLock = new Object();
	private ServiceTracker saxParserFactoryTracker;

	public SAXParserFactory getSAXParserFactory() {
		if (instance == null)
			return null;
		synchronized (saxParserFactoryTrackerLock) {
			if (saxParserFactoryTracker == null) {
				saxParserFactoryTracker = new ServiceTracker(context,
						SAXParserFactory.class.getName(), null);
				saxParserFactoryTracker.open();
			}
			return (SAXParserFactory) saxParserFactoryTracker.getService();
		}
	}

	private void stopSAXParserTracker() {
		synchronized (saxParserFactoryTrackerLock) {
			if (saxParserFactoryTracker != null) {
				saxParserFactoryTracker.close();
				saxParserFactoryTracker = null;
			}
		}
	}

	private DefaultServiceInfoFactory defaultServiceInfoFactory;
	private ServiceRegistration defaultServiceInfoFactoryRegistration;
	private ServiceTracker serviceInfoFactoryTracker;
	private Object serviceInfoFactoryTrackerLock = new Object();

	// ServiceInfo factory
	private void startServiceInfoFactory() {
		// For the service info factory
		// registration, set the service ranking property to Integer.MIN_VALUE
		// so that any other registered factories will be preferred.
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		defaultServiceInfoFactory = new DefaultServiceInfoFactory();
		defaultServiceInfoFactoryRegistration = context.registerService(
				IServiceInfoFactory.class.getName(), defaultServiceInfoFactory,
				(Dictionary) properties);
	}

	private void stopServiceInfoFactory() {
		synchronized (serviceInfoFactoryTrackerLock) {
			if (serviceInfoFactoryTracker != null) {
				serviceInfoFactoryTracker.close();
				serviceInfoFactoryTracker = null;
			}
		}
		if (defaultServiceInfoFactoryRegistration != null) {
			defaultServiceInfoFactoryRegistration.unregister();
			defaultServiceInfoFactoryRegistration = null;
		}
		if (defaultServiceInfoFactory != null) {
			defaultServiceInfoFactory.close();
			defaultServiceInfoFactory = null;
		}
	}

	public IServiceInfoFactory getServiceInfoFactory() {
		if (context == null)
			return null;
		synchronized (serviceInfoFactoryTrackerLock) {
			if (serviceInfoFactoryTracker == null) {
				serviceInfoFactoryTracker = new ServiceTracker(context,
						IServiceInfoFactory.class.getName(), null);
				serviceInfoFactoryTracker.open();
			}
		}
		return (IServiceInfoFactory) serviceInfoFactoryTracker.getService();
	}

	// endpoint description factory
	private DefaultDiscoveredEndpointDescriptionFactory endpointDescriptionFactory;
	private ServiceRegistration endpointDescriptionFactoryRegistration;
	private Object endpointDescriptionFactoryTrackerLock = new Object();
	private ServiceTracker endpointDescriptionFactoryTracker;

	private void startEndpointDescriptionFactory() {
		// For the endpoint description factory
		// registration, set the service ranking property to Integer.MIN_VALUE
		// so that any other registered factories will be preferred.
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		endpointDescriptionFactory = new DefaultDiscoveredEndpointDescriptionFactory();
		endpointDescriptionFactoryRegistration = context.registerService(
				IDiscoveredEndpointDescriptionFactory.class.getName(),
				endpointDescriptionFactory, (Dictionary) properties);
	}

	private void stopEndpointDescriptionFactory() {
		synchronized (endpointDescriptionFactoryTrackerLock) {
			if (endpointDescriptionFactoryTracker != null) {
				endpointDescriptionFactoryTracker.close();
				endpointDescriptionFactoryTracker = null;
			}
		}
		if (endpointDescriptionFactoryRegistration != null) {
			endpointDescriptionFactoryRegistration.unregister();
			endpointDescriptionFactoryRegistration = null;
		}
		if (endpointDescriptionFactory != null) {
			endpointDescriptionFactory.close();
			endpointDescriptionFactory = null;
		}
	}

	public IDiscoveredEndpointDescriptionFactory getDiscoveredEndpointDescriptionFactory() {
		synchronized (endpointDescriptionFactoryTrackerLock) {
			if (context == null)
				return null;
			if (endpointDescriptionFactoryTracker == null) {
				endpointDescriptionFactoryTracker = new ServiceTracker(context,
						IDiscoveredEndpointDescriptionFactory.class.getName(),
						null);
				endpointDescriptionFactoryTracker.open();
			}
			return (IDiscoveredEndpointDescriptionFactory) endpointDescriptionFactoryTracker
					.getService();
		}
	}

	// Logging

	private ServiceTracker logServiceTracker = null;
	private LogService logService = null;
	private Object logServiceTrackerLock = new Object();

	public LogService getLogService() {
		if (context == null)
			return null;
		synchronized (logServiceTrackerLock) {
			if (logServiceTracker == null) {
				logServiceTracker = new ServiceTracker(context,
						LogService.class.getName(), null);
				logServiceTracker.open();
			}
			logService = (LogService) logServiceTracker.getService();
			if (logService == null)
				logService = new SystemLogService(PLUGIN_ID);
			return logService;
		}
	}

	public void log(IStatus status) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(null, LogHelper.getLogCode(status),
					LogHelper.getLogMessage(status), status.getException());
	}

	public void log(ServiceReference sr, IStatus status) {
		log(sr, LogHelper.getLogCode(status), LogHelper.getLogMessage(status),
				status.getException());
	}

	public void log(ServiceReference sr, int level, String message, Throwable t) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(sr, level, message, t);
	}

	private void stopLogServiceTracker() {
		synchronized (logServiceTrackerLock) {
			if (logServiceTracker != null) {
				logServiceTracker.close();
				logServiceTracker = null;
				logService = null;
			}
		}
	}

	private ServiceTracker discoveryAdvertiserTracker;
	private Object discoveryAdvertiserTrackerLock = new Object();

	public IDiscoveryAdvertiser[] getDiscoveryAdvertisers() {
		synchronized (discoveryAdvertiserTrackerLock) {
			if (discoveryAdvertiserTracker == null) {
				discoveryAdvertiserTracker = new ServiceTracker(context,
						IDiscoveryAdvertiser.class.getName(), null);
				discoveryAdvertiserTracker.open();
			}
		}
		return (IDiscoveryAdvertiser[]) discoveryAdvertiserTracker
				.getServices();
	}

	private void stopDiscoveryAdvertiserTracker() {
		synchronized (discoveryAdvertiserTrackerLock) {
			if (discoveryAdvertiserTracker != null) {
				discoveryAdvertiserTracker.close();
				discoveryAdvertiserTracker = null;
			}
		}
	}

	private DefaultEndpointDescriptionPublisher defaultEndpointDescriptionPublisher;
	private ServiceRegistration defaultEndpointDescriptionPublisherRegistration;
	private ServiceTracker endpointDescriptionPublisherTracker;
	private Object endpointDescriptionPublisherTrackerLock = new Object();

	private void startEndpointDescriptionPublisher() {
		// For the endpoint description factory
		// registration, set the service ranking property to Integer.MIN_VALUE
		// so that any other registered factories will be preferred.
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		defaultEndpointDescriptionPublisher = new DefaultEndpointDescriptionPublisher();
		defaultEndpointDescriptionPublisherRegistration = context
				.registerService(IEndpointDescriptionPublisher.class.getName(),
						defaultEndpointDescriptionPublisher,
						(Dictionary) properties);
	}

	private void stopEndpointDescriptionPublisher() {
		synchronized (endpointDescriptionPublisherTrackerLock) {
			if (endpointDescriptionPublisherTracker != null) {
				endpointDescriptionPublisherTracker.close();
				endpointDescriptionPublisherTracker = null;
			}
		}
		if (defaultEndpointDescriptionPublisherRegistration != null) {
			defaultEndpointDescriptionPublisherRegistration.unregister();
			defaultEndpointDescriptionPublisherRegistration = null;
		}
		if (defaultEndpointDescriptionPublisher != null) {
			defaultEndpointDescriptionPublisher.close();
			defaultEndpointDescriptionPublisher = null;
		}
	}

	public IEndpointDescriptionPublisher getEndpointDescriptionPublisher() {
		synchronized (endpointDescriptionPublisherTrackerLock) {
			if (endpointDescriptionPublisherTracker == null) {
				endpointDescriptionPublisherTracker = new ServiceTracker(
						context, IEndpointDescriptionPublisher.class.getName(),
						null);
				endpointDescriptionPublisherTracker.open();
			}
		}
		return (IEndpointDescriptionPublisher) endpointDescriptionPublisherTracker
				.getService();
	}
}
