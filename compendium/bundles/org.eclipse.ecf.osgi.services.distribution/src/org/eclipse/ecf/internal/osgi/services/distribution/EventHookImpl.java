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
import org.eclipse.ecf.core.util.Trace;
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
		if (rscas == null) {
			trace("registerRemoteService",
					"No remote service container adapters found for serviceReference="
							+ serviceReference + " and configuration="
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
							remoteInterfaces,
							getService(serviceReference),
							createPropertiesForRemoteService(rscas[i],
									remoteInterfaces, serviceReference));
			trace("registerRemoteService",
					"REGISTERED REMOTE SERVICE serviceReference="
							+ serviceReference + " remoteRegistration="
							+ remoteRegistration);
			// Step 2
			fireRemoteServiceRegistered(serviceReference, remoteRegistration);
			// Step 3
			publishRemoteService(rscas[i], serviceReference, remoteInterfaces,
					remoteRegistration);
		}
	}

	private void publishRemoteService(RSCAHolder holder, ServiceReference ref,
			String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration) {
		// First create properties for new ServicePublication
		final Dictionary properties = new Hashtable();
		final BundleContext context = Activator.getDefault().getContext();
		// Set mandatory ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME
		properties.put(ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
				getAsCollection(remoteInterfaces));

		// XXX TODO set optional
		// ServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION
		// XXX TODO set optional
		// ServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME

		// Set optional ServicePublication.PROP_KEY_SERVICE_PROPERTIES
		properties.put(ServicePublication.PROP_KEY_SERVICE_PROPERTIES,
				getServiceProperties(ref));
		// Set optional ServicePublication.PROP_KEY_ENDPOINT_ID to
		// container.getID().toExternalForm()
		IContainer container = holder.getContainer();
		properties.put(ServicePublication.PROP_KEY_ENDPOINT_ID, container
				.getID().toExternalForm());

		// ECF remote service property
		// Specify container factory name
		properties.put(Constants.SERVICE_CONTAINER_FACTORY_NAME, holder
				.getContainerTypeDescription().getName());
		// Specify remote service id AS STRING
		properties.put(Constants.SERVICE_ID, ((Long) remoteRegistration
				.getProperty(Constants.SERVICE_ID)).toString());

		// Now, at long last, register the ServicePublication.
		// The RFC 119 discovery should/will pick this up and send it out
		ServiceRegistration reg = context.registerService(
				ServicePublication.class.getName(), new ServicePublication() {
				}, properties);
		fireRemoteServicePublished(ref, reg);
		// And it's done
		trace("publishRemoteService",
				"PUBLISH REMOTE SERVICE serviceReference=" + ref
						+ " properties=" + properties);

	}

	private Collection getAsCollection(String[] remoteInterfaces) {
		List result = new ArrayList();
		for (int i = 0; i < remoteInterfaces.length; i++) {
			result.add(remoteInterfaces[i]);
		}
		return result;
	}

	private Map getServiceProperties(final ServiceReference ref) {
		Map map = (Map) ref
				.getProperty(ServicePublication.PROP_KEY_SERVICE_PROPERTIES);
		if (map == null)
			map = new HashMap();
		return map;
	}

	private Map parseECFConfigurationType(String[] remoteConfigurationType) {
		Map results = new HashMap();
		// TODO parse ecf configuration from remoteConfigurationType
		return results;
	}

	protected Collection /* <? extends String> */registerRemoteService(
			RSCAHolder[] rscas, String[] remoteInterfaces, ServiceReference sr) {
		final ArrayList result = new ArrayList();
		for (int i = 0; i < rscas.length; i++) {
			IRemoteServiceRegistration remoteRegistration = rscas[i]
					.getContainerAdapter().registerRemoteService(
							remoteInterfaces,
							getService(sr),
							createPropertiesForRemoteService(rscas[i],
									remoteInterfaces, sr));
			trace("registerRemoteService",
					"REGISTERED REMOTE SERVICE serviceReference=" + sr
							+ " remoteRegistration=" + remoteRegistration);
			result.add(remoteRegistration.getContainerID().toString());
			fireRemoteServiceRegistered(sr, remoteRegistration);
		}
		return result;
	}

	protected Dictionary createPropertiesForRemoteService(RSCAHolder holder,
			String[] remotes, ServiceReference sr) {
		String[] propKeys = sr.getPropertyKeys();
		Properties newProps = new Properties();
		for (int i = 0; i < propKeys.length; i++) {
			newProps.put(propKeys[i], sr.getProperty(propKeys[i]));
		}
		return newProps;
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
				Trace
						.trace(
								Activator.PLUGIN_ID,
								DebugOptions.DEBUG,
								this.getClass(),
								"getRSCAHoldersFromContainers",
								"Container="
										+ containers[i]
										+ " not an IRemoteServiceContainerAdapter. Excluding rsca="
										+ rsca + " from remote registration");
				continue;
			} else {
				ContainerTypeDescription desc = containerManager
						.getContainerTypeDescription(containers[i].getID());
				if (desc == null) {
					Trace
							.trace(
									Activator.PLUGIN_ID,
									DebugOptions.DEBUG,
									this.getClass(),
									"getRSCAHoldersFromContainers",
									"Container="
											+ containers[i]
											+ " has null container type description. Excluding rsca="
											+ rsca
											+ " from remote registration");
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
			Trace
					.trace(
							Activator.PLUGIN_ID,
							DebugOptions.DEBUG,
							this.getClass(),
							"includeContainer",
							"serviceReference="
									+ serviceReference
									+ " does not set remote service container id service property.  INCLUDING containerID="
									+ container.getID()
									+ " in remote registration");
			return true;
		}
		// Or if the id is specified and it's the same as the containerID under
		// consideration
		// then it's included
		ID containerID = (ID) cID;
		if (container.getID().equals(containerID)) {
			Trace.trace(Activator.PLUGIN_ID, DebugOptions.DEBUG, this
					.getClass(), "includeContainer", "serviceReference="
					+ serviceReference + " has MATCHING container id="
					+ containerID + ".  INCLUDING rsca=" + container.getID()
					+ " in remote registration");
			return true;
		}
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.DEBUG, this.getClass(),
				"includeContainer", "serviceReference=" + serviceReference
						+ " has non-matching id=" + containerID
						+ ".  EXCLUDING id=" + container.getID()
						+ " in remote registration");
		return false;
	}

}
