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

import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;

public class Activator implements BundleActivator, EventHook {
	
	// "_" is a bad tag character for SLP 
	private static final String MARKER = "org.eclipse.ecf.tests.discovery.remote-extender.Activator.class";

	private static final String CONTAINER_TYPE = System.getProperty("org.eclipse.ecf.tests.discovery.remote-extender.containertype", "ecf.r_osgi.peer");
	
	private final String service = System.getProperty("org.eclipse.ecf.tests.discovery.remote-extender.service");
	private Filter filter;
	private Map overwrites;
	private BundleContext context;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext aContext) throws Exception {
		if(service == null) {
			return;
		}
		context = aContext;
		overwrites = new HashMap();
		filter = context.createFilter("(&" +
				"(" + Constants.OBJECTCLASS + "=" + service + ")" +
				"(!(" + IDistributionConstants.SERVICE_EXPORTED_INTERFACES + "=" + new String[]{service} + "))" +
						")");
		
		context.registerService(EventHook.class.getName(), this, null);
		
		ServiceReference[] serviceReferences = context.getAllServiceReferences(service, null);
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
		// also it might be a cyclic event (http://www.eclipse.org/forums/index.php?t=msg&goto=513544&)
		if(context == null || !filter.match(serviceReference) || serviceReference.getProperty(MARKER) != null) {
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
		props.put(MARKER, Boolean.TRUE);
		
		// add OSGi service property indicated export of all interfaces exposed by service (wildcard)
		props.put(IDistributionConstants.SERVICE_EXPORTED_INTERFACES, new String[]{service});
		// add OSGi service property specifying config
		props.put(IDistributionConstants.SERVICE_EXPORTED_CONFIGS, CONTAINER_TYPE);
		// register remote service
		Object remoteService = this.context.getService(aServiceReference);
		// keep in mind that this removes all other interfaces the service was originally registered for
		ServiceRegistration serviceRegistration = context.registerService(service, remoteService, props);
		overwrites.put(aServiceReference, serviceRegistration);
	}
}
