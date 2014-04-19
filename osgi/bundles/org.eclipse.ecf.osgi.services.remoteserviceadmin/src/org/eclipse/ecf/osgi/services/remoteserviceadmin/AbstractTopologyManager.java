/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Abstract superclass for topology managers. This abstract superclass provides
 * basic functionality for topology managers to reuse. New topology managers can
 * extend this class to get or customize desired functionality. Alternatively,
 * they can use this class as a guide to implementing desired topology manager
 * behavior. For description of the role of topology managers see the <a
 * href="http://www.osgi.org/download/r4v42/r4.enterprise.pdf">OSGI 4.2 Remote
 * Service Admin specification (chap 122)</a>.
 * 
 */
public abstract class AbstractTopologyManager {

	public static final String SERVICE_EXPORTED_INTERFACES_WILDCARD = "*"; //$NON-NLS-1$

	private BundleContext context;

	private ServiceTracker<IServiceInfoFactory, IServiceInfoFactory> serviceInfoFactoryTracker;

	private ServiceTracker remoteServiceAdminTracker;
	private Object remoteServiceAdminTrackerLock = new Object();

	private final Map<org.osgi.service.remoteserviceadmin.EndpointDescription, ServiceRegistration<IServiceInfo>> registrations =
			new HashMap<org.osgi.service.remoteserviceadmin.EndpointDescription, ServiceRegistration<IServiceInfo>>();
	private final ReentrantLock registrationLock;
	
