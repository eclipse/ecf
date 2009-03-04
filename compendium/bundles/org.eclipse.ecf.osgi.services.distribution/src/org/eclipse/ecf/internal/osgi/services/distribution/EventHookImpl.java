/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.osgi.services.discovery.ECFServicePublication;
import org.eclipse.ecf.osgi.services.distribution.ECFServiceConstants;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.Constants;
import org.osgi.framework.*;
import org.osgi.service.discovery.ServicePublication;

public class EventHookImpl extends AbstractEventHookImpl {

	/**
	 * 
	 * Inner class for holding onto the triple:
	 * IContainer->IRemoteServiceContainerAdapter->ContainerTypeDescription
	 */
	class RSCAHolder {
		private IContainer container;
		private IRemoteServiceContainerAdapter rsca;
		private ContainerTypeDescription ctd;

		public RSCAHolder(IContainer c, IRemoteServiceContainerAdapter ca,
				ContainerTypeDescription d) {
			Assert.isNotNull(c);
			Assert.isNotNull(ca);
			Assert.isNotNull(d);
			this.container = c;
			this.rsca = ca;
			this.ctd = d;
		}

		public IContainer getContainer() {
			return container;
		}

		public IRemoteServiceContainerAdapter getContainerAdapter() {
			return rsca;
		}

		public ContainerTypeDescription getContainerTypeDescription() {
			return ctd;
		}
	}

	public EventHookImpl(DistributionProviderImpl distributionProvider) {
		super(distributionProvider);
	}

	protected void registerRemoteService(ServiceReference serviceReference,
			String[] remoteInterfaces, String[] remoteConfigurationType) {
		Map ecfConfiguration = parseECFConfigurationType(remoteConfigurationType);
		// First we find ECF distribution providers
		// (IRemoteServiceContainerAdapters)
		RSCAHolder[] rscas = findRSCAHolders(serviceReference, ecfConfiguration);
		// If there are relevant ones then actually register a remote service
		// with them.
		if (rscas == null || rscas.length == 0) {
			trace("registerRemoteService",
					"No remote service container adapters found for serviceReference="
							+ serviceReference + " and configuration="
							+ ecfConfiguration);
			return;
		}
		// Get osgi.remote.requires.intents, and if it is set, verify that the
		// selected container adapters support all required intents (i.e. via
		// findRSCAHoldersSatisfyingRequiredIntents
		String[] remoteRequiresIntents = (String[]) serviceReference
				.getProperty(ECFServiceConstants.OSGI_REMOTE_REQUIRES_INTENTS);
		if (remoteRequiresIntents != null) {
			rscas = findRSCAHoldersSatisfyingRequiredIntents(rscas,
					remoteRequiresIntents);
		}
		// We may not have any remaining, so we need to check again
		if (rscas == null || rscas.length == 0) {
			trace(
					"registerRemoteService",
					"No remote service container adapters found satisfying required intents for serviceReference="
							+ serviceReference
							+ " and configuration="
							+ ecfConfiguration);
			return;
		}
		// Now actually register remote service with remote service container
		// adapters found above. This involves three steps:
		// 1) registering the remote service with each ECF
		// IRemoteServiceContainerAdapter
		// 2) save service reference and remote registration
		// 3) publish remote service (ServicePublication) for discovery
		for (int i = 0; i < rscas.length; i++) {
			// Step 1
			IRemoteServiceRegistration remoteRegistration = rscas[i]
					.getContainerAdapter().registerRemoteService(
							remoteInterfaces, getService(serviceReference),
							getPropertiesForRemoteService(serviceReference));
			trace("registerRemoteService", "containerID="
					+ rscas[i].getContainer().getID() + " serviceReference="
					+ serviceReference + " remoteRegistration="
					+ remoteRegistration);
			// Step 2
			fireRemoteServiceRegistered(serviceReference, remoteRegistration);
			// Step 3
			publishRemoteService(rscas[i], serviceReference, remoteInterfaces,
					remoteRegistration);
		}
	}

