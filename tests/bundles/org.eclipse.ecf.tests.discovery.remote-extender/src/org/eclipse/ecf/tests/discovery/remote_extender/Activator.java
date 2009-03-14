/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.discovery.remote_extender;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;

public class Activator implements BundleActivator, EventHook {
	
	private static final String SERVICE = IDiscoveryAdvertiser.class.getName();
	
	private Filter filter;
	private Map overwrites;
	private BundleContext context;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext aContext) throws Exception {
		context = aContext;
		overwrites = new HashMap();
		filter = context.createFilter("(&" +
				"(" + Constants.OBJECTCLASS + "=" + SERVICE + ")" +
				"(!(osgi.remote.interfaces=" + SERVICE + "))" +
						")");
		
		// for the moment we have to manually create a container
		//TODO move into ecf specific configuration
		ContainerFactory.getDefault().createContainer("ecf.r_osgi.peer",
				new Object[] { IDFactory.getDefault().createStringID(
				"r-osgi://localhost:9278") });

		
		context.registerService(EventHook.class.getName(), this, null);
		
		ServiceReference[] serviceReferences = context.getAllServiceReferences(SERVICE, null);
		if(serviceReferences != null) {
			for (int i = 0; i < serviceReferences.length; i++) {
				ServiceReference serviceReference = serviceReferences[i];
				overwriteServiceRegistration(serviceReference);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		this.context = null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.hooks.service.EventHook#event(org.osgi.framework.ServiceEvent, java.util.Collection)
	 */
	public void event(ServiceEvent event, Collection contexts) {
		ServiceReference serviceReference = event.getServiceReference();
		// either this bundle is not active or it is not responsible
		if(context != null || !filter.match(serviceReference)) {
			return;
		}
		
		switch (event.getType()) {
			case ServiceEvent.MODIFIED:
				throw new UnsupportedOperationException("not yet implemented");
			case ServiceEvent.MODIFIED_ENDMATCH:
				throw new UnsupportedOperationException("not yet implemented");
			case ServiceEvent.REGISTERED:
				contexts.clear();
				overwriteServiceRegistration(serviceReference);
				break;
			case ServiceEvent.UNREGISTERING:
				ServiceRegistration serviceRegistration = (ServiceRegistration) overwrites.get(serviceReference);
				serviceRegistration.unregister();
				break;
		}
	}

	private void overwriteServiceRegistration(ServiceReference aServiceReference) {
		Properties props = new Properties();
		String[] keys = aServiceReference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if(!Constants.SERVICE_ID.equals(key) || Constants.SERVICE_RANKING.equals(key)) {
				props.put(key, aServiceReference.getProperty(key));
			}
		}
		props.put("osgi.remote.interfaces", new String[]{SERVICE});
		Object service = this.context.getService(aServiceReference);
		// keep in mind that this removes all other interfaces the service was originally registered for
		ServiceRegistration serviceRegistration = context.registerService(SERVICE, service, props);
		overwrites.put(aServiceReference, serviceRegistration);
	}
}
