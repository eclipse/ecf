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
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.util.*;
import org.eclipse.ecf.osgi.services.distribution.IRemoteServiceContainerFinder;
import org.eclipse.ecf.osgi.services.distribution.IServiceConstants;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;
import org.osgi.framework.*;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.discovery.DiscoveredServiceTracker;
import org.osgi.service.distribution.DistributionProvider;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.distribution";

	private static Activator plugin;
	private BundleContext context;

	private ServiceTracker containerManagerTracker;

	private DistributionProviderImpl distributionProvider;

	private ServiceRegistration eventHookRegistration;
	private ServiceRegistration distributionProviderRegistration;
	private ServiceRegistration discoveredServiceTrackerRegistration;
	private ServiceRegistration rsContainerFinderRegistration;

	private ServiceTracker logServiceTracker = null;
	private LogService logService = null;

	private ServiceTracker adapterManagerTracker;

	private ServiceTracker rsContainerFinderTracker;

	public static Activator getDefault() {
		return plugin;
	}

	public BundleContext getContext() {
		return context;
	}

	protected LogService getLogService() {
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
			logService.log(null, LogHelper.getLogCode(status), LogHelper
					.getLogMessage(status), status.getException());
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
		this.distributionProvider = new DistributionProviderImpl();
		addDiscoveredServiceTracker();
		addServiceRegistryHooks();
		addDistributionProvider();
	}

	private void addDiscoveredServiceTracker() {
		DiscoveredServiceTrackerImpl dstImpl = new DiscoveredServiceTrackerImpl(
				this.distributionProvider, new ThreadsExecutor());
		this.discoveredServiceTrackerRegistration = this.context
				.registerService(DiscoveredServiceTracker.class.getName(),
						dstImpl, null);
		this.rsContainerFinderRegistration = this.context.registerService(
				IRemoteServiceContainerFinder.class.getName(), dstImpl, null);
	}

	private void addServiceRegistryHooks() {
		// register the event hook to get informed when new services appear
		final EventHookImpl hook = new EventHookImpl(distributionProvider);
		this.eventHookRegistration = this.context.registerService(
				EventHook.class.getName(), hook, null);

		// register all existing services which have the marker property
		try {
			final ServiceReference[] refs = this.context.getServiceReferences(
					null, "(" + IServiceConstants.OSGI_REMOTE_INTERFACES
							+ "=*)");
			if (refs != null) {
				for (int i = 0; i < refs.length; i++) {
					hook.handleRegisteredServiceEvent(refs[i], null);
				}
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	private void addDistributionProvider() {
		final Dictionary properties = new Hashtable();
		properties.put(DistributionProvider.PROP_KEY_VENDOR_NAME,
				DistributionProviderImpl.VENDOR_NAME);
		properties.put(DistributionProvider.PROP_KEY_PRODUCT_NAME,
				DistributionProviderImpl.PRODUCT_NAME);
		properties.put(DistributionProvider.PROP_KEY_PRODUCT_VERSION,
				DistributionProviderImpl.PRODUCT_VERSION);
		properties.put(DistributionProvider.PROP_KEY_SUPPORTED_INTENTS,
				distributionProvider.getSupportedIntents());
		this.distributionProviderRegistration = this.context.registerService(
				DistributionProvider.class.getName(), distributionProvider,
				properties);
	}

	private void removeServiceRegistryHooks() {
		if (this.eventHookRegistration != null) {
			this.eventHookRegistration.unregister();
			this.eventHookRegistration = null;
		}
	}

	private void removeDistributionProvider() {
		if (this.distributionProviderRegistration != null) {
			this.distributionProviderRegistration.unregister();
			this.distributionProviderRegistration = null;
		}
	}

	private void removeDiscoveredServiceTracker() {
		if (this.discoveredServiceTrackerRegistration != null) {
			this.discoveredServiceTrackerRegistration.unregister();
			this.discoveredServiceTrackerRegistration = null;
		}
		if (this.rsContainerFinderRegistration != null) {
			this.rsContainerFinderRegistration.unregister();
			this.rsContainerFinderRegistration = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext ctxt) throws Exception {
		removeDiscoveredServiceTracker();
		removeDistributionProvider();
		removeServiceRegistryHooks();
		if (containerManagerTracker != null) {
			containerManagerTracker.close();
			containerManagerTracker = null;
		}
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		if (adapterManagerTracker != null) {
			adapterManagerTracker.close();
			adapterManagerTracker = null;
		}
		if (distributionProvider != null) {
			distributionProvider.dispose();
			distributionProvider = null;
		}
		if (rsContainerFinderTracker != null) {
			rsContainerFinderTracker.close();
			rsContainerFinderTracker = null;
		}
		this.context = null;
		plugin = null;
	}

	public IContainerManager getContainerManager() {
		if (containerManagerTracker == null) {
			containerManagerTracker = new ServiceTracker(this.context,
					IContainerManager.class.getName(), null);
			containerManagerTracker.open();
		}
		return (IContainerManager) containerManagerTracker.getService();
	}

	public synchronized IRemoteServiceContainerFinder[] getRemoteServiceContainerFinders() {
		if (rsContainerFinderTracker == null) {
			rsContainerFinderTracker = new ServiceTracker(this.context,
					IRemoteServiceContainerFinder.class.getName(), null);
			rsContainerFinderTracker.open();
		}
		Object[] svcs = (Object[]) rsContainerFinderTracker.getServices();
		return (IRemoteServiceContainerFinder[]) Arrays.asList(svcs).toArray(
				new IRemoteServiceContainerFinder[] {});
	}

	public IAdapterManager getAdapterManager() {
		// First, try to get the adapter manager via
		if (adapterManagerTracker == null) {
			adapterManagerTracker = new ServiceTracker(this.context,
					IAdapterManager.class.getName(), null);
			adapterManagerTracker.open();
		}
		IAdapterManager adapterManager = (IAdapterManager) adapterManagerTracker
				.getService();
		// Then, if the service isn't there, try to get from Platform class via
		// PlatformHelper class
		if (adapterManager == null)
			adapterManager = PlatformHelper.getPlatformAdapterManager();
		return adapterManager;
	}
}
