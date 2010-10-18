/**
 * 
 */
package org.eclipse.ecf.internal.discovery;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.osgi.framework.*;

public class DiscoveryServiceListener implements ServiceListener {

	private AbstractDiscoveryContainerAdapter discoveryContainer;
	private BundleContext context;
	private Class listenerClass;

	public DiscoveryServiceListener(
			AbstractDiscoveryContainerAdapter anAbstractDiscoveryContainerAdapter,
			Class clazz) {
		discoveryContainer = anAbstractDiscoveryContainerAdapter;
		listenerClass = clazz;
		context = DiscoveryPlugin.getDefault().getBundleContext();
		try {
			// get existing listener
			ServiceReference[] references = context.getServiceReferences(
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
		context.removeServiceListener(this);
	}

	private void addServiceListener(ServiceReference[] references) {
		for (int i = 0; i < references.length; i++) {
			ServiceReference serviceReference = references[i];
			if (listenerClass.getName()
					.equals(IServiceListener.class.getName())) {
				IServiceTypeID aType = getIServiceTypeID(serviceReference);
				IServiceListener aListener = (IServiceListener) context
						.getService(serviceReference);
				discoveryContainer.addServiceListener(aType, aListener);
			} else {
				IServiceTypeListener aListener = (IServiceTypeListener) context
						.getService(serviceReference);
				discoveryContainer.addServiceTypeListener(aListener);
			}
		}
	}

	private void addServiceListener(ServiceReference reference) {
		addServiceListener(new ServiceReference[] { reference });
	}

	private void removeServiceListener(ServiceReference[] references) {
		for (int i = 0; i < references.length; i++) {
			ServiceReference serviceReference = references[i];
			if (listenerClass.getName()
					.equals(IServiceListener.class.getName())) {
				IServiceTypeID aType = getIServiceTypeID(serviceReference);
				IServiceListener aListener = (IServiceListener) context
						.getService(serviceReference);
				discoveryContainer.removeServiceListener(aType, aListener);
			} else {
				IServiceTypeListener aListener = (IServiceTypeListener) context
						.getService(serviceReference);
				discoveryContainer.removeServiceTypeListener(aListener);
			}
		}
	}

	private void removeServiceListener(ServiceReference reference) {
		removeServiceListener(new ServiceReference[] { reference });
	}

	private IServiceTypeID getIServiceTypeID(ServiceReference serviceReference) {
		Namespace namespace = discoveryContainer.getServicesNamespace();
		String services = (String) serviceReference
				.getProperty("org.eclipse.ecf.discovery.services");
		String scopes = (String) serviceReference
				.getProperty("org.eclipse.ecf.discovery.scopes");
		String protocols = (String) serviceReference
				.getProperty("org.eclipse.ecf.discovery.protocols");
		String namingAuthority = (String) serviceReference
				.getProperty("org.eclipse.ecf.discovery.namingauthority");
		return (IServiceTypeID) IDFactory.getDefault().createID(
				namespace,
				"_" + services + "" + protocols + "" + scopes + ""
						+ namingAuthority);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
	 * ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
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
		return "(" + Constants.OBJECTCLASS + "=" + listenerClass.getName()
				+ ")";
	}
}
