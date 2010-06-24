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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

public class Activator implements BundleActivator, ManagedServiceFactory {

	private Map serviceRegistrations = new HashMap();
	private BundleContext context;
	private static final String NAME = "ecf.discovery.dnssd";

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		// register a managed factory for this service
		final Properties cmProps = new Properties();
		cmProps.put(Constants.SERVICE_PID, NAME);
		context.registerService(ManagedServiceFactory.class.getName(), this, cmProps);
		
		// register the service
		final Properties props = new Properties();
		props.put("org.eclipse.ecf.discovery.containerName", NAME);
		props.put(Constants.SERVICE_RANKING, new Integer(750));
		String[] clazzes = new String[]{IDiscoveryLocator.class.getName(), IDiscoveryAdvertiser.class.getName()};
		serviceRegistrations.put(null, context.registerService(clazzes, new ServiceFactory() {
			private volatile DnsSdDiscoveryLocator locator;

			/* (non-Javadoc)
			 * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)
			 */
			public Object getService(final Bundle bundle, final ServiceRegistration registration) {
				if (locator == null) {
					try {
						locator = new DnsSdDiscoveryLocator();
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
		}, props));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (serviceRegistrations != null) {
			for (Iterator itr = serviceRegistrations.values().iterator(); itr.hasNext();) {
				ServiceRegistration serviceRegistration = (ServiceRegistration) itr.next();
				ServiceReference reference = serviceRegistration.getReference();
				IDiscoveryLocator aLocator = (IDiscoveryLocator) context.getService(reference);
				
				serviceRegistration.unregister();
				IContainer container = (IContainer) aLocator.getAdapter(IContainer.class);
				container.dispose();
				container.disconnect();
			}

			serviceRegistrations = null;
		}
		this.context = null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#getName()
	 */
	public String getName() {
		return this.getClass().getName();
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
	 */
	public void updated(String pid, Dictionary properties)
			throws ConfigurationException {
		if(properties != null) {
			Properties props = new Properties();
			props.put(Constants.SERVICE_PID, pid);
			
			DnsSdDiscoveryLocator locator = new DnsSdDiscoveryLocator();
			DnsSdServiceTypeID targetID = new DnsSdServiceTypeID();
			try {
				final String[] searchPaths = (String[]) properties.get(IDnsSdDiscoveryConstants.CA_SEARCH_PATH);
				if(searchPaths != null) {
					targetID.setSearchPath(searchPaths);
				}

				final String resolver = (String) properties.get(IDnsSdDiscoveryConstants.CA_RESOLVER);
				if(resolver != null) {
					locator.setResolver(resolver);
				}
				
				locator.connect(targetID, null);
				serviceRegistrations.put(pid, context.registerService(IDiscoveryLocator.class.getName(), locator, props));
			} catch (ContainerConnectException e) {
				throw new ConfigurationException("", "", e);
			} catch (ClassCastException cce) {
				throw new ConfigurationException("", "", cce);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
	 */
	public void deleted(String pid) {
		ServiceRegistration serviceRegistration = (ServiceRegistration) serviceRegistrations.get(pid);
		ServiceReference reference = serviceRegistration.getReference();
		IDiscoveryLocator aLocator = (IDiscoveryLocator) context.getService(reference);
		
		serviceRegistration.unregister();
		IContainer container = (IContainer) aLocator.getAdapter(IContainer.class);
		container.dispose();
		container.disconnect();
		return;
	}
}
