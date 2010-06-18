/*******************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.dnssd;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.provider.dnssd.IDnsSdDiscoveryConstants;
import org.omg.CORBA.SystemException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class Activator implements BundleActivator {
	private static Activator instance;

	private IDiscoveryLocator discoveryLocator;

	private ServiceListener listener;

	private final Object lock = new Object();
	
	public Activator() {
		instance = this;
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		String filter = "";
		
		final ServiceReference configAdminServiceRef = context
				.getServiceReference(ConfigurationAdmin.class.getName());

		if (configAdminServiceRef != null) {
			ConfigurationAdmin configAdmin = (ConfigurationAdmin) context
					.getService(configAdminServiceRef);

			Configuration config = configAdmin.createFactoryConfiguration(
					DnsSdDiscoveryServiceTest.ECF_DISCOVERY_DNSSD, null);
			Dictionary properties = new Hashtable();
			properties.put(IDnsSdDiscoveryConstants.CA_SEARCH_PATH, new String[]{DnsSdDiscoveryServiceTest.DOMAIN});
			properties.put(IDnsSdDiscoveryConstants.CA_RESOLVER, "8.8.8.8");
			config.update(properties);

			filter = "(" + Constants.SERVICE_PID + "=" + config.getPid() + ")";
		} else {
			System.err.println("You don't have config admin deployed. Some tests will fail that require configuration!");
			filter = "(" + Constants.OBJECTCLASS + "=" + IDiscoveryLocator.class.getName() + ")";
		}
		
		// add the service listener
		listener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
				switch (event.getType()) {
				case ServiceEvent.REGISTERED:
					ServiceReference serviceReference = event.getServiceReference();
					discoveryLocator = (IDiscoveryLocator) context.getService(serviceReference);
					synchronized (lock) {
						lock.notifyAll();
					}
				}
			}
		};
		context.addServiceListener(listener, filter);
		
		// try to get the service initially
		ServiceReference[] references = null;
		try {
			references = context.getServiceReferences(IDiscoveryLocator.class.getName(), filter);
		} catch (InvalidSyntaxException e) {
			// may never happen
			e.printStackTrace();
		}
		if(references == null) {
			return;
		}
		for (int i = 0; i < references.length;) {
			ServiceReference serviceReference = references[i];
			discoveryLocator = (IDiscoveryLocator) context.getService(serviceReference);
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if(listener != null) {
			context.removeServiceListener(listener);
			listener = null;
		}
	}

	public static Activator getDefault() {
		return instance;
	}

	public IDiscoveryLocator getDiscoveryLocator() {
		if (discoveryLocator == null) {
			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				// may never happen
				e.printStackTrace();
				return null;
			}
		}
		return discoveryLocator;
	}
}
