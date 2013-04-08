package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Collection;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.remoteserviceadmin.EndpointListener;

public class BasicTopologyManagerComponent implements EventHook {

	private boolean exportRegisteredSvcs = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.basictopologymanager.exportRegisteredSvcs", "true")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$

	private String exportRegisteredSvcsFilter = System
			.getProperty(
					"org.eclipse.ecf.osgi.services.basictopologymanager.exportRegisteredSvcsFilter", "(service.exported.interfaces=*)"); //$NON-NLS-1$ //$NON-NLS-2$

	private String exportRegisteredSvcsClassname = System
			.getProperty("org.eclipse.ecf.osgi.services.basictopologymanager.exportRegisteredSvcsClassname"); //$NON-NLS-1$

	private BasicTopologyManagerImpl basicTopologyManagerImpl;

	void bindEndpointListener(EndpointListener el) {
		if (el instanceof BasicTopologyManagerImpl)
			basicTopologyManagerImpl = (BasicTopologyManagerImpl) el;
	}

	void unbindEndpointListener(EndpointListener el) {
		if (el instanceof BasicTopologyManagerImpl)
			basicTopologyManagerImpl = null;
	}

	void activate() {
		if (exportRegisteredSvcs)
			basicTopologyManagerImpl.exportRegisteredServices(
					exportRegisteredSvcsClassname, exportRegisteredSvcsFilter);
	}

	public void event(ServiceEvent event, Collection contexts) {
		basicTopologyManagerImpl.event(event, contexts);
	}

}
