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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractDiscoveredEndpointDescriptionFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractServiceInfoFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultDiscoveredEndpointDescriptionFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultServiceInfoFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IDiscoveredEndpointDescriptionFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IServiceInfoFactory;
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;
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
		startServiceInfoFactory();
		startEndpointDescriptionFactory();
		startExecutor();
		startLocators();
		startBundleTracker();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		stopBundleTracker();
		stopLocators();
		stopEndpointDescriptionFactory();
		stopServiceInfoFactory();
		stopEndpointDescriptionFactoryTracker();
		stopSAXParserTracker();
		stopEndpointListenerTracker();
		stopLogServiceTracker();
		executor = null;
		Activator.context = null;
		Activator.instance = null;
	}

	private BundleTracker bundleTracker;
	private EndpointDescriptionBundleTrackerCustomizer bundleTrackerCustomizer;
	
	private void startBundleTracker() {
		bundleTrackerCustomizer = new EndpointDescriptionBundleTrackerCustomizer();
		bundleTracker = new BundleTracker(context,Bundle.ACTIVE | Bundle.STARTING,bundleTrackerCustomizer);
		bundleTracker.open();
	}
	
	private void stopBundleTracker() {
		if (bundleTracker != null) {
			bundleTracker.close();
			bundleTracker = null;
		}
		if (bundleTrackerCustomizer != null) {
			bundleTrackerCustomizer.close();
			bundleTrackerCustomizer = null;
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

	private ServiceTracker endpointListenerServiceTracker;
	private Object endpointListenerServiceTrackerLock = new Object();

	private void stopEndpointListenerTracker() {
		synchronized (endpointListenerServiceTrackerLock) {
			if (endpointListenerServiceTracker != null) {
				endpointListenerServiceTracker.close();
				endpointListenerServiceTracker = null;
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

	public EndpointListenerHolder[] getMatchingEndpointListenerHolders(
			EndpointDescription description) {
		synchronized (endpointListenerServiceTrackerLock) {
			if (context == null)
				return null;
			if (endpointListenerServiceTracker == null) {
				endpointListenerServiceTracker = new ServiceTracker(context,
						EndpointListener.class.getName(), null);
				endpointListenerServiceTracker.open();
			}
			return getMatchingEndpointListenerHolders(
					endpointListenerServiceTracker.getServiceReferences(),
					description);
		}
	}

	class EndpointListenerHolder {

		private EndpointListener listener;
		private EndpointDescription description;
		private String matchingFilter;

		public EndpointListenerHolder(EndpointListener l,
				EndpointDescription d, String f) {
			this.listener = l;
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

	private EndpointListenerHolder[] getMatchingEndpointListenerHolders(
			ServiceReference[] refs, EndpointDescription description) {
		if (refs == null)
			return null;
		List results = new ArrayList();
		for (int i = 0; i < refs.length; i++) {
			EndpointListener listener = (EndpointListener) context
					.getService(refs[i]);
			if (listener == null)
				continue;
			List filters = getStringPlusProperty(
					EndpointListener.ENDPOINT_LISTENER_SCOPE,
					getMapFromProperties(refs[i]));
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

	private List getStringPlusProperty(String key, Map properties) {
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

	private Object endpointDescriptionFactoryTrackerLock = new Object();
	private ServiceTracker endpointDescriptionFactoryTracker;

	private void stopEndpointDescriptionFactoryTracker() {
		synchronized (endpointDescriptionFactoryTrackerLock) {
			if (endpointDescriptionFactoryTracker != null) {
				endpointDescriptionFactoryTracker.close();
				endpointDescriptionFactoryTracker = null;
			}
		}
	}

	public IDiscoveredEndpointDescriptionFactory getEndpointDescriptionFactory() {
		synchronized (endpointDescriptionFactoryTrackerLock) {
			if (context == null)
				return null;
			if (endpointDescriptionFactoryTracker == null) {
				endpointDescriptionFactoryTracker = new ServiceTracker(
						context, IDiscoveredEndpointDescriptionFactory.class.getName(),
						null);
				endpointDescriptionFactoryTracker.open();
			}
			return (IDiscoveredEndpointDescriptionFactory) endpointDescriptionFactoryTracker
					.getService();
		}
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

	private AbstractServiceInfoFactory serviceInfoFactory;
	private ServiceRegistration serviceInfoFactoryRegistration;

	// ServiceInfo factory
	private void startServiceInfoFactory() {
		// For the service info factory 
		// registration, set the service ranking property to Integer.MIN_VALUE
		// so that any other registered factories will be preferred.
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		serviceInfoFactory = new DefaultServiceInfoFactory();
		serviceInfoFactoryRegistration = context.registerService(
				IServiceInfoFactory.class.getName(), serviceInfoFactory,
				(Dictionary) properties);
	}

	private void stopServiceInfoFactory() {
		if (serviceInfoFactoryRegistration != null) {
			serviceInfoFactoryRegistration.unregister();
			serviceInfoFactoryRegistration = null;
		}
		if (serviceInfoFactory != null) {
			serviceInfoFactory.close();
			serviceInfoFactory = null;
		}
	}

    // endpoint description factory
	private AbstractDiscoveredEndpointDescriptionFactory endpointDescriptionFactory;
	private ServiceRegistration endpointDescriptionFactoryRegistration;

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
		if (endpointDescriptionFactoryRegistration != null) {
			endpointDescriptionFactoryRegistration.unregister();
			endpointDescriptionFactoryRegistration = null;
		}
		if (endpointDescriptionFactory != null) {
			endpointDescriptionFactory.close();
			endpointDescriptionFactory = null;
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

}
