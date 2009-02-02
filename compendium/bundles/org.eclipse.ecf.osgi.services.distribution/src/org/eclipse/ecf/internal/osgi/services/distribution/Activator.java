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

import org.eclipse.ecf.core.IContainerManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.distribution.DistributionProvider;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.distribution";

	private static Activator plugin;
	private BundleContext context;

	private ServiceRegistration eventHookRegistration;
	private ServiceRegistration distributionProviderRegistration;

	private DistributionProviderImpl distributionProvider;

	private ServiceTracker containerManagerTracker;

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
		addServiceRegistryHooks();
		addDistributionProvider();
	}

	private void addServiceRegistryHooks() {
		this.eventHookRegistration = this.context.registerService(
				EventHook.class.getName(), new ECFEventHookImpl(
						distributionProvider), null);
	}

	private void addDistributionProvider() {
		this.distributionProviderRegistration = this.context.registerService(
				DistributionProvider.class.getName(), distributionProvider,
				null);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext ctxt) throws Exception {
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
