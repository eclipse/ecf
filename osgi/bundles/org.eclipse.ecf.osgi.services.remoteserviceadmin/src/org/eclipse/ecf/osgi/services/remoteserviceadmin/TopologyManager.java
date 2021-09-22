/****************************************************************************
 * Copyright (c) 2017 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointEvent;
import org.osgi.service.remoteserviceadmin.EndpointEventListener;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

/**
 * @since 4.6
 */
public class TopologyManager implements EventListenerHook, RemoteServiceAdminListener, ITopologyManager {

	class EndpointEventHolder {
		private final EndpointDescription endpointDescription;
		private final String filter;

		public EndpointEventHolder(EndpointDescription d, String f) {
			this.endpointDescription = d;
			this.filter = f;
		}

		public EndpointDescription getEndpoint() {
			return this.endpointDescription;
		}

		public String getFilter() {
			return this.filter;
		}
	}

	class ProxyEndpointEventListener implements EndpointEventListener {

		private final Bundle bundle;

		public ProxyEndpointEventListener(Bundle b) {
			this.bundle = b;
		}

		public void endpointChanged(EndpointEvent event, String filter) {
			int type = event.getType();
			if (type == EndpointEvent.ADDED) {
				synchronized (bundleEndpointEventListenerMap) {
					List<EndpointEventHolder> endpointEventHolders = bundleEndpointEventListenerMap.get(this.bundle);
					if (endpointEventHolders == null)
						endpointEventHolders = new ArrayList<EndpointEventHolder>();
					endpointEventHolders.add(new EndpointEventHolder(event.getEndpoint(), filter));
					bundleEndpointEventListenerMap.put(this.bundle, endpointEventHolders);
				}
			} else if (type == EndpointEvent.REMOVED) {
				synchronized (bundleEndpointEventListenerMap) {
					List<EndpointEventHolder> endpointEventHolders = bundleEndpointEventListenerMap.get(this.bundle);
					if (endpointEventHolders != null) {
						for (Iterator<EndpointEventHolder> i = endpointEventHolders.iterator(); i.hasNext();) {
							EndpointEventHolder eh = i.next();
							EndpointDescription oldEd = eh.getEndpoint();
							EndpointDescription newEd = event.getEndpoint();
							if (oldEd.equals(newEd))
								i.remove();
						}
						if (endpointEventHolders.size() == 0)
							bundleEndpointEventListenerMap.remove(this.bundle);
					}

				}
			}
			deliverSafe(event, filter);
		}

		private void logError(String methodName, String message, Throwable e) {
			LogUtility.logError(((methodName == null) ? "<unknown>" //$NON-NLS-1$
					: methodName), DebugOptions.TOPOLOGY_MANAGER, TopologyManager.class,
					((message == null) ? "<empty>" //$NON-NLS-1$
							: message),
					e);
		}

		private void deliverSafe(final EndpointEvent endpointEvent, final String matchingFilter) {
			final EndpointEventListener listener = topologyManagerImpl;
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					if (listener != null)
						listener.endpointChanged(endpointEvent, matchingFilter);
				}

