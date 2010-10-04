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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.identity.GUID;
import org.eclipse.ecf.core.identity.GUID.GUIDNamespace;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceTracker;
import org.eclipse.ecf.osgi.services.discovery.IHostDiscoveryListener;
import org.eclipse.ecf.osgi.services.discovery.IProxyDiscoveryListener;
import org.eclipse.ecf.osgi.services.discovery.ServicePublication;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.discovery"; //$NON-NLS-1$

	private static final long DISCOVERY_TIMEOUT = 5000;

	private ServiceTracker servicePublicationTracker;
	private ServiceTracker locatorTracker;
	private ServiceTracker advertiserTracker;
	private ServiceTracker discoveredServiceTrackerTracker;

	private ServiceTracker logServiceTracker = null;
	private LogService logService = null;

	private BundleContext context;
	private static Activator plugin;
	private ServicePublicationHandler servicePublicationHandler;

	public static final Activator getDefault() {
		return plugin;
	}

	private ServiceTracker hostPublicationListenerTracker;
	private ServiceTracker proxyDiscoveredListenerTracker;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext ctxt) throws Exception {
		plugin = this;
		this.context = ctxt;
		servicePublicationHandler = new ServicePublicationHandler(getGUID());

		servicePublicationTracker = new ServiceTracker(context,
				ServicePublication.class.getName(), servicePublicationHandler);
		servicePublicationTracker.open();

		// register the discovery service which is provided by SPH
		// Dictionary props = new Hashtable();
		// props.put(Discovery.VENDOR_NAME, "Eclipse.org");
		// props.put(Discovery.PRODUCT_NAME, "ECF Discovery");
		// props.put(Discovery.PRODUCT_VERSION, "1.0.0");
		// props.put(Discovery.SUPPORTED_PROTOCOLS, "SLP|mDNS|DNS-SRV");
		// ctxt.registerService(Discovery.class.getName(),
		// servicePublicationHandler, props);

		locatorTracker = new ServiceTracker(this.context,
				IDiscoveryLocator.class.getName(),
				new LocatorTrackerCustomizer());
		locatorTracker.open();
		IDiscoveryLocator locator = (IDiscoveryLocator) locatorTracker
				.getService();
		if (locator != null) {
			locator.addServiceListener(servicePublicationHandler);
		}
	}

	private byte[] getGUID() throws UnsupportedEncodingException {
		final GUIDNamespace namespace = new GUID.GUIDNamespace();
		final GUID id = (GUID) namespace.createInstance(null);
		// convert to external form to avoid problems with illegal chars in
		// discovery providers (e.g. '=' is not allowed in SLP)
		return id.toExternalForm().getBytes("ASCII"); //$NON-NLS-1$
	}

	protected synchronized LogService getLogService() {
		if (this.context == null)
			return null;
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context,
					LogService.class.getName(), null);
			logServiceTracker.open();
		}
		logService = (LogService) logServiceTracker.getService();
		if (logService == null)
			logService = new SystemLogService(PLUGIN_ID);
		return logService;
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

	BundleContext getContext() {
		return context;
	}

	public ServicePublicationHandler getServicePublicationHandler() {
		return servicePublicationHandler;
	}

	public synchronized IDiscoveryAdvertiser getAdvertiser()
			throws InterruptedException {
		if (this.context == null)
			return null;
		if (advertiserTracker == null) {
			advertiserTracker = new ServiceTracker(this.context,
					IDiscoveryAdvertiser.class.getName(), null);
			advertiserTracker.open();
		}
		return (IDiscoveryAdvertiser) advertiserTracker
				.waitForService(DISCOVERY_TIMEOUT);
	}

	public ServiceReference[] getDiscoveredServiceTrackerReferences() {
		if (discoveredServiceTrackerTracker == null) {
			discoveredServiceTrackerTracker = new ServiceTracker(this.context,
					DiscoveredServiceTracker.class.getName(), null);
			discoveredServiceTrackerTracker.open();
		}
		return discoveredServiceTrackerTracker.getServiceReferences();
	}

	public synchronized IHostDiscoveryListener[] getHostPublicationListeners() {
		if (this.context == null)
			return null;
		if (hostPublicationListenerTracker == null) {
			hostPublicationListenerTracker = new ServiceTracker(this.context,
					IHostDiscoveryListener.class.getName(), null);
			hostPublicationListenerTracker.open();
		}
		Object[] objs = hostPublicationListenerTracker.getServices();
		if (objs == null)
			return null;
		return (IHostDiscoveryListener[]) Arrays.asList(objs).toArray(
				new IHostDiscoveryListener[] {});
	}

	public synchronized IProxyDiscoveryListener[] getProxyDiscoveredListeners() {
		if (this.context == null)
			return null;
		if (proxyDiscoveredListenerTracker == null) {
			proxyDiscoveredListenerTracker = new ServiceTracker(this.context,
					IProxyDiscoveryListener.class.getName(), null);
			proxyDiscoveredListenerTracker.open();
		}
		Object[] objs = proxyDiscoveredListenerTracker.getServices();
		if (objs == null)
			return null;
		return (IProxyDiscoveryListener[]) Arrays.asList(objs).toArray(
				new IProxyDiscoveryListener[] {});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (discoveredServiceTrackerTracker != null) {
			discoveredServiceTrackerTracker.close();
			discoveredServiceTrackerTracker = null;
		}
		if (locatorTracker != null) {
			locatorTracker.close();
			locatorTracker = null;
		}
		if (advertiserTracker != null) {
			advertiserTracker.close();
			advertiserTracker = null;
		}
		if (servicePublicationTracker != null) {
			servicePublicationTracker.close();
			servicePublicationTracker = null;
		}
		if (servicePublicationHandler != null) {
			servicePublicationHandler.dispose();
			servicePublicationHandler = null;
		}
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		if (hostPublicationListenerTracker != null) {
			hostPublicationListenerTracker.close();
			hostPublicationListenerTracker = null;
		}
		if (proxyDiscoveredListenerTracker != null) {
			proxyDiscoveredListenerTracker.close();
			proxyDiscoveredListenerTracker = null;
		}
		this.context = null;
		plugin = null;
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
			if (locator != null)
				locator.addServiceListener(servicePublicationHandler);
			return locator;
		}

		public void modifiedService(ServiceReference reference, Object service) {
		}

		public void removedService(ServiceReference reference, Object service) {
		}
	}
}