	private RSCAHolder[] findRSCAHoldersSatisfyingRequiredIntents(
			RSCAHolder[] rscas, String[] remoteRequiresIntents) {
		List results = new ArrayList();
		for (int i = 0; i < rscas.length; i++) {
			boolean include = true;
			List supportedIntents = Arrays.asList(rscas[i]
					.getContainerTypeDescription().getSupportedIntents());
			for (int j = 0; j < remoteRequiresIntents.length; j++) {
				if (!supportedIntents.contains(remoteRequiresIntents[j])) {
					include = false;
				}
			}
			if (include) {
				trace("findRSCAHoldersSatisfyingRequiredIntents.include",
						"containerID=" + rscas[i].getContainer().getID()
								+ " satisfying intents.  supported intents="
								+ supportedIntents);
				results.add(rscas[i]);
			} else {
				trace("findRSCAHoldersSatisfyingRequiredIntents.exclude",
						"containerID=" + rscas[i].getContainer().getID()
								+ " supported intents=" + supportedIntents);
			}
		}
		return (RSCAHolder[]) results.toArray(new RSCAHolder[] {});
	}

	Dictionary getServicePublicationProperties(RSCAHolder holder,
			ServiceReference ref, String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration) {
		final Dictionary properties = new Properties();
		// Set mandatory ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME
		properties.put(ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
				getAsCollection(remoteInterfaces));

		// Set optional ServicePublication.PROP_KEY_SERVICE_PROPERTIES
		properties.put(ServicePublication.PROP_KEY_SERVICE_PROPERTIES,
				getServicePropertiesForRemotePublication(ref));

		IContainer container = holder.getContainer();
		// Due to slp bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=216944
		// We are not going to use the RFC 119
		// ServicePublication.PROP_KEY_ENDPOINT_ID...since
		// it won't handle some Strings with (e.g. slp) provider
		properties.put(ECFServicePublication.PROP_KEY_ENDPOINT_CONTAINERID,
				container.getID());
		// Set remote service namespace (String)
		Namespace rsnamespace = holder.getContainerAdapter()
				.getRemoteServiceNamespace();
		if (rsnamespace != null)
			properties.put(Constants.SERVICE_NAMESPACE, rsnamespace.getName());
		// Set the actual remote service id (Long)
		properties.put(Constants.SERVICE_ID, ((Long) remoteRegistration
				.getProperty(Constants.SERVICE_ID)));

		return properties;
	}

	private void publishRemoteService(RSCAHolder holder, ServiceReference ref,
			String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration) {
		// First create properties for new ServicePublication
		final Dictionary properties = getServicePublicationProperties(holder,
				ref, remoteInterfaces, remoteRegistration);
		final BundleContext context = Activator.getDefault().getContext();
		// Now, at long last, register the ServicePublication.
		// The RFC 119 discovery should/will pick this up and send it out
		ServiceRegistration reg = context.registerService(
				ServicePublication.class.getName(), new ServicePublication() {
				}, properties);
		fireRemoteServicePublished(ref, reg);
		// And it's done
		trace("publishRemoteService", "containerID="
				+ holder.getContainer().getID() + ",serviceReference=" + ref
				+ " properties=" + properties + ",remoteRegistration="
				+ remoteRegistration);

	}

	private Collection getAsCollection(String[] remoteInterfaces) {
		List result = new ArrayList();
		for (int i = 0; i < remoteInterfaces.length; i++) {
			result.add(remoteInterfaces[i]);
		}
		return result;
	}

	private Map parseECFConfigurationType(String[] remoteConfigurationType) {
		Map results = new HashMap();
		// TODO parse ecf configuration from remoteConfigurationType
		return results;
	}

	protected Dictionary getPropertiesForRemoteService(ServiceReference sr) {
		String[] propKeys = sr.getPropertyKeys();
		Properties newProps = new Properties();
		for (int i = 0; i < propKeys.length; i++) {
			if (!excludeRemoteServiceProperty(propKeys[i]))
				newProps.put(propKeys[i], sr.getProperty(propKeys[i]));
		}
		return newProps;
	}

