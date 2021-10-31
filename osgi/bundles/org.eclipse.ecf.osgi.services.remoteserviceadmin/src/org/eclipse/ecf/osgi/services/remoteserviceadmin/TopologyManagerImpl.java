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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointEvent;
import org.osgi.service.remoteserviceadmin.EndpointEventListener;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * @since 4.6
 */
public class TopologyManagerImpl extends AbstractTopologyManager implements EndpointListener, EndpointEventListener {

	public static final int STARTUP_WAIT_TIME = Integer
			.getInteger("org.eclipse.ecf.osgi.services.remoteserviceadmin.startupWaitTime", 20000); //$NON-NLS-1$

	/**
	 * @since 4.9
	 */
	private static final String ECF_DEFAULT_NAMESPACE = System.getProperty(TopologyManagerImpl.class.getName() + ".defaultNamespace","org.eclipse.ecf.core.identity.StringID"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * @since 4.9
	 */
	private String ecfLocalEndpointListenerScope = System.getProperty(TopologyManager.class.getName() + ".ecfLocalEndpointListenerScope"); //$NON-NLS-1$
	private String ecfNonLocalEndpointListenerScope = System.getProperty(TopologyManager.class.getName() + ".ecfNonLocalEndpointListenerScope"); //$NON-NLS-1$
	private String nonECFLocalEndpointListenerScope  = System.getProperty(TopologyManager.class.getName() + ".nonECFLocalEndpointListenerScope"); //$NON-NLS-1$
	private String nonECFNonLocalEndpointListenerScope  = System.getProperty(TopologyManager.class.getName() + ".nonECFNonLocalEndpointListenerScope"); //$NON-NLS-1$

	private boolean nonECFTopologyManager = Boolean.valueOf(System.getProperty(TopologyManagerImpl.class.getName() + ".nonECFTopologyMananger","false")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$
	
	private String processFrameworkLocal(String input) {
		return input.replaceAll("<<LOCAL>>", getFrameworkUUID()); //$NON-NLS-1$
	}
	
	private boolean allowLocalHost;
	private List<String> otherFilters;
	
	boolean isNonECFTopologyManager() {
		return nonECFTopologyManager;
	}
	
	public TopologyManagerImpl(BundleContext context) {
		this(context,false, (String[]) null);
	}
	/**
	 * @since 4.9
	 */
	public TopologyManagerImpl(BundleContext context, boolean allowLocalHost, String...otherFilters) {
		super(context);
		this.allowLocalHost = (nonECFTopologyManager)?true:allowLocalHost;
		String frameworkUUID = getFrameworkUUID();
		String localFrameworkFilter = new StringBuffer("(").append(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID).append("=").append(frameworkUUID).append(")").toString();  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String nonLocalFrameworkFilter = new StringBuffer("(!" + localFrameworkFilter + ")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.ecfLocalEndpointListenerScope == null) {
			this.ecfLocalEndpointListenerScope = new StringBuffer("(&").append(localFrameworkFilter).append(ITopologyManager.ONLY_ECF_SCOPE).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			// replace all occurrences of '<<LOCAL>>' with frameworkUUID
			this.ecfLocalEndpointListenerScope = processFrameworkLocal(this.ecfLocalEndpointListenerScope);
		}
		if (this.ecfNonLocalEndpointListenerScope == null) {
			this.ecfNonLocalEndpointListenerScope = new StringBuffer("(&").append(nonLocalFrameworkFilter).append(ITopologyManager.ONLY_ECF_SCOPE).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			// replace all occurrences of '<<LOCAL>>' with frameworkUUID
			this.ecfNonLocalEndpointListenerScope = processFrameworkLocal(this.ecfNonLocalEndpointListenerScope);
		}
			String nonECFScope = "(!" + ITopologyManager.ONLY_ECF_SCOPE + ")";  //$NON-NLS-1$//$NON-NLS-2$
			if (this.nonECFLocalEndpointListenerScope == null) {
				this.nonECFLocalEndpointListenerScope = new StringBuffer("(&").append(localFrameworkFilter).append(nonECFScope).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// replace all occurrences of '<<LOCAL>>' with frameworkUUID
				this.nonECFLocalEndpointListenerScope = processFrameworkLocal(this.nonECFLocalEndpointListenerScope);
			}
			if (this.nonECFNonLocalEndpointListenerScope == null) {
				this.nonECFNonLocalEndpointListenerScope = new StringBuffer("(&").append(localFrameworkFilter).append(nonECFScope).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// replace all occurrences of '<<LOCAL>>' with frameworkUUID
				this.nonECFNonLocalEndpointListenerScope = processFrameworkLocal(this.nonECFNonLocalEndpointListenerScope);
			}
		this.otherFilters = otherFilters == null?new ArrayList<String>():Arrays.asList(otherFilters);
	}

	protected String getFrameworkUUID() {
		synchronized ("org.osgi.framework.uuid") { //$NON-NLS-1$
			String result = getContext().getProperty("org.osgi.framework.uuid"); //$NON-NLS-1$
			if (result == null) {
				UUID newUUID = UUID.randomUUID();
				result = newUUID.toString();
				System.setProperty("org.osgi.framework.uuid", //$NON-NLS-1$
						newUUID.toString());
			}
			return result;
		}
	}

	String[] getScope() {
		synchronized (this) {
			List<String> l = new ArrayList<String>();
			if (allowLocalHost) {
				l.add(this.ecfLocalEndpointListenerScope);
			}
			l.add(this.ecfNonLocalEndpointListenerScope);
			if (nonECFTopologyManager) {
				if (allowLocalHost) {
					l.add(this.nonECFLocalEndpointListenerScope);
				}
				l.add(this.nonECFNonLocalEndpointListenerScope);
			}
			if (this.otherFilters != null) {
				for(String s: this.otherFilters) {
					l.add(s);
				}
			}
			return l.toArray(new String[l.size()]);
		}
	}

	// EventListenerHook impl
	protected void handleEvent(ServiceEvent event, Map listeners) {
		if (!isNonECFTopologyManager()) {
			super.handleEvent(event, listeners);
		}
	}

	// RemoteServiceAdminListener impl
	protected void handleRemoteAdminEvent(RemoteServiceAdminEvent event) {
		if (!(event instanceof RemoteServiceAdmin.RemoteServiceAdminEvent))
			return;
		RemoteServiceAdmin.RemoteServiceAdminEvent rsaEvent = (RemoteServiceAdmin.RemoteServiceAdminEvent) event;

		int eventType = event.getType();
		EndpointDescription endpointDescription = rsaEvent.getEndpointDescription();

		switch (eventType) {
		case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
			if (!nonECFTopologyManager) { 
				advertiseEndpointDescription(endpointDescription);
			}
			break;
		case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
			if (!nonECFTopologyManager) {
				unadvertiseEndpointDescription(endpointDescription);
			}
			break;
		case RemoteServiceAdminEvent.EXPORT_ERROR:
			logError("handleRemoteAdminEvent.EXPORT_ERROR", "Export error with event=" + rsaEvent); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case RemoteServiceAdminEvent.EXPORT_WARNING:
			logWarning("handleRemoteAdminEvent.EXPORT_WARNING", "Export warning with event=" + rsaEvent); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case RemoteServiceAdminEvent.EXPORT_UPDATE:
			if (!nonECFTopologyManager) {
				advertiseModifyEndpointDescription(endpointDescription);
			}
			break;
		case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
			break;
		case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
			break;
		case RemoteServiceAdminEvent.IMPORT_ERROR:
			logError("handleRemoteAdminEvent.IMPORT_ERROR", "Import error with event=" + rsaEvent); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case RemoteServiceAdminEvent.IMPORT_WARNING:
			logWarning("handleRemoteAdminEvent.IMPORT_WARNING", "Import warning with event=" + rsaEvent); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		default:
			logWarning("handleRemoteAdminEvent", //$NON-NLS-1$
					"RemoteServiceAdminEvent=" + rsaEvent + " received with unrecognized type"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void endpointChanged(EndpointEvent event, String matchedFilter) {
		int eventType = event.getType();
		org.osgi.service.remoteserviceadmin.EndpointDescription ed = event.getEndpoint();
		switch (eventType) {
		case EndpointEvent.ADDED:
			handleEndpointAdded(ed, matchedFilter);
			break;
		case EndpointEvent.REMOVED:
			handleEndpointRemoved(ed, matchedFilter);
			break;
		case EndpointEvent.MODIFIED:
			handleEndpointModified(ed, matchedFilter);
			break;
		case EndpointEvent.MODIFIED_ENDMATCH:
			handleEndpointModifiedEndmatch(ed, matchedFilter);
			break;
		}
	}

	protected void handleEndpointAdded(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (matchedFilter.equals(nonECFLocalEndpointListenerScope)) {
			advertiseEndpointDescription(endpoint);
		} else if (matchedFilter.equals(nonECFNonLocalEndpointListenerScope)) {
			EndpointDescription ed = convertEndpointDescriptionFromOSGiToECF(endpoint);
			if (ed != null) {
				handleECFEndpointAdded(ed);
			}
		} else if (matchedFilter.equals(ecfNonLocalEndpointListenerScope) || matchedFilter.equals(ecfLocalEndpointListenerScope)) {
			handleECFEndpointAdded((EndpointDescription) endpoint);
		} else if (this.otherFilters.contains(matchedFilter)) {
			handleOtherFilterEndpointAdded(endpoint, matchedFilter);
		}
	}

	/**
	 * @since 4.9
	 */
	protected void handleOtherFilterEndpointAdded(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
	}
	
	protected void handleEndpointRemoved(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (matchedFilter.equals(nonECFLocalEndpointListenerScope)) {
			unadvertiseEndpointDescription(endpoint);
		} else if (matchedFilter.equals(nonECFNonLocalEndpointListenerScope)) {
			EndpointDescription ed = convertEndpointDescriptionFromOSGiToECF(endpoint);
			if (ed != null)
				handleECFEndpointRemoved(ed);
		} else if (matchedFilter.equals(ecfNonLocalEndpointListenerScope) || matchedFilter.equals(ecfLocalEndpointListenerScope)) {
			handleECFEndpointRemoved((EndpointDescription) endpoint);
		} else if (this.otherFilters.contains(matchedFilter)) {
			handleOtherFilterEndpointRemoved(endpoint,matchedFilter);
		}
	}

	/**
	 * @since 4.9
	 */
	protected void handleOtherFilterEndpointRemoved(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
	}
	
	/**
	 * @since 4.9
	 */
	protected EndpointDescription convertEndpointDescriptionFromOSGiToECF(
			org.osgi.service.remoteserviceadmin.EndpointDescription ed) {
		Map<String, Object> newProps = new HashMap<String, Object>();
		newProps.putAll(ed.getProperties());

		String ecfNS = (String) newProps.remove(RemoteConstants.OSGI_CONTAINER_ID_NS);
		if (ecfNS == null)
			ecfNS = ECF_DEFAULT_NAMESPACE;
		newProps.put(RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE, ecfNS);
		return new EndpointDescription(newProps);
	}


	protected void handleEndpointModifiedEndmatch(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
	}

	/**
	 * @since 4.9
	 */
	protected void handleOtherFilterEndpointModifiedEndmatch(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint, String matchedFilter) {
	}
	
	private static Long getOSGiEndpointModifiedValue(Map<String, Object> properties) {
		Object modifiedValue = properties.get(RemoteConstants.OSGI_ENDPOINT_MODIFIED);
		if (modifiedValue != null && modifiedValue instanceof String)
			return Long.valueOf((String) modifiedValue);
		return null;
	}


	protected void handleEndpointModified(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (matchedFilter.equals(nonECFLocalEndpointListenerScope)) {
			Map<String, Object> edProperties = endpoint.getProperties();
			Long modified = getOSGiEndpointModifiedValue(edProperties);
			Map<String, Object> newEdProperties = new HashMap<String, Object>();
			newEdProperties.putAll(endpoint.getProperties());
			if (modified != null) {
				newEdProperties.remove(RemoteConstants.OSGI_ENDPOINT_MODIFIED);
				handleNonECFEndpointModified(this,
						new org.osgi.service.remoteserviceadmin.EndpointDescription(newEdProperties));
			} else {
				newEdProperties.put(RemoteConstants.OSGI_ENDPOINT_MODIFIED, String.valueOf(System.currentTimeMillis()));
				advertiseModifyEndpointDescription(
						new org.osgi.service.remoteserviceadmin.EndpointDescription(newEdProperties));
			}
		} else if (matchedFilter.equals(nonECFNonLocalEndpointListenerScope)) {
			handleNonECFEndpointModified(this, endpoint);
		} else if (matchedFilter.equals(ecfNonLocalEndpointListenerScope) || matchedFilter.equals(ecfLocalEndpointListenerScope)) {
			handleECFEndpointModified((EndpointDescription) endpoint);
		} else if (this.otherFilters.contains(matchedFilter)) {
			handleOtherFilterEndpointModified(endpoint, matchedFilter);
		}
	}

	/**
	 * @since 4.9
	 */
	protected void handleOtherFilterEndpointModified(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
	}
	
	protected void exportRegisteredServices(final String exportRegisteredSvcsFilter) {
		new Thread(new Runnable() {
			public void run() {
				try {
					final CountDownLatch latch = new CountDownLatch(1);
					BundleTracker bt = new BundleTracker<Bundle>(getContext(), Bundle.INSTALLED | Bundle.RESOLVED
							| Bundle.STARTING | Bundle.START_TRANSIENT | Bundle.ACTIVE, new BundleTrackerCustomizer() {
								public Bundle addingBundle(Bundle bundle, BundleEvent event) {
									String bsn = bundle.getSymbolicName();
									if (bsn != null && bsn.equals(Activator.PLUGIN_ID)) {
										if (bundle.getState() == Bundle.ACTIVE) {
											latch.countDown();
											return null;
										} else
											return bundle;
									}
									return null;
								}

								public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
									if (event != null && event.getType() == BundleEvent.STARTED)
										latch.countDown();
								}

								public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
								}
							});
					bt.open();
					// wait STARTUP_WAIT_TIME for latch going to zero
					if (!latch.await(STARTUP_WAIT_TIME, TimeUnit.MILLISECONDS)) {
						bt.close();
						throw new TimeoutException(
								"RemoteServiceAdmin did not become active in " + STARTUP_WAIT_TIME + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					// Otherwise we close bundle tracker and register remote services
					bt.close();
					final ServiceReference[] existingServiceRefs = getContext().getAllServiceReferences(null,
							exportRegisteredSvcsFilter);
					// Now export as if the service was registering right now...i.e.
					// perform
					// export
					if (existingServiceRefs != null && existingServiceRefs.length > 0) {
						// After having collected all pre-registered services (with
						// marker prop) we are going to asynchronously remote them.
						// Registering potentially is a long-running operation (due to
						// discovery I/O...) and thus should no be carried out in the
						// OSGi FW thread. (https://bugs.eclipse.org/405027)

						for (int i = 0; i < existingServiceRefs.length; i++) {
							// This method will check the service properties for
							// remote service props. If previously registered as
							// a
							// remote service, it will export the remote
							// service if not it will simply return/skip
							handleServiceRegistering(existingServiceRefs[i]);
						}
					}
				} catch (Exception e) {
					logError("exportRegisteredServices", //$NON-NLS-1$
							"Could not retrieve existing service references for exportRegisteredSvcsFilter=" //$NON-NLS-1$
									+ exportRegisteredSvcsFilter,
							e);
				}

			}
		}, "BasicTopologyManagerPreRegSrvExporter").start(); //$NON-NLS-1$
	}

	@Override
	public void endpointAdded(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint, String matchedFilter) {
		handleEndpointAdded(endpoint, matchedFilter);
	}

	@Override
	public void endpointRemoved(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleEndpointRemoved(endpoint, matchedFilter);
	}

}
