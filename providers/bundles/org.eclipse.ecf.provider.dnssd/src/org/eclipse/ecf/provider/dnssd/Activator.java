/****************************************************************************
- * Copyright (c) 2009 Markus Alexander Kuppe.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.dnssd;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ExtensionRegistryRunnable;
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

	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.dnssd"; //$NON-NLS-1$
	public static final String DISCOVERY_CONTAINER_NAME_VALUE = "ecf.discovery.dnssd"; //$NON-NLS-1$
	public static final String LOCATOR = ".locator"; //$NON-NLS-1$
	public static final String ADVERTISER = ".advertiser"; //$NON-NLS-1$

	private static final String DISCOVERY_CONTAINER_NAME_KEY = "org.eclipse.ecf.discovery.containerName"; //$NON-NLS-1$
	
	@SuppressWarnings("rawtypes")
	private final Map serviceRegistrations = new HashMap();
	private volatile BundleContext context;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void start(final BundleContext context) throws Exception {
		this.context = context;
		
		SafeRunner.run(new ExtensionRegistryRunnable(context) {
			protected void runWithoutRegistry() throws Exception {
				context.registerService(Namespace.class, new DnsSdNamespace(), null);
				context.registerService(ContainerTypeDescription.class, new ContainerTypeDescription(DISCOVERY_CONTAINER_NAME_VALUE + LOCATOR,new ContainerInstantiator(),"Discovery Locator Container"), null);
				context.registerService(ContainerTypeDescription.class, new ContainerTypeDescription(DISCOVERY_CONTAINER_NAME_VALUE + ADVERTISER,new ContainerInstantiator(),"Discovery Advertiser Container"), null);
			}
		});
		
		// register a managed factory for the locator service
		final Hashtable locCmProps = new Hashtable();
		locCmProps.put(Constants.SERVICE_PID, DISCOVERY_CONTAINER_NAME_VALUE + LOCATOR);
		context.registerService(ManagedServiceFactory.class.getName(), new DnsSdManagedServiceFactory(DnsSdDiscoveryLocator.class), locCmProps);
		
		// register the locator service
		final Hashtable locProps = new Hashtable();
		locProps.put(DISCOVERY_CONTAINER_NAME_KEY, DISCOVERY_CONTAINER_NAME_VALUE + LOCATOR);
		locProps.put(Constants.SERVICE_RANKING, Integer.valueOf(750));
		serviceRegistrations.put(null, context.registerService(IDiscoveryLocator.class.getName(), new DnsSdServiceFactory(DnsSdDiscoveryLocator.class), locProps));

		// register a managed factory for the advertiser service
		final Hashtable advCmProps = new Hashtable();
		advCmProps.put(Constants.SERVICE_PID, DISCOVERY_CONTAINER_NAME_VALUE + ADVERTISER);
		context.registerService(ManagedServiceFactory.class.getName(), new DnsSdManagedServiceFactory(DnsSdDiscoveryAdvertiser.class), advCmProps);
		
		// register the advertiser service
		final Hashtable advProps = new Hashtable();
		advProps.put(DISCOVERY_CONTAINER_NAME_KEY, DISCOVERY_CONTAINER_NAME_VALUE + ADVERTISER);
		advProps.put(Constants.SERVICE_RANKING, Integer.valueOf(750));
		serviceRegistrations.put(null, context.registerService(IDiscoveryAdvertiser.class.getName(), new DnsSdServiceFactory(DnsSdDiscoveryAdvertiser.class), advProps));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@SuppressWarnings("rawtypes")
	public void stop(BundleContext context) throws Exception {
		if (serviceRegistrations != null) {
			for (final Iterator itr = serviceRegistrations.values().iterator(); itr.hasNext();) {
				final ServiceRegistration serviceRegistration = (ServiceRegistration) itr.next();
				disposeServiceRegistration(serviceRegistration);
			}
		}
		this.context = null;
	}
	
	/**
	 * @param serviceRegistration disconnects the underlying IContainer and unregisters the service
	 */
	@SuppressWarnings("unchecked")
	private void disposeServiceRegistration(@SuppressWarnings("rawtypes") ServiceRegistration serviceRegistration) {
		@SuppressWarnings("rawtypes")
		final ServiceReference reference = serviceRegistration.getReference();
		final IContainer aContainer = (DnsSdDiscoveryContainerAdapter) context.getService(reference);
		
		serviceRegistration.unregister();
		final IContainer container = (IContainer) aContainer.getAdapter(IContainer.class);
		container.dispose();
		container.disconnect();
	}
	
	/**
	 * A ManagedServiceFactory capable to handle DnsSdDiscoveryContainerAdapters
	 */
	private class DnsSdManagedServiceFactory implements ManagedServiceFactory {
		@SuppressWarnings("rawtypes")
		private final Class containerClass;

		public DnsSdManagedServiceFactory(@SuppressWarnings("rawtypes") Class aContainerClass) {
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
		@SuppressWarnings({ "unchecked", "rawtypes" })
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
						adapter = (DnsSdDiscoveryContainerAdapter) containerClass.getDeclaredConstructor().newInstance();
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
						final Hashtable props = new Hashtable();
						props.put(Constants.SERVICE_PID, pid);
						adapter.connect(targetID, null);
						serviceRegistrations.put(pid, context.registerService(IDiscoveryLocator.class.getName(), adapter, props));
					}
				} catch (Exception e) {
					throw new ConfigurationException("IDnsSdDiscoveryConstants properties", e.getLocalizedMessage(), e); //$NON-NLS-1$
				}
				
			}
		}

		/* (non-Javadoc)
		 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
		 */
		@SuppressWarnings("rawtypes")
		public void deleted(String pid) {
			final ServiceRegistration serviceRegistration = (ServiceRegistration) serviceRegistrations.get(pid);
			disposeServiceRegistration(serviceRegistration);
		}
	}
	
	/**
	 * A ServiceFactory capable to handle DnsSdDiscoveryContainerAdapters
	 */
	@SuppressWarnings("rawtypes")
	public class DnsSdServiceFactory implements ServiceFactory {
		private volatile DnsSdDiscoveryContainerAdapter container;
		private final Class containerClass;

		public DnsSdServiceFactory(Class aDiscoveryContainerClass) {
			containerClass = aDiscoveryContainerClass;
		}

		/* (non-Javadoc)
		 * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)
		 */
		@SuppressWarnings("unchecked")
		public Object getService(Bundle bundle, ServiceRegistration registration) {
			if (container == null) {
				try {
					container = (DnsSdDiscoveryContainerAdapter) containerClass.getDeclaredConstructor().newInstance();
					container.connect(null, null);
				} catch (Exception e) {
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
			// nop
		}
	}
}
