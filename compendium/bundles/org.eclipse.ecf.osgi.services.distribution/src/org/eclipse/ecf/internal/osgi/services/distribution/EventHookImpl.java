/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.osgi.services.discovery.RemoteServicePublication;
import org.eclipse.ecf.osgi.services.discovery.ServicePublication;
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

public class EventHookImpl implements EventHook {

	private final DistributionProviderImpl distributionProvider;
	private final Map srvRefToRemoteSrvRegistration = new HashMap();
	private final Map srvRefToServicePublicationRegistration = new HashMap();

	public EventHookImpl(DistributionProviderImpl distributionProvider) {
		this.distributionProvider = distributionProvider;
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

	private String[] getExportedConfigs(ServiceReference serviceReference) {
		return getStringArrayFromPropertyValue(serviceReference
				.getProperty(IDistributionConstants.SERVICE_EXPORTED_CONFIGS));
	}

	void handleRegisteredServiceEvent(ServiceReference serviceReference,
			Collection contexts) {

		// Using OSGI 4.2 Chap 13 Remote Services spec, get the specified remote
		// interfaces for the given service reference
		String[] exportedInterfaces = getExportedInterfaces(serviceReference);
		// If no remote interfaces set, then we don't do anything with it
		if (exportedInterfaces == null)
			return;

		// Get optional service property for exported configs
		String[] exportedConfigs = getExportedConfigs(serviceReference);

		// Get all intents (service.intents, service.exported.intents,
		// service.exported.intents.extra)
		String[] serviceIntents = getServiceIntents(serviceReference);

		// Now call out to find host remote service containers via
		// IHostContainerFinder service (service to allow extensibility
		// in matching to available ECF containers/providers
		IRemoteServiceContainer[] rsContainers = findHostContainers(
				serviceReference, exportedInterfaces, exportedConfigs,
				serviceIntents);

		if (rsContainers == null || rsContainers.length == 0) {
			LogUtility.logWarning(
					"handleRegisteredServiceEvent", //$NON-NLS-1$
					DebugOptions.EVENTHOOKDEBUG, this.getClass(),
					"No remote service containers found for serviceReference=" //$NON-NLS-1$
							+ serviceReference + ". Service NOT EXPORTED"); //$NON-NLS-1$
			return;
		}
		Dictionary remoteServiceProperties = getPropertiesForRemoteService(serviceReference);
		Object remoteService = getService(serviceReference);
		// Now actually register remote service with remote service container
		// adapters found above.
		for (int i = 0; i < rsContainers.length; i++) {
			// Step 1 - Register with remote service container adapter for given
			// all found containers/providers
			IRemoteServiceRegistration remoteRegistration = rsContainers[i]
					.getContainerAdapter().registerRemoteService(
							exportedInterfaces, remoteService,
							remoteServiceProperties);
			trace("registerRemoteService", "containerID=" //$NON-NLS-1$ //$NON-NLS-2$
					+ rsContainers[i].getContainer().getID()
					+ " serviceReference=" + serviceReference //$NON-NLS-1$
					+ " remoteRegistration=" + remoteRegistration); //$NON-NLS-1$
			// Step 2 - Save registration
			fireRemoteServiceRegistered(serviceReference, remoteRegistration);
			// Step 3 - Publish via discovery API
			publishRemoteService(rsContainers[i], serviceReference,
					exportedInterfaces, remoteRegistration, serviceIntents,
					exportedConfigs, remoteServiceProperties);
			// Step 4 - Fire registered event to listeners
			fireHostRegisteredUnregistered(serviceReference, rsContainers[i],
					remoteRegistration, true);
		}

	}

	private String[] getServiceIntents(ServiceReference serviceReference) {
		List results = new ArrayList();
		String[] intents = getStringArrayFromPropertyValue(serviceReference
				.getProperty(IDistributionConstants.SERVICE_INTENTS));
		if (intents != null)
			results.addAll(Arrays.asList(intents));
		String[] exportedIntents = getStringArrayFromPropertyValue(serviceReference
				.getProperty(IDistributionConstants.SERVICE_EXPORTED_INTENTS));
		if (exportedIntents != null)
			results.addAll(Arrays.asList(exportedIntents));
		String[] extraIntents = getStringArrayFromPropertyValue(serviceReference
				.getProperty(IDistributionConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
		if (extraIntents != null)
			results.addAll(Arrays.asList(extraIntents));
		if (results.size() == 0)
			return null;
		return (String[]) results.toArray(new String[] {});
	}

	private IRemoteServiceContainer[] findHostContainers(
			ServiceReference serviceReference, String[] exportedInterfaces,
			String[] exportedConfigs, String[] serviceIntents) {
		// Get activator
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		// Get finder (as service)
		IHostContainerFinder finder = activator
				.getHostRemoteServiceContainerFinder();
		// If none found, then we have nothing that we can do except log the
		// error and return
		if (finder == null) {
			logError("findRemoteServiceContainers", //$NON-NLS-1$
					"No container finders available"); //$NON-NLS-1$
			return null;
		}
		// Call out to find host containers as candidates
		return finder.findHostContainers(serviceReference, exportedInterfaces,
				exportedConfigs, serviceIntents);
	}

	private Dictionary getServicePublicationProperties(
			IRemoteServiceContainer rsContainer, ServiceReference ref,
			String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration,
			String[] serviceIntents, String[] supportedConfigs,
			Dictionary remoteProperties) {

		final Dictionary result = new Properties();
		IContainer container = rsContainer.getContainer();

		// Set mandatory ServicePublication.SERVICE_INTERFACE_NAME
		result.put(RemoteServicePublication.SERVICE_INTERFACE_NAME,
				getAsCollection(remoteInterfaces));

		// If supportedConfigs is null, then get supported configs from
		// description and it must also not be null
		if (supportedConfigs == null)
			supportedConfigs = getSupportedConfigs(rsContainer);
		result.put(RemoteServicePublication.ENDPOINT_SUPPORTED_CONFIGS,
				getAsCollection(supportedConfigs));

		if (serviceIntents != null)
			result.put(RemoteServicePublication.ENDPOINT_SERVICE_INTENTS,
					getAsCollection(serviceIntents));

		// Set optional ServicePublication.PROP_KEY_SERVICE_PROPERTIES
		result.put(RemoteServicePublication.SERVICE_PROPERTIES,
				remoteProperties);

		// Due to slp bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=216944
		// We are not going to use the RFC 119
		// ServicePublication.PROP_KEY_ENDPOINT_ID...since
		// it won't handle some Strings with (e.g. slp) provider
		ID endpointID = container.getID();
		result.put(RemoteServicePublication.ENDPOINT_CONTAINERID, endpointID);

		// Also put the target ID in the service properties...*only*
		// if the target ID is non-null and it's *not* the same as the
		// endpointID, then include it in the set of properties delivered
		// for publication
		ID targetID = container.getConnectedID();
		if (targetID != null && !targetID.equals(endpointID)) {
			// put the target ID into the properties
			result.put(RemoteServicePublication.TARGET_CONTAINERID, targetID);
		}

		// Set remote service namespace (String)
		Namespace rsnamespace = rsContainer.getContainerAdapter()
				.getRemoteServiceNamespace();
		if (rsnamespace != null)
			result.put(Constants.SERVICE_NAMESPACE, rsnamespace.getName());

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
					"getServicePublicationProperties", //$NON-NLS-1$
					"RemoteRegistration property remote.service.id is not set in remoteRegistration=" //$NON-NLS-1$
							+ remoteRegistration);
			serviceIdAsBytes = "0".getBytes(); //$NON-NLS-1$
		}

		result.put(Constants.SERVICE_ID, serviceIdAsBytes);

		return result;
	}

	private void publishRemoteService(IRemoteServiceContainer rsContainer,
			final ServiceReference ref, String[] remoteInterfaces,
			IRemoteServiceRegistration remoteRegistration,
			String[] serviceIntents, String[] supportedConfigs,
			Dictionary remoteServiceProperties) {
		// First create properties for new ServicePublication
		final Dictionary properties = getServicePublicationProperties(
				rsContainer, ref, remoteInterfaces, remoteRegistration,
				serviceIntents, supportedConfigs, remoteServiceProperties);
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
			trace("publishRemoteService", "containerID=" //$NON-NLS-1$ //$NON-NLS-2$
					+ rsContainer.getContainer().getID() + ",serviceReference=" //$NON-NLS-1$
					+ ref + " properties=" + properties //$NON-NLS-1$
					+ ",remoteRegistration=" + remoteRegistration); //$NON-NLS-1$
		}
	}

