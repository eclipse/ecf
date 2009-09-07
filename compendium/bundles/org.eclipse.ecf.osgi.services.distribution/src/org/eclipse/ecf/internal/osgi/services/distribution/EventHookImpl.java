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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.osgi.services.discovery.RemoteServicePublication;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.osgi.services.distribution.IHostContainerFinder;
import org.eclipse.ecf.osgi.services.distribution.IHostDistributionListener;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.discovery.ServicePublication;

public class EventHookImpl implements EventHook {

	private final static String[] EMPTY_STRING_ARRAY = new String[0];

	private final DistributionProviderImpl distributionProvider;
	private final Map srvRefToRemoteSrvRegistration = new HashMap();
	private final Map srvRefToServicePublicationRegistration = new HashMap();

	public EventHookImpl(DistributionProviderImpl distributionProvider) {
		this.distributionProvider = distributionProvider;
	}

	private void findContainersAndRegisterRemoteService(
			ServiceReference serviceReference, String[] remoteInterfaces) {

		// Get optional service property osgi.remote.configuration.type
		String[] remoteConfigurationType = getStringArrayFromPropertyValue(serviceReference
				.getProperty(IDistributionConstants.REMOTE_CONFIGURATION_TYPE));
		// Get optional service property service.intents
		String[] remoteRequiresIntents = getStringArrayFromPropertyValue(serviceReference
				.getProperty(IDistributionConstants.REMOTE_REQUIRES_INTENTS));
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
			// Now notify any listeners
			fireHostRegisteredUnregistered(serviceReference, rsContainers[i],
					remoteRegistration, true);
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
		if (finders == null) {
			logError("findRemoteServiceContainers",
					"No container finders available");
			return null;
		}
		Map rsContainers = new HashMap();
		// For each container finder
		for (int i = 0; i < finders.length; i++) {
			// call out to the container finder to get candidates for that
			// container finder
			IRemoteServiceContainer[] candidates = finders[i]
					.findHostContainers(serviceReference, remoteInterfaces,
							remoteConfigurationType, remoteRequiresIntents);

			if (candidates != null) {
				// Then for all candidates make sure that they are not already
				// present in results. This makes sure that
				for (int j = 0; j < candidates.length; j++) {
					ID containerID = candidates[j].getContainer().getID();
					if (containerID != null)
						rsContainers.put(containerID, candidates[j]);
				}
			}
		}
		// Then move to results list
		List results = new ArrayList();
		for (Iterator i = rsContainers.keySet().iterator(); i.hasNext();)
			results.add(rsContainers.get(i.next()));
		return (IRemoteServiceContainer[]) results
				.toArray(new IRemoteServiceContainer[] {});
	}

	private Dictionary getServicePublicationProperties(
			IRemoteServiceContainer rsContainer, ServiceReference ref,
			String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration) {
		final Dictionary properties = new Properties();
		// Set mandatory ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME
		properties.put(ServicePublication.SERVICE_INTERFACE_NAME,
				getAsCollection(remoteInterfaces));

		// Set optional ServicePublication.PROP_KEY_SERVICE_PROPERTIES
		properties.put(ServicePublication.SERVICE_PROPERTIES,
				getServicePropertiesForRemotePublication(ref));

		IContainer container = rsContainer.getContainer();

		// Due to slp bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=216944
		// We are not going to use the RFC 119
		// ServicePublication.PROP_KEY_ENDPOINT_ID...since
		// it won't handle some Strings with (e.g. slp) provider
		ID endpointID = container.getID();
		properties.put(RemoteServicePublication.ENDPOINT_CONTAINERID,
				endpointID);

		// Also put the target ID in the service properties...*only*
		// if the target ID is non-null and it's *not* the same as the
		// endpointID, then include it in the set of properties delivered
		// for publication
		ID targetID = container.getConnectedID();
		if (targetID != null && !targetID.equals(endpointID)) {
			// put the target ID into the properties
			properties.put(RemoteServicePublication.TARGET_CONTAINERID,
					targetID);
		}

		// Set remote service namespace (String)
		Namespace rsnamespace = rsContainer.getContainerAdapter()
				.getRemoteServiceNamespace();
		if (rsnamespace != null)
			properties.put(Constants.SERVICE_NAMESPACE, rsnamespace.getName());
		// Set the actual remote service id (Long)
		Long serviceId = (Long) remoteRegistration
				.getProperty(Constants.SERVICE_ID);
		byte[] serviceIdAsBytes = null;
		if (serviceId != null) {
			// this should always be true, as every remote service should have a
			// non-null remote serviceID
			serviceIdAsBytes = serviceId.toString().getBytes();
		} else {
			logError(
					"getServicePublicationProperties",
					"RemoteRegistration property remote.service.id is not set in remoteRegistration="
							+ remoteRegistration);
			serviceIdAsBytes = "0".getBytes();
		}

		properties.put(Constants.SERVICE_ID, serviceIdAsBytes);

		return properties;
	}

