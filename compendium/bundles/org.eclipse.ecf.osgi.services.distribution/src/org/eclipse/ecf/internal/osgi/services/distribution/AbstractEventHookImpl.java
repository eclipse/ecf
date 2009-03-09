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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.osgi.services.distribution.ECFServiceConstants;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.*;
import org.osgi.framework.hooks.service.EventHook;

public abstract class AbstractEventHookImpl implements EventHook {

	private final static String[] EMPTY_STRING_ARRAY = new String[0];

	private final DistributionProviderImpl distributionProvider;

	private final Map srvRefToRemoteSrvRegistration = new HashMap(); /*
																	 * ServiceReference
																	 * -> Vector
																	 * of
																	 * IRemoteServiceRegistration
																	 */

	private final Map srvRefToServicePublicationRegistration = new HashMap(); /*
																			 * ServiceReference
																			 * -
																			 * >
																			 * Vector
																			 * of
																			 * ServiceRegistration
																			 */

	public AbstractEventHookImpl(DistributionProviderImpl distributionProvider) {
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

	void handleRegisteredServiceEvent(ServiceReference serviceReference,
			Collection contexts) {
		// This checks to see if the serviceReference has any remote interfaces
		// declared via osgi.remote.interfaces property
		Object osgiRemotes = serviceReference
				.getProperty(ECFServiceConstants.OSGI_REMOTE_INTERFACES);
		// If osgi.remote.interfaces required property is non-null then we
		// handle further, if null then ignore the service registration event
		if (osgiRemotes != null) {
			// The osgiRemotes should be of type String [] according to
			// RFC119...if it's not String [] we ignore
			String[] remoteInterfacesArr = (String[]) ((osgiRemotes instanceof String[]) ? osgiRemotes
					: null);
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
			// Now get optional service property osgi.remote.configuration.type
			Object osgiRemoteConfigurationType = serviceReference
					.getProperty(ECFServiceConstants.OSGI_REMOTE_CONFIGURATION_TYPE);
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
			// Now call registerRemoteService
			registerRemoteService(serviceReference, remoteInterfaces,
					remoteConfigurationType);
		}
	}

	protected abstract void registerRemoteService(
			ServiceReference serviceReference, String[] remoteInterfaces,
			String[] remoteConfigurationType);

	protected void fireRemoteServiceRegistered(
			ServiceReference serviceReference,
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

	protected void fireRemoteServiceUnregistered(ServiceReference reference) {
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
			}
		}
	}

	protected void fireRemoteServicePublished(
			ServiceReference serviceReference,
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

	protected void fireRemoteServiceUnpublished(ServiceReference reference) {
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
				.getProperty(Constants.OBJECTCLASS));
		for (int i = 0; i < remoteInterfaces.length; i++) {
			String intf = remoteInterfaces[i];
			if (ECFServiceConstants.OSGI_REMOTE_INTERFACES_WILDCARD
					.equals(intf))
				return (String[]) interfaces.toArray(new String[] {});
			if (intf != null && interfaces.contains(intf))
				results.add(intf);
		}
		return (String[]) results.toArray(new String[] {});
	}

	protected void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.EVENTHOOKDEBUG, this
				.getClass(), methodName, message);
	}

	protected void traceException(String methodName, String message, Throwable t) {
		Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), ((methodName == null) ? "<unknown>"
						: methodName)
						+ ":" + ((message == null) ? "<empty>" : message), t);
	}

	protected void logError(String methodName, String message, Throwable t) {
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

	protected void logError(String methodName, String message) {
		logError(methodName, message, null);
		traceException(methodName, message, null);
	}

	protected void handleUnregisteringServiceEvent(
			ServiceReference serviceReference, Collection contexts) {
		fireRemoteServiceUnregistered(serviceReference);
		fireRemoteServiceUnpublished(serviceReference);
	}

	protected void handleModifiedServiceEvent(
			ServiceReference serviceReference, Collection contexts) {
		// TODO
	}

	protected Object getService(ServiceReference sr) {
		return Activator.getDefault().getContext().getService(sr);
	}

}
