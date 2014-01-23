/*******************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.discovery;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.util.StringUtils;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.equinox.concurrent.future.*;
import org.osgi.framework.*;

public class DiscoveryServiceListener implements ServiceListener {

	private final AbstractDiscoveryContainerAdapter discoveryContainer;
	private final Class listenerClass;
	private final BundleContext context;
	private final IServiceIDFactory idFactory;
	private final Namespace discoveryNamespace;

	// Need a fair lock to gurantee ordering of add and remove events
	private final ILock cacheLock;
	private final Set cache = new HashSet();
	private final IExecutor executor;
	private final IServiceListener cachingListener = new IServiceListener() {

		public void serviceUndiscovered(IServiceEvent anEvent) {
			try {
				cacheLock.acquire();
				cache.remove(anEvent);
			} finally {
				cacheLock.release();
			}
		}

		public void serviceDiscovered(IServiceEvent anEvent) {
			try {
				cacheLock.acquire();
				cache.add(anEvent);
			} finally {
				cacheLock.release();
			}
		}
	};
	private final DiscoveryContainerConfig config;

	public DiscoveryServiceListener(
			AbstractDiscoveryContainerAdapter anAbstractDiscoveryContainerAdapter,
			Class clazz, DiscoveryContainerConfig config) {

		this.config = config;
		executor = new ThreadsExecutor();
		cacheLock = Job.getJobManager().newLock();

		discoveryContainer = anAbstractDiscoveryContainerAdapter;

		// Add a cache to the discovery container
		discoveryContainer.addServiceListener(cachingListener);

		listenerClass = clazz;
		discoveryNamespace = IDFactory.getDefault().getNamespaceByName(
				DiscoveryNamespace.NAME);
		idFactory = ServiceIDFactory.getDefault();
		context = DiscoveryPlugin.getDefault().getBundleContext();
		try {
			// get existing listener
			final ServiceReference[] references = context.getServiceReferences(
					listenerClass.getName(), null);
			addServiceListener(references);

			// listen for more listeners
			context.addServiceListener(this, getFilter());
		} catch (InvalidSyntaxException e) {
			DiscoveryPlugin.getDefault().log(
					new Status(IStatus.ERROR, DiscoveryPlugin.PLUGIN_ID,
							IStatus.ERROR, "Cannot create filter", e)); //$NON-NLS-1$
		}
	}

	public void dispose() {
		discoveryContainer.removeServiceListener(cachingListener);
		purgeCache();

		if (!DiscoveryPlugin.isStopped()) {
			context.removeServiceListener(this);
		}
	}

	private void addServiceListener(ServiceReference[] references) {
		if (references == null) {
			return;
		}
		final Map futures = new HashMap(references.length);
		for (int i = 0; i < references.length; i++) {
			final ServiceReference serviceReference = references[i];
			if (listenerClass.getName()
					.equals(IServiceListener.class.getName())) {
				final IServiceTypeID aType = getIServiceTypeID(serviceReference);
				if (aType == null) {
					continue;
				}
				final IServiceListener aListener = (IServiceListener) context
						.getService(serviceReference);
				discoveryContainer.addServiceListener(aType, aListener);

				// Notify newly registered ISI of previously discovered services
				if (serviceReference.getProperty(IServiceListener.Cache.USE) != null) {
					for (Iterator iterator = cache.iterator(); iterator
							.hasNext();) {
						final IServiceEvent event = (IServiceEvent) iterator
								.next();
						// if
						// (aType.equals(event.getServiceInfo().getServiceID()
						// .getServiceTypeID())) {
						aListener.serviceDiscovered(event);
						// }
					}
				}
				if (Boolean.TRUE.equals(serviceReference
						.getProperty(IServiceListener.Cache.REFRESH))) {
					// Just trigger re-discovery without caring for the result
					futures.put(discoveryContainer.getAsyncServices(),
							aListener);
				}
			} else {
				final IServiceTypeListener aListener = (IServiceTypeListener) context
						.getService(serviceReference);
				discoveryContainer.addServiceTypeListener(aListener);
			}

			// Finally notify all listeners
			if (futures.size() > 0) {
				final IProgressRunnable runnable = new IProgressRunnable() {
					public Object run(final IProgressMonitor arg0)
							throws Exception {
						for (final Iterator iterator = futures.keySet()
								.iterator(); iterator.hasNext();) {
							final IFuture f = (IFuture) iterator.next();
							final IServiceListener listener = (IServiceListener) futures
									.get(f);
							final IServiceInfo[] infos = (IServiceInfo[]) f
									.get();
							for (int i = 0; i < infos.length; i++) {
								listener.serviceDiscovered(new ServiceContainerEvent(
										infos[i], config.getID()));
							}
						}
						return null;
					}
				};
				executor.execute(runnable, null);
			}
		}
	}

	private void addServiceListener(ServiceReference reference) {
		addServiceListener(new ServiceReference[] { reference });
	}

	private void removeServiceListener(ServiceReference[] references) {
		if (references == null) {
			return;
		}
		for (int i = 0; i < references.length; i++) {
			final ServiceReference serviceReference = references[i];
			if (listenerClass.getName()
					.equals(IServiceListener.class.getName())) {
				final IServiceTypeID aType = getIServiceTypeID(serviceReference);
				if (aType == null) {
					continue;
				}
				final IServiceListener aListener = (IServiceListener) context
						.getService(serviceReference);
				discoveryContainer.removeServiceListener(aType, aListener);
			} else {
				final IServiceTypeListener aListener = (IServiceTypeListener) context
						.getService(serviceReference);
				discoveryContainer.removeServiceTypeListener(aListener);
			}
		}
	}

	private void removeServiceListener(ServiceReference reference) {
		removeServiceListener(new ServiceReference[] { reference });
	}

	private IServiceTypeID getIServiceTypeID(ServiceReference serviceReference) {
		String namingAuthority = (String) serviceReference
				.getProperty("org.eclipse.ecf.discovery.namingauthority"); //$NON-NLS-1$
		if (namingAuthority == null) {
			namingAuthority = "*"; //$NON-NLS-1$
		}
		try {
			final IServiceTypeID createServiceTypeID = idFactory
					.createServiceTypeID(
							discoveryNamespace,
							convert(serviceReference,
									"org.eclipse.ecf.discovery.services"), //$NON-NLS-1$
							convert(serviceReference,
									"org.eclipse.ecf.discovery.scopes"), //$NON-NLS-1$
							convert(serviceReference,
									"org.eclipse.ecf.discovery.protocols"), //$NON-NLS-1$
							namingAuthority);
			return createServiceTypeID;
		} catch (final IDCreateException e) {
			return null;
		}
	}

	private String[] convert(ServiceReference serviceReference, String key) {
		final Object value = serviceReference.getProperty(key);
		// default to wildcard for non-set values
		if (value == null) {
			return new String[] { "*" }; //$NON-NLS-1$
		} else if (value instanceof String[]) {
			return (String[]) value;
		}
		return StringUtils.split((String) value, "._"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
	 * ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		// ignore events that are targeted at different discovery containers
		final Object containerName = event.getServiceReference().getProperty(
				IDiscoveryLocator.CONTAINER_NAME);
		if (!discoveryContainer.getContainerName().equals(containerName)) {
			return;
		}
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
			addServiceListener(event.getServiceReference());
			break;
		case ServiceEvent.UNREGISTERING:
			removeServiceListener(event.getServiceReference());
			break;
		default:
			break;
		}
	}

	private String getFilter() {
		return "(" + Constants.OBJECTCLASS + "=" + listenerClass.getName() //$NON-NLS-1$ //$NON-NLS-2$
				+ ")"; //$NON-NLS-1$
	}

	public void purgeCache() {
		try {
			cacheLock.acquire();
			cache.clear();
		} finally {
			cacheLock.release();
		}
	}
}