				public void handleException(Throwable exception) {
					String message = "Exception in EndpointEventListener listener=" //$NON-NLS-1$
							+ listener + " event=" //$NON-NLS-1$
							+ endpointEvent + " matchingFilter=" //$NON-NLS-1$
							+ matchingFilter;
					logError("deliverSafe", message, exception); //$NON-NLS-1$
				};
			});
		}

		public void deliverRemoveEventForBundle(EndpointEventHolder eventHolder) {
			deliverSafe(new EndpointEvent(EndpointEvent.REMOVED, eventHolder.getEndpoint()), eventHolder.getFilter());
		}
	}

	class ProxyEndpointListener implements EndpointListener {

		private final Bundle bundle;

		public ProxyEndpointListener(Bundle b) {
			this.bundle = b;
		}

		private void logError(String methodName, String message, Throwable e) {
			LogUtility.logError(((methodName == null) ? "<unknown>" //$NON-NLS-1$
					: methodName), DebugOptions.TOPOLOGY_MANAGER, TopologyManager.class,
					((message == null) ? "<empty>" //$NON-NLS-1$
							: message),
					e);
		}

		private void deliverSafe(final EndpointDescription endpoint, final String matchingFilter, boolean added) {
			final EndpointListener listener = topologyManagerImpl;
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					if (added) {
						listener.endpointAdded(endpoint, matchingFilter);
					} else {
						listener.endpointRemoved(endpoint, matchingFilter);
					}
				}

				public void handleException(Throwable exception) {
					String message = "Exception in EndpointListener listener=" //$NON-NLS-1$
							+ listener + " endpoint=" //$NON-NLS-1$
							+ endpoint + " matchingFilter=" //$NON-NLS-1$
							+ matchingFilter + " added=" //$NON-NLS-1$
							+ added;
					logError("deliverSafe", message, exception); //$NON-NLS-1$
				};
			});
		}

		public void deliverRemoveEventForBundle(EndpointEventHolder eventHolder) {
			deliverSafe(eventHolder.getEndpoint(), eventHolder.getFilter(), false);
		}

		@Override
		public void endpointAdded(EndpointDescription endpoint, String matchedFilter) {
			synchronized (bundleEndpointEventListenerMap) {
				List<EndpointEventHolder> endpointEventHolders = bundleEndpointEventListenerMap.get(this.bundle);
				if (endpointEventHolders == null)
					endpointEventHolders = new ArrayList<EndpointEventHolder>();
				endpointEventHolders.add(new EndpointEventHolder(endpoint, matchedFilter));
				bundleEndpointEventListenerMap.put(this.bundle, endpointEventHolders);
			}
			deliverSafe(endpoint, matchedFilter, true);
		}

		@Override
		public void endpointRemoved(EndpointDescription endpoint, String matchedFilter) {
			synchronized (bundleEndpointEventListenerMap) {
				List<EndpointEventHolder> endpointEventHolders = bundleEndpointEventListenerMap.get(this.bundle);
				if (endpointEventHolders != null) {
					for (Iterator<EndpointEventHolder> i = endpointEventHolders.iterator(); i.hasNext();) {
						EndpointEventHolder eh = i.next();
						EndpointDescription oldEd = eh.getEndpoint();
						EndpointDescription newEd = endpoint;
						if (oldEd.equals(newEd))
							i.remove();
					}
					if (endpointEventHolders.size() == 0)
						bundleEndpointEventListenerMap.remove(this.bundle);
				}
			}
			deliverSafe(endpoint, matchedFilter, false);
		}
	}

	private Map<Bundle, List<EndpointEventHolder>> bundleEndpointEventListenerMap = new HashMap<Bundle, List<EndpointEventHolder>>();

	protected TopologyManagerImpl topologyManagerImpl;
	protected ServiceRegistration<?> endpointListenerRegistration;

	ServiceRegistration<?> legacyEndpointListenerRegistration;

	String getFrameworkUUID(BundleContext context) {
		synchronized ("org.osgi.framework.uuid") { //$NON-NLS-1$
			String result = context.getProperty("org.osgi.framework.uuid"); //$NON-NLS-1$
			if (result == null) {
				UUID newUUID = UUID.randomUUID();
				result = newUUID.toString();
				System.setProperty("org.osgi.framework.uuid", //$NON-NLS-1$
						newUUID.toString());
			}
			return result;
		}
	}

	/**
	 * @since 4.9
	 */
	protected TopologyManagerImpl createTopologyManagerImpl(BundleContext context, boolean allowLocalhost, String[] extraFilters) {
		return new TopologyManagerImpl(context, allowLocalhost, extraFilters);
	}
	
	protected void activate(BundleContext context, Map<String, ?> properties) throws Exception {

		Boolean allowLocalhost = (Boolean) properties.get(ENDPOINT_ALLOWLOCALHOST_PROP);
		if (allowLocalhost == null)
			allowLocalhost = ENDPOINT_ALLOWLOCALHOST;

		String extraFilters = (String) properties.get(ENDPOINT_EXTRA_FILTERS_PROP);
		extraFilters = (extraFilters != null) ? extraFilters : ENDPOINT_EXTRA_FILTERS;

		String[] extraFiltersArr = null;
		if (extraFilters != null)
			extraFiltersArr = extraFilters.split(","); //$NON-NLS-1$

		this.topologyManagerImpl = createTopologyManagerImpl(context, allowLocalhost, extraFiltersArr);
		
		Dictionary<String, Object> props = createEndpointListenerProps(
				Arrays.asList(this.topologyManagerImpl.getScope()));

		endpointListenerRegistration = context.registerService(EndpointEventListener.class,
				new ServiceFactory<EndpointEventListener>() {
					public EndpointEventListener getService(Bundle bundle,
							ServiceRegistration<EndpointEventListener> registration) {
						return new ProxyEndpointEventListener(bundle);
					}

					public void ungetService(Bundle bundle, ServiceRegistration<EndpointEventListener> registration,
							EndpointEventListener service) {
						ProxyEndpointEventListener peel = (service instanceof ProxyEndpointEventListener)
								? (ProxyEndpointEventListener) service
								: null;
						if (peel == null)
							return;
						synchronized (bundleEndpointEventListenerMap) {
							List<EndpointEventHolder> endpointEventHolders = bundleEndpointEventListenerMap.get(bundle);
							if (endpointEventHolders != null)
								for (EndpointEventHolder eh : endpointEventHolders)
									peel.deliverRemoveEventForBundle(eh);
						}
					}
				}, (Dictionary<String, Object>) props);

		legacyEndpointListenerRegistration = context.registerService(EndpointListener.class,
				new ServiceFactory<EndpointListener>() {
					public EndpointListener getService(Bundle bundle,
							ServiceRegistration<EndpointListener> registration) {
						return new ProxyEndpointListener(bundle);
					}

					public void ungetService(Bundle bundle, ServiceRegistration<EndpointListener> registration,
							EndpointListener service) {
						ProxyEndpointListener peel = (service instanceof ProxyEndpointListener)
								? (ProxyEndpointListener) service
								: null;
						if (peel == null)
							return;
						synchronized (bundleEndpointEventListenerMap) {
							List<EndpointEventHolder> endpointEventHolders = bundleEndpointEventListenerMap.get(bundle);
							if (endpointEventHolders != null)
								for (EndpointEventHolder eh : endpointEventHolders)
									peel.deliverRemoveEventForBundle(eh);
						}
					}
				}, (Dictionary<String, Object>) props);

		String exportRegisteredSvcsFilter = (String) properties.get(EXPORT_REGISTERED_SERVICES_FILTER_PROP);
		if (exportRegisteredSvcsFilter == null)
			exportRegisteredSvcsFilter = EXPORT_REGISTERED_SERVICES_FILTER;

		this.topologyManagerImpl.exportRegisteredServices(exportRegisteredSvcsFilter);
	}

	protected Dictionary<String, Object> createEndpointListenerProps(List<String> filters) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(org.osgi.service.remoteserviceadmin.EndpointEventListener.ENDPOINT_LISTENER_SCOPE,
				filters.toArray(new String[filters.size()]));
		return props;
	}

	protected void deactivate() {
		if (legacyEndpointListenerRegistration != null) {
			legacyEndpointListenerRegistration.unregister();
			legacyEndpointListenerRegistration = null;
		}
		if (endpointListenerRegistration != null) {
			endpointListenerRegistration.unregister();
			endpointListenerRegistration = null;
		}
		if (this.topologyManagerImpl != null) {
			this.topologyManagerImpl.close();
			this.topologyManagerImpl = null;
		}
	}

	// RemoteServiceAdminListener impl
	public void remoteAdminEvent(RemoteServiceAdminEvent event) {
		topologyManagerImpl.handleRemoteAdminEvent(event);
	}

	// EventListenerHook impl
	public void event(ServiceEvent event, @SuppressWarnings("rawtypes") Map listeners) {
		topologyManagerImpl.handleEvent(event, listeners);
	}

	public String[] getEndpointFilters() {
		return topologyManagerImpl.getScope();
	}

	public String[] setEndpointFilters(String[] newFilters) {
		// XXX return null for now
		return null;
	}

}