	private void publishRemoteService(IRemoteServiceContainer rsContainer,
			final ServiceReference ref, String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration) {
		// First create properties for new ServicePublication
		final Dictionary properties = getServicePublicationProperties(
				rsContainer, ref, remoteInterfaces, remoteRegistration);
		// Just prior to registering the ServicePublication, notify
		// the IHostRegistrationListeners
		Activator activator = Activator.getDefault();
		if (activator != null) {
			final BundleContext context = activator.getContext();
			// Now, at long last, register the ServicePublication.
			// The RFC 119 discovery should/will pick this up and send it out
			ServiceRegistration reg = context.registerService(
					ServicePublication.class.getName(),
					new ServicePublication() {
						public ServiceReference getReference() {
							return ref;
						}
					}, properties);
			fireRemoteServicePublished(ref, reg);
			// And it's done
			trace("publishRemoteService", "containerID="
					+ rsContainer.getContainer().getID() + ",serviceReference="
					+ ref + " properties=" + properties
					+ ",remoteRegistration=" + remoteRegistration);
		}
	}

	private Collection getAsCollection(String[] remoteInterfaces) {
		List result = new ArrayList();
		for (int i = 0; i < remoteInterfaces.length; i++) {
			result.add(remoteInterfaces[i]);
		}
		return result;
	}

	private Dictionary getPropertiesForRemoteService(ServiceReference sr) {
		String[] propKeys = sr.getPropertyKeys();
		Properties newProps = new Properties();
		for (int i = 0; i < propKeys.length; i++) {
			if (!excludeRemoteServiceProperty(propKeys[i]))
				newProps.put(propKeys[i], sr.getProperty(propKeys[i]));
		}
		return newProps;
	}