	private boolean requireServiceExportedConfigs = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager.requireServiceExportedConfigs", //$NON-NLS-1$
					"false")).booleanValue(); //$NON-NLS-1$
	
	public AbstractTopologyManager(BundleContext context) {
		serviceInfoFactoryTracker = new ServiceTracker(
				context, createISIFFilter(context), null);
		serviceInfoFactoryTracker.open();
		this.context = context;
		// Use a FAIR lock here to guarantee that an endpoint removed operation
		// for EP x never executes before its corresponding endpoint added op
		// for EP x.
		// This might happen for an unfair lock (e.g. synchronized) because it
		// doesn't maintain ordering of the waiting threads.
		this.registrationLock = new ReentrantLock(true);
	}

	protected BundleContext getContext() {
		return context;
	}

	protected String getFrameworkUUID() {
		Activator a = Activator.getDefault();
		if (a == null)
			return null;
		return a.getFrameworkUUID();
	}

	public void close() {
		registrationLock.lock();
		try {
			registrations.clear();
		} finally {
			registrationLock.unlock();
		}
		synchronized (remoteServiceAdminTrackerLock) {
			if (remoteServiceAdminTracker != null) {
				remoteServiceAdminTracker.close();
				remoteServiceAdminTracker = null;
			}
		}
		context = null;
	}

	protected void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message);
	}

	protected Filter createRSAFilter() {
		String filterString = "(&(" //$NON-NLS-1$
				+ org.osgi.framework.Constants.OBJECTCLASS
				+ "=" //$NON-NLS-1$
				+ org.osgi.service.remoteserviceadmin.RemoteServiceAdmin.class
						.getName()
				+ ")(" //$NON-NLS-1$
				+ org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.SERVICE_PROP
				+ "=*))"; //$NON-NLS-1$
		try {
			return getContext().createFilter(filterString);
		} catch (InvalidSyntaxException doesNotHappen) {
			// Should never happen
			doesNotHappen.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @since 4.0
	 */
	protected Filter createISIFFilter(BundleContext ctx) {
		String filterString = "(" //$NON-NLS-1$
				+ org.osgi.framework.Constants.OBJECTCLASS
				+ "=" //$NON-NLS-1$
				+ IServiceInfoFactory.class
						.getName()
				+ ")"; //$NON-NLS-1$
		try {
			return ctx.createFilter(filterString);
		} catch (InvalidSyntaxException doesNotHappen) {
			// Should never happen
			doesNotHappen.printStackTrace();
			return null;
		}
	}

	protected org.osgi.service.remoteserviceadmin.RemoteServiceAdmin getRemoteServiceAdmin() {
		synchronized (remoteServiceAdminTrackerLock) {
			if (remoteServiceAdminTracker == null) {
				remoteServiceAdminTracker = new ServiceTracker(
						Activator.getContext(), createRSAFilter(), null);
				remoteServiceAdminTracker.open();
			}
		}
		return (org.osgi.service.remoteserviceadmin.RemoteServiceAdmin) remoteServiceAdminTracker
				.getService();
	}

	/**
	 * @since 3.0
	 */
	protected void advertiseEndpointDescription(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		this.registrationLock.lock();
		try {
			if (this.registrations.containsKey(endpointDescription)) {
				return;
			}
			final IServiceInfoFactory service = serviceInfoFactoryTracker
					.getService();
			if (service != null) {
				final IServiceInfo serviceInfo = service.createServiceInfo(null,
						endpointDescription);
				if (serviceInfo != null) {
					trace("advertiseEndpointDescription", //$NON-NLS-1$
							"advertising endpointDescription=" + endpointDescription +  //$NON-NLS-1$
							" and IServiceInfo " + serviceInfo); //$NON-NLS-1$
					
					final ServiceRegistration<IServiceInfo> registerService = this.context
							.registerService(IServiceInfo.class, serviceInfo, null);
					this.registrations.put(endpointDescription, registerService);
				} else {
					logError(
							"advertiseEndpointDescription",  //$NON-NLS-1$
							"IServiceInfoFactory failed to convert EndpointDescription " + endpointDescription); //$NON-NLS-1$1
				}
			} else {
				logError(
						"advertiseEndpointDescription",  //$NON-NLS-1$
						"no IServiceInfoFactory service found"); //$NON-NLS-1$
			}
		} finally {
			this.registrationLock.unlock();
		}
	}

	/**
	 * @since 3.0
	 */
	protected void unadvertiseEndpointDescription(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		this.registrationLock.lock();
		try {
			final ServiceRegistration<IServiceInfo> serviceRegistration = this.registrations
					.remove(endpointDescription);
			if (serviceRegistration != null) {
				serviceRegistration.unregister();
				return;
			}
		} finally {
			this.registrationLock.unlock();
		}
		logWarning("unadvertiseEndpointDescription", //$NON-NLS-1$
				"Failed to unadvertise endpointDescription: " //$NON-NLS-1$
						+ endpointDescription
						+ ". Seems it was never advertised."); //$NON-NLS-1$
	}

	protected void logError(String methodName, String message,
			Throwable exception) {
		LogUtility.logError(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message, exception);
	}

	protected void logError(String methodName, String message, IStatus result) {
		LogUtility.logError(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), result);
	}

	protected void trace(String methodName, String message) {
		LogUtility.trace(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message);
	}

	protected void logError(String methodName, String message) {
		LogUtility.logError(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message);
	}

	/**
	 * @since 3.0
	 */
	protected void handleECFEndpointAdded(
			EndpointDescription endpointDescription) {
		trace("handleEndpointAdded", "endpointDescription=" //$NON-NLS-1$ //$NON-NLS-2$
				+ endpointDescription);
		// Import service
		getRemoteServiceAdmin().importService(endpointDescription);
	}

	/**
	 * @since 3.0
	 */
	protected void handleECFEndpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		trace("handleEndpointRemoved", "endpointDescription=" //$NON-NLS-1$ //$NON-NLS-2$
				+ endpointDescription);
		RemoteServiceAdmin rsa = (RemoteServiceAdmin) getRemoteServiceAdmin();
		List<RemoteServiceAdmin.ImportRegistration> importedRegistrations = rsa
				.getImportedRegistrations();
		org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription ed = (org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) endpointDescription;
		for (RemoteServiceAdmin.ImportRegistration importedRegistration : importedRegistrations) {
			if (importedRegistration.match(ed)) {
				trace("handleEndpointRemoved", "closing importedRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
						+ importedRegistration);
				importedRegistration.close();
			}
		}
	}

	private String isInterested(Object scopeobj,
			org.osgi.service.remoteserviceadmin.EndpointDescription description) {
		if (scopeobj instanceof List<?>) {
			List<String> scope = (List<String>) scopeobj;
			for (Iterator<String> it = scope.iterator(); it.hasNext();) {
				String filter = it.next();

				if (description.matches(filter)) {
					return filter;
				}
			}
		} else if (scopeobj instanceof String[]) {
			String[] scope = (String[]) scopeobj;
			for (String filter : scope) {
				if (description.matches(filter)) {
					return filter;
				}
			}
		} else if (scopeobj instanceof String) {
			StringTokenizer st = new StringTokenizer((String) scopeobj, " "); //$NON-NLS-1$
			for (; st.hasMoreTokens();) {
				String filter = st.nextToken();
				if (description.matches(filter)) {
					return filter;
				}
			}
		}
		return null;
	}

	private void notifyOtherEndpointListeners(
			EndpointListener exceptEndpointListener,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			boolean added) {
		ServiceReference[] listeners = null;
		try {
			listeners = context.getServiceReferences(
					EndpointListener.class.getName(),
					"(" + EndpointListener.ENDPOINT_LISTENER_SCOPE + "=*)"); //$NON-NLS-1$//$NON-NLS-2$
		} catch (InvalidSyntaxException doesNotHappen) {
			// Should never happen
			doesNotHappen.printStackTrace();
		}
		if (listeners != null) {
			for (int i = 0; i < listeners.length; i++) {
				EndpointListener listener = (EndpointListener) getContext()
						.getService(listeners[i]);
				if (listener != exceptEndpointListener) {
					Object scope = listeners[i]
							.getProperty(EndpointListener.ENDPOINT_LISTENER_SCOPE);
					String matchedFilter = isInterested(scope,
							endpointDescription);
					if (matchedFilter != null) {
						if (added)
							listener.endpointAdded(endpointDescription,
									matchedFilter);
						else
							listener.endpointRemoved(endpointDescription,
									matchedFilter);
					}
				}
			}
		}
	}

	/**
	 * @since 3.0
	 */
	protected void handleNonECFEndpointAdded(
			EndpointListener listener,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		notifyOtherEndpointListeners(listener, endpointDescription, true);
	}

	/**
	 * @since 3.0
	 */
	protected void handleNonECFEndpointRemoved(
			EndpointListener listener,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		notifyOtherEndpointListeners(listener, endpointDescription, false);
	}

	/**
	 * @since 3.0
	 */
	protected void handleNonECFEndpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			String matchedFilter) {
		advertiseEndpointDescription(endpointDescription);
	}

	/**
	 * @since 3.0
	 */
	protected void handleAdvertisingResult(
			IStatus result,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			boolean advertise) {
		if (!result.isOK())
			logError(
					"handleAdvertisingResult", //$NON-NLS-1$
					(advertise ? "Advertise" : "Unadvertise") + " of endpointDescription=" + endpointDescription //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ " FAILED", result); //$NON-NLS-1$
	}

	protected void handleInvalidImportRegistration(
			ImportRegistration importRegistration, Throwable t) {
		logError("handleInvalidImportRegistration", "importRegistration=" //$NON-NLS-1$ //$NON-NLS-2$
				+ importRegistration, t);
	}

	/**
	 * @since 3.0
	 */
	protected void handleEvent(ServiceEvent event, Map listeners) {
		switch (event.getType()) {
		case ServiceEvent.MODIFIED:
			handleServiceModifying(event.getServiceReference());
			break;
		case ServiceEvent.REGISTERED:
			handleServiceRegistering(event.getServiceReference());
			break;
		default:
			break;
		}
	}

	protected void handleServiceRegistering(ServiceReference serviceReference) {
		// Using OSGI 5 Chap 13 Remote Services spec, get the specified remote
		// interfaces for the given service reference
		String[] exportedInterfaces = PropertiesUtil
				.getExportedInterfaces(serviceReference);
		// If no remote interfaces set, then we don't do anything with it
		if (exportedInterfaces == null)
			return;
		
		// Get serviceExportedConfigs property
		String[] serviceExportedConfigs = PropertiesUtil
				.getStringArrayFromPropertyValue(serviceReference
						.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS));
		// If requireServiceExportedConfigs is set to true (default is false) then if serviceExportedConfigs 
		// is null/not set, then we don't do anything with this service registration
		if (requireServiceExportedConfigs
				&& (serviceExportedConfigs == null || Arrays.asList(
						serviceExportedConfigs).size() == 0))
			return;
		// If we get this far, then we are going to export it
		// prepare export properties
		Map<String, Object> exportProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);
		exportProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
						exportedInterfaces);
		trace("handleServiceRegistering", "serviceReference=" //$NON-NLS-1$ //$NON-NLS-2$
				+ serviceReference + " exportProperties=" + exportProperties); //$NON-NLS-1$
		// Do the export with RSA
		getRemoteServiceAdmin().exportService(serviceReference,
				exportProperties);
	}

	protected void handleServiceModifying(ServiceReference serviceReference) {
		logWarning(
				"handleServiceModifying", "serviceReference=" + serviceReference + " modified with no response"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected void handleServiceUnregistering(ServiceReference serviceReference) {
		List<RemoteServiceAdmin.ExportRegistration> exportedRegistrations = ((RemoteServiceAdmin) getRemoteServiceAdmin())
				.getExportedRegistrations();
		for (RemoteServiceAdmin.ExportRegistration exportedRegistration : exportedRegistrations) {
			if (exportedRegistration.match(serviceReference)) {
				trace("handleServiceUnregistering", "closing exportRegistration for serviceReference=" //$NON-NLS-1$ //$NON-NLS-2$
								+ serviceReference);
				exportedRegistration.close();
			}
		}
	}
}
