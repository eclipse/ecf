/****************************************************************************
 * Copyright (c) 2007 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.jslp;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.Locator;
import java.util.Hashtable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ExtensionRegistryRunnable;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.provider.jslp.container.ContainerInstantiator;
import org.eclipse.ecf.provider.jslp.container.JSLPDiscoveryContainer;
import org.eclipse.ecf.provider.jslp.identity.JSLPNamespace;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

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
	private volatile BundleContext bundleContext;

	private volatile ServiceTracker<Locator, Locator> locatorSt;
	private volatile ServiceTracker<Advertiser, Advertiser> advertiserSt;
	private volatile ServiceRegistration<?> serviceRegistration;

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
		locatorSt.open();
		final Locator aLocator = locatorSt.getService();
		if (aLocator == null) {
			return new NullPatternLocator();
		}
		return new LocatorDecoratorImpl(aLocator);
	}

	public Advertiser getAdvertiser() {
		advertiserSt.open();
		final Advertiser advertiser = advertiserSt.getService();
		if (advertiser == null) {
			return new NullPatternAdvertiser();
		}
		return advertiser;
	}

	private static final boolean ENABLE_JSLP = Boolean.valueOf(System.getProperty("ch.ethz.iks.slp.enable", "false")); //$NON-NLS-1$ //$NON-NLS-2$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		if (ENABLE_JSLP) {
			bundleContext = context;

			SafeRunner.run(new ExtensionRegistryRunnable(context) {
				protected void runWithoutRegistry() throws Exception {
					context.registerService(Namespace.class, new JSLPNamespace(), null);
					context.registerService(ContainerTypeDescription.class, new ContainerTypeDescription("ecf.discovery.jslp", new ContainerInstantiator(), "JSLP Discovery Container", true, false), null); //$NON-NLS-1$//$NON-NLS-2$
					context.registerService(ContainerTypeDescription.class, new ContainerTypeDescription("ecf.discovery.jslp.locator", new ContainerInstantiator(), "JSLP Discovery Locator Container", true, false), null); //$NON-NLS-1$//$NON-NLS-2$
					context.registerService(ContainerTypeDescription.class, new ContainerTypeDescription("ecf.discovery.jslp.advertiser", new ContainerInstantiator(), "JSLP Discovery Advertiser Container", true, false), null); //$NON-NLS-1$//$NON-NLS-2$
				}
			});

			// initially get the locator and add a life cycle listener
			locatorSt = new ServiceTracker<Locator, Locator>(context, Locator.class.getName(), null);

			// initially get the advertiser and add a life cycle listener
			advertiserSt = new ServiceTracker<Advertiser, Advertiser>(context, Advertiser.class.getName(), null);

			// register ourself as an OSGi service
			final Hashtable<String, Object> props = new Hashtable<String, Object>();
			props.put(IDiscoveryService.CONTAINER_NAME, JSLPDiscoveryContainer.NAME);
			props.put(Constants.SERVICE_RANKING, Integer.valueOf(500));

			String[] clazzes = new String[] {IDiscoveryService.class.getName(), IDiscoveryLocator.class.getName(), IDiscoveryAdvertiser.class.getName()};
			serviceRegistration = context.registerService(clazzes, serviceFactory, props);
		}
	}

	private final DiscoveryServiceFactory serviceFactory = new DiscoveryServiceFactory();

	class DiscoveryServiceFactory implements ServiceFactory {
		private volatile JSLPDiscoveryContainer jdc;

		/* (non-Javadoc)
		 * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)
		 */
		public Object getService(final Bundle bundle, final ServiceRegistration registration) {
			if (jdc == null) {
				try {
					jdc = new JSLPDiscoveryContainer();
					jdc.connect(null, null);
				} catch (final ContainerConnectException e) {
					Trace.catching(Activator.PLUGIN_ID, Activator.PLUGIN_ID + "/debug/methods/tracing", this.getClass(), "getService(Bundle, ServiceRegistration)", e); //$NON-NLS-1$ //$NON-NLS-2$
					jdc = null;
				}
			}
			return jdc;
		}

		/**
		 * @return false if this factory has never created a service instance, true otherwise
		 */
		public boolean isActive() {
			return jdc != null;
		}

		/* (non-Javadoc)
		 * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration, java.lang.Object)
		 */
		public void ungetService(final Bundle bundle, final ServiceRegistration registration, final Object service) {
			//TODO-mkuppe we later might want to dispose jSLP when the last!!! consumer ungets the service 
			//Though don't forget about the (ECF) Container which might still be in use
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		//TODO-mkuppe here we should do something like a deregisterAll(), but see ungetService(...);
		if (serviceRegistration != null && serviceFactory.isActive()) {
			ServiceReference<?> reference = serviceRegistration.getReference();
			IDiscoveryLocator aLocator = (IDiscoveryLocator) context.getService(reference);

			serviceRegistration.unregister();

			IContainer container = aLocator.getAdapter(IContainer.class);
			container.disconnect();
			container.dispose();

			serviceRegistration = null;
		}
		plugin = null;
		bundleContext = null;
		if (advertiserSt != null) {
			advertiserSt.close();
			advertiserSt = null;
		}
		if (locatorSt != null) {
			locatorSt.close();
			locatorSt = null;
		}
	}
}
