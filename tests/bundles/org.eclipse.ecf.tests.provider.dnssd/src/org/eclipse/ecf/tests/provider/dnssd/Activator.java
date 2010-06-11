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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class Activator implements BundleActivator {
	private static Activator instance;
	
	private Filter filter;

	private BundleContext context;

	public Activator() {
		instance = this;
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		final ServiceReference configAdminServiceRef = context
				.getServiceReference(ConfigurationAdmin.class.getName());

		if (configAdminServiceRef != null) {
			ConfigurationAdmin configAdmin = (ConfigurationAdmin) context
					.getService(configAdminServiceRef);

			Configuration config = configAdmin.createFactoryConfiguration(
					DnsSdDiscoveryServiceTest.ECF_DISCOVERY_DNSSD, null);
			Dictionary properties = new Hashtable();
			properties.put("searchPath", new String[]{DnsSdDiscoveryServiceTest.DOMAIN});
			properties.put("resolver", "8.8.8.8");
			config.update(properties);

			filter = context.createFilter("(" + Constants.SERVICE_PID + "=" + config.getPid() + ")");
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		context = null;
	}

	public static Activator getDefault() {
		return instance;
	}

	public IDiscoveryLocator getDiscoveryLocator() {
		//TODO need to block until the service comes available
		ServiceReference[] references = null;
		try {
			references = context.getServiceReferences(IDiscoveryLocator.class.getName(), filter.toString());
		} catch (InvalidSyntaxException e) {
			// may never happen
			e.printStackTrace();
			return null;
		}
		for (int i = 0; i < references.length;) {
			ServiceReference serviceReference = references[i];
			return (IDiscoveryLocator) context.getService(serviceReference);
		}
		return null;
	}
}
