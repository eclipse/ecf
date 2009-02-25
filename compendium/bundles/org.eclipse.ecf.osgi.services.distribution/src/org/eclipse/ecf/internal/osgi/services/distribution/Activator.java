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

import java.util.Dictionary;
import java.util.Hashtable;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.osgi.services.distribution.ECFServiceConstants;
import org.osgi.framework.*;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.discovery.DiscoveredServiceTracker;
import org.osgi.service.distribution.DistributionProvider;
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

	public static Activator getDefault() {
		return plugin;
	}

	public BundleContext getContext() {
		return context;
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
		this.discoveredServiceTrackerRegistration = this.context
				.registerService(DiscoveredServiceTracker.class.getName(),
						new DiscoveredServiceTrackerImpl(
								this.distributionProvider), null);
	}

	private void addServiceRegistryHooks() {
		// register the event hook to get informed when new services appear
		final EventHookImpl hook = new EventHookImpl(distributionProvider);
		this.eventHookRegistration = this.context.registerService(
				EventHook.class.getName(), hook, null);

		// register all existing services which have the marker property
		try {
			final ServiceReference[] refs = this.context.getServiceReferences(
					null, "(" + ECFServiceConstants.OSGI_REMOTE_INTERFACES
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
		this.distributionProvider = null;
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

}
