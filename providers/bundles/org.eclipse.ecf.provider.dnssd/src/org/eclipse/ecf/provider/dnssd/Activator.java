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
package org.eclipse.ecf.provider.dnssd;

import java.util.Properties;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private ServiceRegistration serviceRegistration;
	private static final String NAME = "ecf.discovery.dnssd";

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		final Properties props = new Properties();
		props.put("org.eclipse.ecf.discovery.containerName", NAME);
		props.put(Constants.SERVICE_RANKING, new Integer(750));
		serviceRegistration = context.registerService(IDiscoveryLocator.class.getName(), new ServiceFactory() {
			private volatile DnsSdDisocoveryLocator locator;

			/* (non-Javadoc)
			 * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)
			 */
			public Object getService(final Bundle bundle, final ServiceRegistration registration) {
				if (locator == null) {
					try {
						locator = new DnsSdDisocoveryLocator();
						locator.connect(null, null);
					} catch (final ContainerConnectException e) {
						locator = null;
					}
				}
				return locator;
			}

			/* (non-Javadoc)
			 * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration, java.lang.Object)
			 */
			public void ungetService(final Bundle bundle, final ServiceRegistration registration, final Object service) {
			}
		}, props);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (serviceRegistration != null) {
			ServiceReference reference = serviceRegistration.getReference();
			IDiscoveryLocator aLocator = (IDiscoveryLocator) context.getService(reference);

			serviceRegistration.unregister();

			IContainer container = (IContainer) aLocator.getAdapter(IContainer.class);
			container.disconnect();
			container.dispose();

			serviceRegistration = null;
		}
	}
}
