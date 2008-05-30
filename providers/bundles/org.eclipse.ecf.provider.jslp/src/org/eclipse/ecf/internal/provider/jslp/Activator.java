/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.jslp;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.Locator;
import org.eclipse.core.runtime.Assert;
import org.osgi.framework.*;

public class Activator implements BundleActivator {
	// The shared instance
	private static Activator plugin;
	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.jslp"; //$NON-NLS-1$

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	// we need to keep a ref on our context
	// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=108214
	private BundleContext bundleContext;

	private LocatorDecorator locator = new NullPatternLocator();
	private Advertiser advertiser = new NullPatternAdvertiser();

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	public Bundle getBundle() {
		return bundleContext.getBundle();
	}

	public LocatorDecorator getLocator() {
		Assert.isNotNull(locator);
		return locator;
	}

	public Advertiser getAdvertiser() {
		Assert.isNotNull(advertiser);
		return advertiser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;

		// initially get the locator and add a life cycle listener
		final ServiceReference lRef = context.getServiceReference(Locator.class.getName());
		if (lRef != null) {
			locator = new LocatorDecoratorImpl((Locator) context.getService(lRef));
		}
		context.addServiceListener(new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
				switch (event.getType()) {
					case ServiceEvent.REGISTERED :
						Object service = bundleContext.getService(event.getServiceReference());
						locator = new LocatorDecoratorImpl((Locator) service);
						break;
					case ServiceEvent.UNREGISTERING :
						locator = new NullPatternLocator();
						break;
				}
			}
		}, "(" + Constants.OBJECTCLASS + "=" + Locator.class.getName() + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

		// initially get the advertiser and add a life cycle listener
		final ServiceReference aRef = context.getServiceReference(Advertiser.class.getName());
		if (aRef != null) {
			advertiser = (Advertiser) context.getService(aRef);
		}
		context.addServiceListener(new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
				switch (event.getType()) {
					case ServiceEvent.REGISTERED :
						advertiser = (Advertiser) bundleContext.getService(event.getServiceReference());
						break;
					case ServiceEvent.UNREGISTERING :
						advertiser = new NullPatternAdvertiser();
						break;
				}
			}
		}, "(" + Constants.OBJECTCLASS + "=" + Advertiser.class.getName() + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

		//TODO-mkuppe https://bugs.eclipse.org/232813
		// register the jSLP discovery service (will be automatically unregistered when this bundle gets uninstalled)
		//		JSLPDiscoveryContainer ids = new JSLPDiscoveryContainer();
		//		ids.connect(null, null);
		//		Properties props = new Properties();
		//		props.put(IDiscoveryService.CONTAINER_ID, ids.getID());
		//		props.put(IDiscoveryContainerAdapter.CONTAINER_CONNECT_TARGET, JSLPDiscoveryContainer.NAME);
		//		context.registerService(IDiscoveryService.class.getName(), ids, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		//TODO-mkuppe here we should do something like a deregisterAll();
		plugin = null;
		bundleContext = null;
	}
}