	private String[] getSupportedConfigs(IRemoteServiceContainer rsContainer) {
		Activator a = Activator.getDefault();
		if (a == null)
			return null;
		IContainerManager containerManager = a.getContainerManager();
		if (containerManager == null)
			return null;
		ContainerTypeDescription ctd = containerManager
				.getContainerTypeDescription(rsContainer.getContainer().getID());
		if (ctd == null)
			return null;
		return ctd.getSupportedConfigs();
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

	private static final List excludedProperties = Arrays
			.asList(new String[] {
					org.osgi.framework.Constants.SERVICE_ID,
					org.osgi.framework.Constants.OBJECTCLASS,
					IDistributionConstants.SERVICE_EXPORTED_INTERFACES,
					IDistributionConstants.SERVICE_INTENTS,
					IDistributionConstants.SERVICE_EXPORTED_INTENTS,
					IDistributionConstants.SERVICE_EXPORTED_INTENTS_EXTRA,
					IDistributionConstants.SERVICE_IMPORTED,
					IDistributionConstants.SERVICE_EXPORTED_CONFIGS,
					IDistributionConstants.SERVICE_EXPORTED_CONTAINER_ID,
					IDistributionConstants.SERVICE_EXPORTED_CONTAINER_CONNECT_CONTEXT,
					IDistributionConstants.SERVICE_EXPORTED_CONTAINER_CONNECT_TARGET,
					IDistributionConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGUMENTS,
					// ECF constants
					org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_ID, });

	private boolean excludeRemoteServiceProperty(String string) {
		if (excludedProperties.contains(string))
			return true;
		return false;
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
									"fireHostRegisteredUnregistered", //$NON-NLS-1$
									"Exception calling host distribution listener", //$NON-NLS-1$
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
					logError("fireRemoteServiceUnregistered", //$NON-NLS-1$
							"Exception unregistering remote registration=" //$NON-NLS-1$
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
					logError("fireRemoteServiceUnpublished", //$NON-NLS-1$
							"Exception unregistering service publication registrations=" //$NON-NLS-1$
									+ registrations[i], e);
				}
			}
		}
	}

	private String[] getExportedInterfaces(ServiceReference serviceReference) {
		// Get the OSGi 4.2 specified required service property value
		Object propValue = serviceReference
				.getProperty(IDistributionConstants.SERVICE_EXPORTED_INTERFACES);
		// If the required property is not set then it's not being registered
		// as a remote service so we return null
		if (propValue == null)
			return null;
		boolean wildcard = propValue
				.equals(IDistributionConstants.SERVICE_EXPORTED_INTERFACES_WILDCARD);
		if (wildcard)
			return (String[]) serviceReference
					.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
		else {
			final String[] stringValue = getStringArrayFromPropertyValue(propValue);
			if (stringValue != null
					&& stringValue.length == 1
					&& stringValue[0]
							.equals(IDistributionConstants.SERVICE_EXPORTED_INTERFACES_WILDCARD)) {
				LogUtility
						.logWarning(
								"getExportedInterfaces", //$NON-NLS-1$
								DebugOptions.EVENTHOOKDEBUG, this.getClass(),
								"Service Exported Interfaces Wildcard does not accept String[\"*\"]"); //$NON-NLS-1$
			}
			return stringValue;
		}
	}

	private void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.EVENTHOOKDEBUG,
				this.getClass(), methodName, message);
	}

	private void traceException(String methodName, String message, Throwable t) {
		Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), ((methodName == null) ? "<unknown>" //$NON-NLS-1$
						: methodName)
						+ ":" + ((message == null) ? "<empty>" : message), t); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void logError(String methodName, String message, Throwable t) {
		traceException(methodName, message, t);
		Activator.getDefault().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
						this.getClass().getName() + ":" //$NON-NLS-1$
								+ ((methodName == null) ? "<unknown>" //$NON-NLS-1$
										: methodName) + ":" //$NON-NLS-1$
								+ ((message == null) ? "<empty>" //$NON-NLS-1$
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
				.getProperty(IDistributionConstants.SERVICE_EXPORTED_INTERFACES);
		if (osgiRemotes != null) {
			// XXX we currently don't handle the modified service event
			trace("org.eclipse.ecf.internal.osgi.services.distribution.EventHookImpl.handleModifiedServiceEvent(ServiceReference, Collection)", //$NON-NLS-1$
			"implement!"); //$NON-NLS-1$
		}
	}

	private Object getService(ServiceReference sr) {
		return Activator.getDefault().getContext().getService(sr);
	}

}