	private Map getServicePropertiesForRemotePublication(ServiceReference sr) {
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
			IDistributionConstants.REMOTE_INTERFACES,
			IDistributionConstants.REMOTE_REQUIRES_INTENTS,
			IDistributionConstants.REMOTE,
			IDistributionConstants.REMOTE_CONFIGURATION_TYPE,
			// ECF constants
			org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_ID, });

	private boolean excludeRemoteServiceProperty(String string) {
		if (excludedProperties.contains(string))
			return true;
		return false;
	}

	public void event(ServiceEvent event, Collection contexts) {
		switch (event.getType()) {
		case ServiceEvent.MODIFIED:
			handleModifiedServiceEvent(event.getServiceReference(), contexts);
			break;
		case ServiceEvent.MODIFIED_ENDMATCH:
			break;
		case ServiceEvent.REGISTERED:
			handleRegisteredServiceEvent(event.getServiceReference(), contexts);
			break;
		case ServiceEvent.UNREGISTERING:
			handleUnregisteringServiceEvent(event.getServiceReference(),
					contexts);
			break;
		default:
			break;
		}
	}

	private String[] getStringArrayFromPropertyValue(Object value) {
		if (value == null)
			return null;
		else if (value instanceof String)
			return new String[] { (String) value };
		else if (value instanceof String[])
			return (String[]) value;
		else if (value instanceof Collection)
			return (String[]) ((Collection) value).toArray(new String[] {});
		else
			return null;
	}

	void handleRegisteredServiceEvent(ServiceReference serviceReference,
			Collection contexts) {
		// This checks to see if the serviceReference has any remote interfaces
		// declared via osgi.remote.interfaces property
		Object osgiRemotes = serviceReference
				.getProperty(IDistributionConstants.REMOTE_INTERFACES);
		// If osgi.remote.interfaces required property is non-null then we
		// handle further, if null then ignore the service registration event
		if (osgiRemotes != null) {
			String[] remoteInterfacesArr = getStringArrayFromPropertyValue(osgiRemotes);
			if (remoteInterfacesArr == null) {
				logError("handleRegisteredServiceEvent",
						"remoteInterfaces not of String [] type as required by RFC 119");
				return;
			}
			trace("handleRegisteredServiceEvent", "serviceReference="
					+ serviceReference + ",remoteInterfaces="
					+ Arrays.asList(remoteInterfacesArr));
			// We compare the osgi.remote.interfaces with those exposed by the
			// service reference and make sure that expose some common
			// interfaces
			String[] remoteInterfaces = (remoteInterfacesArr != null) ? getInterfacesForServiceReference(
					remoteInterfacesArr, serviceReference)
					: null;
			if (remoteInterfaces == null) {
				logError("handleRegisteredServiceEvent",
						"No exposed remoteInterfaces found for serviceReference="
								+ serviceReference);
				return;
			}
			// Now call registerRemoteService
			findContainersAndRegisterRemoteService(serviceReference,
					remoteInterfaces);
		}
	}

	private void fireRemoteServiceRegistered(ServiceReference serviceReference,
			IRemoteServiceRegistration remoteServiceRegistration) {
		synchronized (srvRefToRemoteSrvRegistration) {
			List l = (List) srvRefToRemoteSrvRegistration.get(serviceReference);
			if (l == null) {
				l = new ArrayList();
				srvRefToRemoteSrvRegistration.put(serviceReference, l);
			}
			l.add(remoteServiceRegistration);
			distributionProvider.addExposedService(serviceReference);
		}
	}

	private void fireHostRegisteredUnregistered(
			final ServiceReference reference,
			final IRemoteServiceContainer container,
			final IRemoteServiceRegistration remoteRegistration,
			final boolean registered) {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			IHostDistributionListener[] listeners = activator
					.getHostRegistrationListeners();
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					final IHostDistributionListener l = listeners[i];
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							logError(
									"fireHostRegisteredUnregistered",
									"Exception calling host distribution listener",
									exception);
						}

						public void run() throws Exception {
							if (registered)
								l.registered(reference, container,
										remoteRegistration);
							else
								l.unregistered(reference, remoteRegistration);
						}
					});
				}
			}
		}
	}

	private void fireRemoteServiceUnregistered(ServiceReference reference) {
		IRemoteServiceRegistration[] registrations = null;
		synchronized (srvRefToRemoteSrvRegistration) {
			distributionProvider.removeExposedService(reference);
			List l = (List) srvRefToRemoteSrvRegistration.remove(reference);
			if (l != null) {
				registrations = (IRemoteServiceRegistration[]) l
						.toArray(new IRemoteServiceRegistration[] {});
				l.clear();
			}
		}
		if (registrations != null) {
			for (int i = 0; i < registrations.length; i++) {
				try {
					registrations[i].unregister();
				} catch (IllegalStateException e) {
					// ignore
				} catch (Exception e) {
					logError("fireRemoteServiceUnregistered",
							"Exception unregistering remote registration="
									+ registrations[i], e);
				}
				// Now notify any listeners that this servicereference has been
				// unregistered
				fireHostRegisteredUnregistered(reference, null,
						registrations[i], false);
			}
		}
	}

	private void fireRemoteServicePublished(ServiceReference serviceReference,
			ServiceRegistration servicePublicationRegistration) {
		synchronized (srvRefToServicePublicationRegistration) {
			List l = (List) srvRefToServicePublicationRegistration
					.get(serviceReference);
			if (l == null) {
				l = new ArrayList();
				srvRefToServicePublicationRegistration.put(serviceReference, l);
			}
			l.add(servicePublicationRegistration);
		}
	}

	private void fireRemoteServiceUnpublished(ServiceReference reference) {
		ServiceRegistration[] registrations = null;
		synchronized (srvRefToServicePublicationRegistration) {
			List l = (List) srvRefToServicePublicationRegistration
					.remove(reference);
			if (l != null) {
				registrations = (ServiceRegistration[]) l
						.toArray(new ServiceRegistration[] {});
				l.clear();
			}
		}
		if (registrations != null) {
			for (int i = 0; i < registrations.length; i++) {
				try {
					registrations[i].unregister();
				} catch (Exception e) {
					logError("fireRemoteServiceUnpublished",
							"Exception unregistering service publication registrations="
									+ registrations[i], e);
				}
			}
		}
	}

	private String[] getInterfacesForServiceReference(
			String[] remoteInterfaces, ServiceReference serviceReference) {
		if (remoteInterfaces == null || remoteInterfaces.length == 0)
			return EMPTY_STRING_ARRAY;
		List results = new ArrayList();
		List interfaces = Arrays.asList((String[]) serviceReference
				.getProperty(org.osgi.framework.Constants.OBJECTCLASS));
		for (int i = 0; i < remoteInterfaces.length; i++) {
			String intf = remoteInterfaces[i];
			if (IDistributionConstants.REMOTE_INTERFACES_WILDCARD.equals(intf))
				return (String[]) interfaces.toArray(new String[] {});
			if (intf != null && interfaces.contains(intf))
				results.add(intf);
		}
		return (String[]) results.toArray(new String[] {});
	}

	private void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.EVENTHOOKDEBUG, this
				.getClass(), methodName, message);
	}

	private void traceException(String methodName, String message, Throwable t) {
		Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), ((methodName == null) ? "<unknown>"
						: methodName)
						+ ":" + ((message == null) ? "<empty>" : message), t);
	}

	private void logError(String methodName, String message, Throwable t) {
		traceException(methodName, message, t);
		Activator.getDefault()
				.log(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								IStatus.ERROR, this.getClass().getName()
										+ ":"
										+ ((methodName == null) ? "<unknown>"
												: methodName)
										+ ":"
										+ ((message == null) ? "<empty>"
												: message), t));
	}

	private void logError(String methodName, String message) {
		logError(methodName, message, null);
		traceException(methodName, message, null);
	}

	private void handleUnregisteringServiceEvent(
			ServiceReference serviceReference, Collection contexts) {
		fireRemoteServiceUnregistered(serviceReference);
		fireRemoteServiceUnpublished(serviceReference);
	}

	private void handleModifiedServiceEvent(ServiceReference serviceReference,
			Collection contexts) {
		// This checks to see if the serviceReference has any remote interfaces
		// declared via osgi.remote.interfaces property
		Object osgiRemotes = serviceReference
				.getProperty(IDistributionConstants.REMOTE_INTERFACES);
		if (osgiRemotes != null) {
			// XXX we currently don't handle the modified service event
			trace(
					"org.eclipse.ecf.internal.osgi.services.distribution.EventHookImpl.handleModifiedServiceEvent(ServiceReference, Collection)",
					"implement!");
		}
	}

	private Object getService(ServiceReference sr) {
		return Activator.getDefault().getContext().getService(sr);
	}

}
