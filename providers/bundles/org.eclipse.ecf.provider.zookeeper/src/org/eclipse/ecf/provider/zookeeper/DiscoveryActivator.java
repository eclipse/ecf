/*******************************************************************************
 *  Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Ahmed Aadel - initial API and implementation     
 *******************************************************************************/
package org.eclipse.ecf.provider.zookeeper;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainer;
import org.eclipse.ecf.provider.zookeeper.core.IDiscoveryConfig;
import org.eclipse.ecf.provider.zookeeper.core.internal.BundleStoppingListener;
import org.eclipse.ecf.provider.zookeeper.util.Logger;
import org.eclipse.ecf.provider.zookeeper.util.PrettyPrinter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
public class DiscoveryActivator implements BundleActivator {

	private static BundleContext context;
	private ServiceRegistration discoveryRegistration;
	private static Set<BundleStoppingListener> stopListeners = new HashSet<BundleStoppingListener>();
	private ServiceTracker confTracker, logServiceTraker;

	public void start(final BundleContext c) {
		context = c;
		Properties props = new Properties();
		props.put(IDiscoveryLocator.CONTAINER_NAME, ZooDiscoveryContainerInstantiator.NAME);
		props.put(IDiscoveryAdvertiser.CONTAINER_NAME,
				ZooDiscoveryContainerInstantiator.NAME);
		/*
		 * Make us available as IDiscoveryLocator and IDiscoveryAdvertiser
		 * services for OSGi trackers
		 */
		this.discoveryRegistration = c.registerService(new String[] {
				IDiscoveryLocator.class.getName(),
				IDiscoveryAdvertiser.class.getName() }, ZooDiscoveryContainer
				.getSingleton(), props);
		ZooDiscoveryContainer.getSingleton().setDiscoveryProperties(props);
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			public void run() {
				// track discovery container configuration
				Filter filter = null;
				try {
					filter = context.createFilter("(|(" //$NON-NLS-1$
							+ IDiscoveryConfig.ZOODISCOVERY_FLAVOR_CENTRALIZED
							+ "=*)" //$NON-NLS-1$
							+ "(" //$NON-NLS-1$
							+ IDiscoveryConfig.ZOODISCOVERY_FLAVOR_REPLICATED
							+ "=*)" //$NON-NLS-1$
							+ "(" //$NON-NLS-1$
							+ IDiscoveryConfig.ZOODISCOVERY_FLAVOR_STANDALONE
							+ "=*)" + "(" //$NON-NLS-1$ //$NON-NLS-2$							
							+ Constants.OBJECTCLASS + "=" //$NON-NLS-1$
							+ IDiscoveryConfig.class.getName() + "))"); //$NON-NLS-1$
				} catch (InvalidSyntaxException e) {
					e.printStackTrace();
				}
				DiscoveryActivator.this.confTracker = new ServiceTracker(
						context, filter, null) {
					public Object addingService(ServiceReference reference) {
						ZooDiscoveryContainer.getSingleton().init(reference);
						return super.addingService(reference);
					}
				};
				confTracker.open(true);
			}
		});
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			public void run() {
				// track OSGi log services
				DiscoveryActivator.this.logServiceTraker = new ServiceTracker(
						context, org.osgi.service.log.LogService.class
								.getName(), null) {
					public Object addingService(ServiceReference reference) {
						Logger.bindLogService((LogService) context
								.getService(reference));
						return super.addingService(reference);
					}

					public void removedService(ServiceReference reference,
							Object service) {
						Logger.unbindLogService((LogService) service);
						removedService(reference, service);
					}
				};
				logServiceTraker.open(true);
			}
		});

		// prompt hi!
		PrettyPrinter.prompt(PrettyPrinter.ACTIVATED, null);

	}

	public void stop(BundleContext c) throws Exception {
		dispose();
	}

	private void dispose() {
		for (BundleStoppingListener l : stopListeners) {
			l.bundleStopping();
		}
		stopListeners.clear();
		ZooDiscoveryContainer.getSingleton().shutdown();
		if (this.discoveryRegistration != null) {
			this.discoveryRegistration.unregister();
		}
		if (this.confTracker != null) {
			this.confTracker.close();
		}
		// Initiates an orderly shutdown of all our cached threads
		ZooDiscoveryContainer.CACHED_THREAD_POOL.shutdown();
	}

	public static BundleContext getContext() {
		return context;
	}

	public static void registerBundleStoppingListner(BundleStoppingListener l) {
		stopListeners.add(l);
	}

}
