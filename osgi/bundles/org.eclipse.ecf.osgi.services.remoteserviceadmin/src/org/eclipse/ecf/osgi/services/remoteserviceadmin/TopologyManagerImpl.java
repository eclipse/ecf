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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointEvent;
import org.osgi.service.remoteserviceadmin.EndpointEventListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * @since 4.6
 */
public class TopologyManagerImpl extends AbstractTopologyManager implements EndpointEventListener {

	public static final int STARTUP_WAIT_TIME = Integer.getInteger("org.eclipse.ecf.osgi.services.remoteserviceadmin.startupWaitTime", 20000); //$NON-NLS-1$

	public TopologyManagerImpl(BundleContext context) {
		super(context);
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

	protected void handleEndpointAdded(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleECFEndpointAdded((EndpointDescription) endpoint);
	}

	protected void handleEndpointRemoved(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleECFEndpointRemoved((EndpointDescription) endpoint);
	}

	// EventListenerHook impl
	protected void handleEvent(ServiceEvent event, Map listeners) {
		super.handleEvent(event, listeners);
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
			advertiseEndpointDescription(endpointDescription);
			break;
		case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
			unadvertiseEndpointDescription(endpointDescription);
			break;
		case RemoteServiceAdminEvent.EXPORT_ERROR:
			logError("handleRemoteAdminEvent.EXPORT_ERROR", "Export error with event=" + rsaEvent); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case RemoteServiceAdminEvent.EXPORT_WARNING:
			logWarning("handleRemoteAdminEvent.EXPORT_WARNING", "Export warning with event=" + rsaEvent); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case RemoteServiceAdminEvent.EXPORT_UPDATE:
			advertiseModifyEndpointDescription(endpointDescription);
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

	protected void handleEndpointModifiedEndmatch(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		// By default do nothing for end match. subclasses may decide
		// to change this behavior
	}

	protected void handleEndpointModified(org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleECFEndpointModified((EndpointDescription) endpoint);
	}
	
	protected void exportRegisteredServices(final String exportRegisteredSvcsFilter) {
		new Thread(new Runnable() {
			public void run() {
				try {
					final CountDownLatch latch = new CountDownLatch(1);
					BundleTracker bt = new BundleTracker<Bundle>(getContext(),Bundle.INSTALLED | Bundle.RESOLVED |
				            Bundle.STARTING | Bundle.START_TRANSIENT | Bundle.ACTIVE , new BundleTrackerCustomizer() {
						public Bundle addingBundle(Bundle bundle, BundleEvent event) {
							String bsn = bundle.getSymbolicName();
							if (bsn != null && bsn.equals(Activator.PLUGIN_ID)) {
								if (bundle.getState() == Bundle.ACTIVE) {
									latch.countDown();
									return null;
								}
								else return bundle;
							}
							return null;
						}
						public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
							if (event != null && event.getType() == BundleEvent.STARTED) 
								latch.countDown();
						}
						public void removedBundle(Bundle bundle, BundleEvent event, Object object) {}
					});
					bt.open();
					// wait STARTUP_WAIT_TIME for latch going to zero
					if (!latch.await(STARTUP_WAIT_TIME, TimeUnit.MILLISECONDS)) {
						bt.close();
						throw new TimeoutException("RemoteServiceAdmin did not become active in "+STARTUP_WAIT_TIME+"ms"); //$NON-NLS-1$ //$NON-NLS-2$
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

}
