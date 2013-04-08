package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Collection;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointListener;

public class BasicTopologyManagerImpl extends AbstractTopologyManager implements
		EndpointListener {

	BasicTopologyManagerImpl(BundleContext context) {
		super(context);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.remoteserviceadmin.EndpointListener#endpointAdded(org
	 * .osgi.service.remoteserviceadmin.EndpointDescription, java.lang.String)
	 */
	public void endpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleEndpointAdded(endpoint, matchedFilter);
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

	void event(ServiceEvent event, Collection contexts) {
		handleEvent(event, contexts);
	}

}
