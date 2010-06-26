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

public class Activator implements BundleActivator {

	private final Map serviceRegistrations = new HashMap();
	private volatile BundleContext context;
	private static final String NAME = "ecf.discovery.dnssd";

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		// register a managed factory for the locator service
		final Properties locCmProps = new Properties();
		locCmProps.put(Constants.SERVICE_PID, NAME + ".locator");
		context.registerService(ManagedServiceFactory.class.getName(), new DnsSdManagedServiceFactory(DnsSdDiscoveryLocator.class), locCmProps);
		
		// register the locator service
		final Properties locProps = new Properties();
		locProps.put("org.eclipse.ecf.discovery.containerName", NAME + ".locator");
		locProps.put(Constants.SERVICE_RANKING, new Integer(750));
		serviceRegistrations.put(null, context.registerService(IDiscoveryLocator.class.getName(), new DnsSdServiceFactory(DnsSdDiscoveryLocator.class), locProps));

		// register a managed factory for the advertiser service
		final Properties advCmProps = new Properties();
		advCmProps.put(Constants.SERVICE_PID, NAME + ".advertiser");
		context.registerService(ManagedServiceFactory.class.getName(), new DnsSdManagedServiceFactory(DnsSdDiscoveryAdvertiser.class), advCmProps);
		
		// register the advertiser service
		final Properties advProps = new Properties();
		advProps.put("org.eclipse.ecf.discovery.containerName", NAME + ".advertiser");
		advProps.put(Constants.SERVICE_RANKING, new Integer(750));
		serviceRegistrations.put(null, context.registerService(IDiscoveryAdvertiser.class.getName(), new DnsSdServiceFactory(DnsSdDiscoveryAdvertiser.class), advProps));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (serviceRegistrations != null) {
			for (Iterator itr = serviceRegistrations.values().iterator(); itr.hasNext();) {
				ServiceRegistration serviceRegistration = (ServiceRegistration) itr.next();
				disposeServiceRegistration(serviceRegistration);
			}
		}
		this.context = null;
	}
	
	/**
	 * @param serviceRegistration disconnects the underlying IContainer and unregisters the service
	 */
	private void disposeServiceRegistration(ServiceRegistration serviceRegistration) {
		ServiceReference reference = serviceRegistration.getReference();
		IContainer aContainer = (DnsSdDiscoveryContainerAdapter) context.getService(reference);
		
		serviceRegistration.unregister();
		IContainer container = (IContainer) aContainer.getAdapter(IContainer.class);
		container.dispose();
		container.disconnect();
	}
	
	/**
	 * A ManagedServiceFactory capable to handle DnsSdDiscoveryContainerAdapters
	 */
	private class DnsSdManagedServiceFactory implements ManagedServiceFactory {
		private final Class containerClass;

		public DnsSdManagedServiceFactory(Class aContainerClass) {
			containerClass = aContainerClass;
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
				DnsSdDiscoveryContainerAdapter adapter = null;
				DnsSdServiceTypeID targetID = null;
				try {
					// get existing or create new discoverycontainer
					final ServiceRegistration serviceRegistration = (ServiceRegistration) serviceRegistrations.get(pid);
					if(serviceRegistration != null) {
						adapter = (DnsSdDiscoveryContainerAdapter) context.getService(serviceRegistration.getReference());
						targetID = (DnsSdServiceTypeID) adapter.getConnectedID();
					} else {
						adapter = (DnsSdDiscoveryContainerAdapter) containerClass.newInstance();
						targetID = new DnsSdServiceTypeID();
					}

					// apply configuration
					final String[] searchPaths = (String[]) properties.get(IDnsSdDiscoveryConstants.CA_SEARCH_PATH);
					if(searchPaths != null) {
						targetID.setSearchPath(searchPaths);
					}

					final String resolver = (String) properties.get(IDnsSdDiscoveryConstants.CA_RESOLVER);
					if(resolver != null) {
						adapter.setResolver(resolver);
					}
					
					final String tsigKey = (String) properties.get(IDnsSdDiscoveryConstants.CA_TSIG_KEY);
					if(tsigKey != null) {
						final String tsigKeyName = (String) properties.get(IDnsSdDiscoveryConstants.CA_TSIG_KEY_NAME);
						adapter.setTsigKey(tsigKeyName, tsigKey);
					}
					
					// finally connect container and keep ser reg for later updates/deletes
					if(serviceRegistration == null) {
						final Properties props = new Properties();
						props.put(Constants.SERVICE_PID, pid);
						adapter.connect(targetID, null);
						serviceRegistrations.put(pid, context.registerService(IDiscoveryLocator.class.getName(), adapter, props));
					}
				} catch (ContainerConnectException e) {
					throw new ConfigurationException("IDnsSdDiscoveryConstants properties", e.getLocalizedMessage(), e);
				} catch (ClassCastException cce) {
					throw new ConfigurationException("IDnsSdDiscoveryConstants properties", cce.getLocalizedMessage(), cce);
				} catch (InstantiationException e) {
					// may never happen
					throw new ConfigurationException("InstantiationException", e.getLocalizedMessage(), e);
				} catch (IllegalAccessException e) {
					// may never happen
					throw new ConfigurationException("IllegalAccessException", e.getLocalizedMessage(), e);
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
		 */
		public void deleted(String pid) {
			ServiceRegistration serviceRegistration = (ServiceRegistration) serviceRegistrations.get(pid);
			disposeServiceRegistration(serviceRegistration);
		}
	}
	
	/**
	 * A ServiceFactory capable to handle DnsSdDiscoveryContainerAdapters
	 */
	public class DnsSdServiceFactory implements ServiceFactory {
		private volatile DnsSdDiscoveryContainerAdapter container;
		private final Class containerClass;

		public DnsSdServiceFactory(Class aDiscoveryContainerClass) {
			containerClass = aDiscoveryContainerClass;
		}

		/* (non-Javadoc)
		 * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)
		 */
		public Object getService(Bundle bundle, ServiceRegistration registration) {
			if (container == null) {
				try {
					container = (DnsSdDiscoveryContainerAdapter) containerClass.newInstance();
					container.connect(null, null);
				} catch (final ContainerConnectException e) {
					// may never happen
					e.printStackTrace();
					container = null;
				} catch (InstantiationException e) {
					// may never happen
					e.printStackTrace();
					container = null;
				} catch (IllegalAccessException e) {
					// may never happen
					e.printStackTrace();
					container = null;
				}
			}
			return container;
		}

		/* (non-Javadoc)
		 * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration, java.lang.Object)
		 */
		public void ungetService(Bundle bundle,
				ServiceRegistration registration, Object service) {
		}
	}
}
