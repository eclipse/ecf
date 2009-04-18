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
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.osgi.services.discovery.IServicePublication;
import org.eclipse.ecf.osgi.services.distribution.IHostContainerFinder;
import org.eclipse.ecf.osgi.services.distribution.IServiceConstants;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.Constants;
import org.osgi.framework.*;
import org.osgi.service.discovery.ServicePublication;

public class EventHookImpl extends AbstractEventHookImpl implements
		IHostContainerFinder {

	public EventHookImpl(DistributionProviderImpl distributionProvider) {
		super(distributionProvider);
	}

	protected void findContainersAndRegisterRemoteService(
			ServiceReference serviceReference, String[] remoteInterfaces) {

		// Get optional service property osgi.remote.configuration.type
		Object osgiRemoteConfigurationType = serviceReference
				.getProperty(IServiceConstants.OSGI_REMOTE_CONFIGURATION_TYPE);
		// The osgiRemoteConfigurationType is optional and can be null. If
		// non-null, it should be of type String [] according to RFC119...if
		// it's non-null and not String [] we ignore
		String[] remoteConfigurationType = null;
		if (osgiRemoteConfigurationType != null) {
			if (!(osgiRemoteConfigurationType instanceof String[])) {
				logError("handleRegisteredServiceEvent",
						"osgi.remote.configuration.type is not String[] as required by RFC 119");
				return;
			}
			remoteConfigurationType = (String[]) osgiRemoteConfigurationType;
		}
		// Get optional service property service.intents
		Object osgiRemoteRequiresIntents = serviceReference
				.getProperty(IServiceConstants.OSGI_REMOTE_REQUIRES_INTENTS);
		String[] remoteRequiresIntents = null;
		if (osgiRemoteRequiresIntents != null) {
			if (!(osgiRemoteRequiresIntents instanceof String[])) {
				logError("handleRegisteredServiceEvent",
						"service.intents is not String[] as required by RFC 119");
				return;
			}
			osgiRemoteRequiresIntents = (String[]) osgiRemoteRequiresIntents;
		}
		// Now call out to find host remote service containers
		IRemoteServiceContainer[] rsContainers = findRemoteServiceContainers(
				serviceReference, remoteInterfaces, remoteConfigurationType,
				remoteRequiresIntents);

		if (rsContainers == null || rsContainers.length == 0) {
			trace("registerRemoteService",
					"No remote service container adapters found for serviceReference="
							+ serviceReference);
			return;
		}
		// Now actually register remote service with remote service container
		// adapters found above. This involves three steps:
		// 1) registering the remote service with each ECF
		// IRemoteServiceContainerAdapter
		// 2) save service reference and remote registration
		// 3) publish remote service (ServicePublication) for discovery
		for (int i = 0; i < rsContainers.length; i++) {
			// Step 1
			IRemoteServiceRegistration remoteRegistration = rsContainers[i]
					.getContainerAdapter().registerRemoteService(
							remoteInterfaces, getService(serviceReference),
							getPropertiesForRemoteService(serviceReference));
			trace("registerRemoteService", "containerID="
					+ rsContainers[i].getContainer().getID()
					+ " serviceReference=" + serviceReference
					+ " remoteRegistration=" + remoteRegistration);
			// Step 2
			fireRemoteServiceRegistered(serviceReference, remoteRegistration);
			// Step 3
			publishRemoteService(rsContainers[i], serviceReference,
					remoteInterfaces, remoteRegistration);
		}
	}

	private IRemoteServiceContainer[] findRemoteServiceContainers(
			ServiceReference serviceReference, String[] remoteInterfaces,
			String[] remoteConfigurationType, String[] remoteRequiresIntents) {
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		IHostContainerFinder[] finders = activator
				.getHostRemoteServiceContainerFinders();
		if (finders == null || finders.length == 0) {
			logError("findRemoteServiceContainers",
					"No container finders available");
			return null;
		}
		List result = new ArrayList();
		for (int i = 0; i < finders.length; i++) {
			IRemoteServiceContainer[] foundRSContainers = finders[i]
					.findHostContainers(serviceReference,
							remoteInterfaces, remoteConfigurationType,
							remoteRequiresIntents);
			if (foundRSContainers != null && foundRSContainers.length > 0) {
				trace("findRemoteServiceContainersViaService",
						"findRemoteServiceContainers finder=" + finders[i]
								+ " foundRSContainers="
								+ Arrays.asList(foundRSContainers));
				for (int j = 0; j < foundRSContainers.length; j++)
					result.add(foundRSContainers[j]);
			}
		}
		return (IRemoteServiceContainer[]) result
				.toArray(new IRemoteServiceContainer[] {});
	}

	public IRemoteServiceContainer[] findHostContainers(
			ServiceReference serviceReference, String[] remoteInterfaces,
			String[] remoteConfigurationType, String[] remoteRequiresIntents) {
		Collection rsContainers = findRemoteContainersSatisfyingRequiredIntents(remoteRequiresIntents);
		List results = new ArrayList();
		for (Iterator i = rsContainers.iterator(); i.hasNext();) {
			IRemoteServiceContainer rsContainer = (IRemoteServiceContainer) i
					.next();
			if (includeContainer(serviceReference, rsContainer))
				results.add(rsContainer);
		}
		return (IRemoteServiceContainer[]) results
				.toArray(new IRemoteServiceContainer[] {});
	}

	protected boolean includeContainer(ServiceReference serviceReference,
			IRemoteServiceContainer rsContainer) {
		IContainer container = rsContainer.getContainer();
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

	private Collection findRemoteContainersSatisfyingRequiredIntents(
			String[] remoteRequiresIntents) {
		List results = new ArrayList();
		IContainer[] containers = Activator.getDefault().getContainerManager()
				.getAllContainers();
		if (containers == null || containers.length == 0)
			return null;
		for (int i = 0; i < containers.length; i++) {
			// Check to make sure it's a rs container adapter. If it's not go
			// onto next one
			IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) containers[i]
					.getAdapter(IRemoteServiceContainerAdapter.class);
			if (adapter == null)
				continue;
			// Get container type description and intents
			ContainerTypeDescription description = Activator.getDefault()
					.getContainerManager().getContainerTypeDescription(
							containers[i].getID());
			// If it has no description continue
			if (description == null)
				continue;
			List supportedIntents = Arrays.asList(description
					.getSupportedIntents());
			boolean hasIntents = true;
			if (remoteRequiresIntents != null) {
				for (int j = 0; j < remoteRequiresIntents.length; j++) {
					if (!supportedIntents.contains(remoteRequiresIntents[j]))
						hasIntents = false;
				}
			}
			if (hasIntents) {
				trace("findHostRemoteServiceContainers.include", "containerID="
						+ containers[i].getID());
				results.add(new RemoteServiceContainer(containers[i], adapter));
			} else {
				trace("findHostRemoteServiceContainers.exclude", "containerID="
						+ containers[i].getID() + " supported intents="
						+ supportedIntents);
			}
		}
		return results;
	}

	Dictionary getServicePublicationProperties(
			IRemoteServiceContainer rsContainer, ServiceReference ref,
			String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration) {
		final Dictionary properties = new Properties();
		// Set mandatory ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME
		properties.put(ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
				getAsCollection(remoteInterfaces));

		// Set optional ServicePublication.PROP_KEY_SERVICE_PROPERTIES
		properties.put(ServicePublication.PROP_KEY_SERVICE_PROPERTIES,
				getServicePropertiesForRemotePublication(ref));

		IContainer container = rsContainer.getContainer();

		// Due to slp bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=216944
		// We are not going to use the RFC 119
		// ServicePublication.PROP_KEY_ENDPOINT_ID...since
		// it won't handle some Strings with (e.g. slp) provider
		ID endpointID = container.getID();
		properties.put(IServicePublication.PROP_KEY_ENDPOINT_CONTAINERID,
				container.getID());

		// Also put the target ID in the service properties...*only*
		// if the target ID is non-null and it's *not* the same as the
		// endpointID, then include it in the set of properties delivered
		// for publication
		ID targetID = container.getConnectedID();
		if (targetID != null && !targetID.equals(endpointID)) {
			// put the target ID into the properties
			properties.put(IServicePublication.PROP_KEY_TARGET_CONTAINERID,
					targetID);
		}

		// Set remote service namespace (String)
		Namespace rsnamespace = rsContainer.getContainerAdapter()
				.getRemoteServiceNamespace();
		if (rsnamespace != null)
			properties.put(Constants.SERVICE_NAMESPACE, rsnamespace.getName());
		// Set the actual remote service id (Long)
		properties.put(Constants.SERVICE_ID, ((Long) remoteRegistration
				.getProperty(Constants.SERVICE_ID)));

		return properties;
	}

	private void publishRemoteService(IRemoteServiceContainer rsContainer,
			final ServiceReference ref, String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration) {
		// First create properties for new ServicePublication
		final Dictionary properties = getServicePublicationProperties(
				rsContainer, ref, remoteInterfaces, remoteRegistration);
		final BundleContext context = Activator.getDefault().getContext();
		// Now, at long last, register the ServicePublication.
		// The RFC 119 discovery should/will pick this up and send it out
		ServiceRegistration reg = context.registerService(
				ServicePublication.class.getName(), new ServicePublication() {
					public ServiceReference getReference() {
						return ref;
					}
				}, properties);
		fireRemoteServicePublished(ref, reg);
		// And it's done
		trace("publishRemoteService", "containerID="
				+ rsContainer.getContainer().getID() + ",serviceReference="
				+ ref + " properties=" + properties + ",remoteRegistration="
				+ remoteRegistration);

	}

	private Collection getAsCollection(String[] remoteInterfaces) {
		List result = new ArrayList();
		for (int i = 0; i < remoteInterfaces.length; i++) {
			result.add(remoteInterfaces[i]);
		}
		return result;
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
			IServiceConstants.OSGI_REMOTE_INTERFACES,
			IServiceConstants.OSGI_REMOTE_REQUIRES_INTENTS,
			IServiceConstants.OSGI_REMOTE,
			IServiceConstants.OSGI_REMOTE_CONFIGURATION_TYPE,
			// ECF constants
			org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_ID, });

	private boolean excludeRemoteServiceProperty(String string) {
		if (excludedProperties.contains(string))
			return true;
		return false;
	}

}
