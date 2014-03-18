package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Map;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointEvent;
import org.osgi.service.remoteserviceadmin.EndpointEventListener;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;

public class BasicTopologyManagerImpl extends AbstractTopologyManager implements
		EndpointListener, EndpointEventListener {

	private static final boolean allowLoopbackReference = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.discovery.allowLoopbackReference", //$NON-NLS-1$
					"false")).booleanValue(); //$NON-NLS-1$

	private static final String defaultScope = System
			.getProperty("org.eclipse.ecf.osgi.services.discovery.endpointListenerScope"); //$NON-NLS-1$

	private static final boolean disableDiscovery = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.discovery.disableDiscovery", "false")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$

	private String endpointListenerScope;
	private static final String ONLY_ECF_SCOPE = "(" + RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE + "=*)"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String NO_ECF_SCOPE = "(!(" //$NON-NLS-1$
			+ RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE + "=*))"; //$NON-NLS-1$

	BasicTopologyManagerImpl(BundleContext context) {
		super(context);
		if (defaultScope != null)
			this.endpointListenerScope = defaultScope;
		// If loopback is allowed, then for this endpoint listener we only
		// consider those that have a namespace (only ECF endpoint descriptions)
		if (allowLoopbackReference)
			endpointListenerScope = ONLY_ECF_SCOPE;
		else {
			// If loopback not allowed, then we have our scope include
			// both !frameworkUUID same, and ONLY_ECF_SCOPE
			StringBuffer elScope = new StringBuffer(""); //$NON-NLS-1$
			// filter so that local framework uuid is not the same as local
			// value
			elScope.append("(&(!(").append(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID).append("=").append(getFrameworkUUID()).append("))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			elScope.append(ONLY_ECF_SCOPE);
			elScope.append(")"); //$NON-NLS-1$
			endpointListenerScope = elScope.toString();
		}
	}

	String[] getScope() {
		return new String[] { endpointListenerScope, NO_ECF_SCOPE };
	}

	protected String getFrameworkUUID() {
		return super.getFrameworkUUID();
	}

	void exportRegisteredServices(String exportRegisteredSvcsClassname,
			String exportRegisteredSvcsFilter) {
		try {
			final ServiceReference[] existingServiceRefs = getContext()
					.getAllServiceReferences(exportRegisteredSvcsClassname,
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
				new Thread(new Runnable() {
					public void run() {
						for (int i = 0; i < existingServiceRefs.length; i++) {
							// This method will check the service properties for
							// remote service props. If previously registered as
							// a
							// remote service, it will export the remote
							// service if not it will simply return/skip
							handleServiceRegistering(existingServiceRefs[i]);
						}
					}
				}, "BasicTopologyManagerPreRegSrvExporter").start(); //$NON-NLS-1$
			}
		} catch (InvalidSyntaxException e) {
			logError(
					"exportRegisteredServices", //$NON-NLS-1$
					"Could not retrieve existing service references for exportRegisteredSvcsClassname=" //$NON-NLS-1$
							+ exportRegisteredSvcsClassname
							+ " and exportRegisteredSvcsFilter=" //$NON-NLS-1$
							+ exportRegisteredSvcsFilter, e);
		}
	}

	// EndpointListener impl
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.remoteserviceadmin.EndpointListener#endpointAdded(org
	 * .osgi.service.remoteserviceadmin.EndpointDescription, java.lang.String)
	 * 
	 * 
	 * From the R5 spec page 329 section 122.6.2:
	 * 
	 * Notify the Endpoint Listener of a new Endpoint Description. The second
	 * parameter is the filter that matched the Endpoint Description.
	 * Registering the same Endpoint multiple times counts as a single
	 * registration.
	 */
	public void endpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleEndpointAdded(endpoint, matchedFilter);
	}

	protected void handleEndpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (matchedFilter.equals(endpointListenerScope))
			if (endpoint instanceof EndpointDescription)
				handleECFEndpointAdded((EndpointDescription) endpoint);
			else
				handleNonECFEndpointAdded(this, endpoint);
		else if (matchedFilter.equals(NO_ECF_SCOPE))
			if (endpoint instanceof EndpointDescription)
				handleECFEndpointAdded((EndpointDescription) endpoint);
			else
				advertiseEndpointDescription(endpoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.remoteserviceadmin.EndpointListener#endpointRemoved(
	 * org.osgi.service.remoteserviceadmin.EndpointDescription,
	 * java.lang.String)
	 */
	public void endpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleEndpointRemoved(endpoint, matchedFilter);
	}

	protected void handleEndpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (matchedFilter.equals(endpointListenerScope))
			if (endpoint instanceof EndpointDescription)
				handleECFEndpointRemoved((EndpointDescription) endpoint);
			else
				handleNonECFEndpointRemoved(this, endpoint);
		else if (matchedFilter.equals(NO_ECF_SCOPE))
			if (endpoint instanceof EndpointDescription)
				handleECFEndpointRemoved((EndpointDescription) endpoint);
			else
				unadvertiseEndpointDescription(endpoint);
	}

	// EventListenerHook impl
	void event(ServiceEvent event, Map listeners) {
		handleEvent(event, listeners);
	}

	// RemoteServiceAdminListener impl
	void handleRemoteAdminEvent(RemoteServiceAdminEvent event) {
		if (!(event instanceof RemoteServiceAdmin.RemoteServiceAdminEvent))
			return;
		RemoteServiceAdmin.RemoteServiceAdminEvent rsaEvent = (RemoteServiceAdmin.RemoteServiceAdminEvent) event;

		int eventType = event.getType();
		EndpointDescription endpointDescription = rsaEvent
				.getEndpointDescription();

		if (disableDiscovery) {
			logWarning(
					"handleRemoteAdminEvent", "discovery disabled.  RemoteServiceAdminEvent type=" + eventType + " description=" + endpointDescription); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}

		switch (eventType) {
		case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
			advertiseEndpointDescription(endpointDescription);
			break;
		case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
			unadvertiseEndpointDescription(endpointDescription);
			break;
		case RemoteServiceAdminEvent.EXPORT_ERROR:
			logError("handleExportError", "Export error with event=" + rsaEvent); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
			break;
		case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
			break;
		case RemoteServiceAdminEvent.IMPORT_ERROR:
			break;
		default:
			logWarning(
					"handleRemoteAdminEvent", "RemoteServiceAdminEvent=" + rsaEvent + " received with unrecognized type"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * Implementation of
	 * org.osgi.service.remoteserviceadmin.EndpointEventListener for rfc 203/RSA
	 * 1.1
	 * 
	 * @see EndpointEventListener#endpointChanged(EndpointEvent, String)
	 */
	public void endpointChanged(EndpointEvent event, String matchedFilter) {
		int eventType = event.getType();
		org.osgi.service.remoteserviceadmin.EndpointDescription ed = event
				.getEndpoint();
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
			endpointModifiedEndMatch(ed, matchedFilter);
			break;
		}
	}

	protected void endpointModifiedEndMatch(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		// By default do nothing for end match. subclasses may decide
		// to change this behavior
	}

	protected void handleEndpointModified(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (matchedFilter.equals(endpointListenerScope))
			if (endpoint instanceof EndpointDescription)
				handleECFEndpointModified((EndpointDescription) endpoint);
			else
				handleNonECFEndpointModified(this, endpoint);
		else if (matchedFilter.equals(NO_ECF_SCOPE))
			if (endpoint instanceof EndpointDescription)
				handleECFEndpointModified((EndpointDescription) endpoint);
			else
				advertiseEndpointDescription(endpoint);
	}

}
