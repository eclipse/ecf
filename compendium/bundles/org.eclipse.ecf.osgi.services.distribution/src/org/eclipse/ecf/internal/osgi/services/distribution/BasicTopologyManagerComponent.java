package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Map;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.service.remoteserviceadmin.EndpointEventListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

public class BasicTopologyManagerComponent implements EventListenerHook,
		RemoteServiceAdminListener {

	private boolean exportRegisteredSvcs = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.basictopologymanager.exportRegisteredSvcs", "true")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$

	private String exportRegisteredSvcsFilter = System
			.getProperty(
					"org.eclipse.ecf.osgi.services.basictopologymanager.exportRegisteredSvcsFilter", "(service.exported.interfaces=*)"); //$NON-NLS-1$ //$NON-NLS-2$

	private String exportRegisteredSvcsClassname = System
			.getProperty("org.eclipse.ecf.osgi.services.basictopologymanager.exportRegisteredSvcsClassname"); //$NON-NLS-1$

	private BasicTopologyManagerImpl basicTopologyManagerImpl;

	void bindEndpointEventListener(EndpointEventListener el) {
		if (el instanceof BasicTopologyManagerImpl)
			basicTopologyManagerImpl = (BasicTopologyManagerImpl) el;
	}

	void unbindEndpointEventListener(EndpointEventListener el) {
		if (el instanceof BasicTopologyManagerImpl)
			basicTopologyManagerImpl = null;
	}

	void activate() {
		if (exportRegisteredSvcs)
			basicTopologyManagerImpl.exportRegisteredServices(
					exportRegisteredSvcsClassname, exportRegisteredSvcsFilter);
	}

	// RemoteServiceAdminListener impl
	public void remoteAdminEvent(RemoteServiceAdminEvent event) {
		basicTopologyManagerImpl.handleRemoteAdminEvent(event);
	}

	// EventListenerHook impl
	public void event(ServiceEvent event, Map listeners) {
		basicTopologyManagerImpl.event(event, listeners);
	}

}
