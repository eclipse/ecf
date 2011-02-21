package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Properties;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.remoteserviceadmin.EndpointListener;

public class BasicTopologyManager extends AbstractTopologyManager implements
		EventHook, EndpointListener {

	private static final boolean allowLoopbackReference = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.discovery.allowLoopbackReference", //$NON-NLS-1$
					"false")).booleanValue(); //$NON-NLS-1$

	private static final String endpointListenerScope = System
			.getProperty("org.eclipse.ecf.osgi.services.discovery.endpointListenerScope"); //$NON-NLS-1$

	private ServiceRegistration endpointListenerRegistration;

	private ServiceRegistration eventHookRegistration;

	public BasicTopologyManager(BundleContext context) {
		super(context);
	}

	private String getEndpointListenerScope() {
		// If it's set via system property, then simply use it
		if (endpointListenerScope != null)
			return endpointListenerScope;
		// Otherwise create it
		// if allowLoopbackReference is true, then return a filter to match all
		// endpoint description ids
		StringBuffer elScope = new StringBuffer("("); //$NON-NLS-1$
		if (allowLoopbackReference) {
			elScope.append(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID);
			elScope.append("=*"); //$NON-NLS-1$
		} else {
			// filter so that local framework uuid is not the same as local
			// value
			elScope.append("!("); //$NON-NLS-1$
			elScope.append(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID);
			elScope.append("="); //$NON-NLS-1$
			elScope.append(getFrameworkUUID());
			elScope.append(")"); //$NON-NLS-1$
		}
		elScope.append(")"); //$NON-NLS-1$
		String result = elScope.toString();
		trace("getEndpointListenerScope", "endpointListenerScope=" + result); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}

	public void start() throws Exception {
		// Register as EndpointListener, so that it gets notified when Endpoints
		// are discovered
		Properties props = new Properties();
		props.put(
				org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE,
				getEndpointListenerScope());
		endpointListenerRegistration = getContext().registerService(
				EndpointListener.class.getName(), this, (Dictionary) props);

		// Register as EventHook, so that we get notified when remote services
		// are registered
		eventHookRegistration = getContext().registerService(
				EventHook.class.getName(), this, null);
	}

	public void endpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleEndpointAdded(endpoint, matchedFilter);
	}

	public void endpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleEndpointRemoved(endpoint, matchedFilter);
	}

	public void event(ServiceEvent event, Collection contexts) {
		handleEvent(event, contexts);
	}

	public void close() {
		if (eventHookRegistration != null) {
			eventHookRegistration.unregister();
			eventHookRegistration = null;
		}
		if (endpointListenerRegistration != null) {
			endpointListenerRegistration.unregister();
			endpointListenerRegistration = null;
		}
		super.close();
	}

}