	protected Map getServicePropertiesForRemotePublication(ServiceReference sr) {
		String[] propKeys = sr.getPropertyKeys();
		Properties newProps = new Properties();
		for (int i = 0; i < propKeys.length; i++) {
			if (!excludeRemoteServiceProperty(propKeys[i]))
				newProps.put(propKeys[i], sr.getProperty(propKeys[i]));
		}
		return newProps;
	}

	private static final List excludedProperties = Arrays.asList(new String[] {
			org.osgi.framework.Constants.SERVICE_ID,
			org.osgi.framework.Constants.OBJECTCLASS,
			ECFServiceConstants.OSGI_REMOTE_INTERFACES,
			ECFServiceConstants.OSGI_REMOTE_REQUIRES_INTENTS,
			ECFServiceConstants.OSGI_REMOTE,
			ECFServiceConstants.OSGI_REMOTE_CONFIGURATION_TYPE,
			// ECF constants
			org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_ID, });

	private boolean excludeRemoteServiceProperty(String string) {
		if (excludedProperties.contains(string))
			return true;
		return false;
	}

	protected RSCAHolder[] findRSCAHolders(ServiceReference serviceReference,
			Map ecfConfiguration) {
		IContainerManager containerManager = Activator.getDefault()
				.getContainerManager();
		return (containerManager != null) ? findRSCAHoldersWithContainerManager(
				containerManager, serviceReference, ecfConfiguration)
				: null;
	}

	private RSCAHolder[] findRSCAHoldersWithContainerManager(
			IContainerManager containerManager,
			ServiceReference serviceReference, Map ecfConfiguration) {
		IContainer[] containers = containerManager.getAllContainers();
		if (containers == null)
			return null;
		List rscas = new ArrayList();
		for (int i = 0; i < containers.length; i++) {
			IRemoteServiceContainerAdapter rsca = (IRemoteServiceContainerAdapter) containers[i]
					.getAdapter(IRemoteServiceContainerAdapter.class);
			if (rsca == null) {
				trace(
						"getRSCAHoldersFromContainers",
						"Container="
								+ containers[i].getID()
								+ " not an IRemoteServiceContainerAdapter. Excluding rsca="
								+ rsca + " from remote registration");
				continue;
			} else {
				ContainerTypeDescription desc = containerManager
						.getContainerTypeDescription(containers[i].getID());
				if (desc == null) {
					trace(
							"getRSCAHoldersFromContainers",
							"Container="
									+ containers[i].getID()
									+ " has null ContainerTypeDescription. Excluding rsca="
									+ rsca + " from remote registration");
				} else if (includeContainer(containers[i], rsca, desc,
						serviceReference, ecfConfiguration))
					rscas.add(new RSCAHolder(containers[i], rsca, desc));
			}
		}
		return (RSCAHolder[]) rscas.toArray(new RSCAHolder[] {});
	}

	protected boolean includeContainer(IContainer container,
			IRemoteServiceContainerAdapter rsca, ContainerTypeDescription desc,
			ServiceReference serviceReference, Map ecfConfiguration) {
		Object cID = serviceReference
				.getProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_ID);
		// If the SERVICE_CONTAINER_ID property is not set, then we'll include
		// it by default
		if (cID == null || !(cID instanceof ID)) {
			trace(
					"includeContainer",
					"serviceReference="
							+ serviceReference
							+ " does not set remote service container id service property.  INCLUDING containerID="
							+ container.getID() + " in remote registration");
			return true;
		}
		// Or if the id is specified and it's the same as the containerID under
		// consideration
		// then it's included
		ID containerID = (ID) cID;
		if (container.getID().equals(containerID)) {
			trace("includeContainer", "serviceReference=" + serviceReference
					+ " has MATCHING container id=" + containerID
					+ ".  INCLUDING rsca=" + container.getID()
					+ " in remote registration");
			return true;
		}
		trace("includeContainer", "serviceReference=" + serviceReference
				+ " has non-matching id=" + containerID + ".  EXCLUDING id="
				+ container.getID() + " in remote registration");
		return false;
	}

}
