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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
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

	private ServiceTracker endpointDescriptionAdvertiserTracker;
	private Object endpointDescriptionAdvertiserTrackerLock = new Object();

	private ServiceTracker remoteServiceAdminTracker;
	private Object remoteServiceAdminTrackerLock = new Object();

	public AbstractTopologyManager(BundleContext context) {
		this.context = context;
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

	/**
	 * @since 3.0
	 */
	protected IEndpointDescriptionAdvertiser getEndpointDescriptionAdvertiser(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		synchronized (endpointDescriptionAdvertiserTrackerLock) {
			if (endpointDescriptionAdvertiserTracker == null) {
				endpointDescriptionAdvertiserTracker = new ServiceTracker(
						getContext(),
						IEndpointDescriptionAdvertiser.class.getName(), null);
				endpointDescriptionAdvertiserTracker.open();
			}
		}
		return (IEndpointDescriptionAdvertiser) endpointDescriptionAdvertiserTracker
				.getService();
	}

	public void close() {
		synchronized (endpointDescriptionAdvertiserTrackerLock) {
			if (endpointDescriptionAdvertiserTracker != null) {
				endpointDescriptionAdvertiserTracker.close();
				endpointDescriptionAdvertiserTracker = null;
			}
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
		} catch (InvalidSyntaxException e) {
			// Should never happen
			return null;
		}
	}

	protected org.osgi.service.remoteserviceadmin.RemoteServiceAdmin getRemoteServiceAdmin() {
		synchronized (remoteServiceAdminTrackerLock) {
			if (remoteServiceAdminTracker == null) {
				remoteServiceAdminTracker = new ServiceTracker(getContext(),
						createRSAFilter(), null);
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
			EndpointDescription endpointDescription) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser(endpointDescription);
		if (advertiser == null) {
			logWarning("advertiseExportedRegistration", //$NON-NLS-1$
					"No endpoint description advertiser available for endpointDescription=" //$NON-NLS-1$
							+ endpointDescription);
			return;
		}
		// Now advertise endpoint description using endpoint description
		// advertiser
		trace("advertiseEndpointDescription", //$NON-NLS-1$
				"advertising endpointDescription=" + endpointDescription //$NON-NLS-1$
						+ " with advertiser=" + advertiser); //$NON-NLS-1$
		IStatus result = advertiser.advertise(endpointDescription);
		if (!result.isOK())
			logError("advertiseExportedRegistration", //$NON-NLS-1$
					"Advertise of endpointDescription=" + endpointDescription //$NON-NLS-1$
							+ " FAILED", result); //$NON-NLS-1$
	}

	/**
	 * @since 3.0
	 */
	protected void unadvertiseEndpointDescription(
			EndpointDescription endpointDescription) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser(endpointDescription);
		if (advertiser == null) {
			logError(
					"unadvertiseEndpointDescription", //$NON-NLS-1$
					"No endpoint description advertiser available to unadvertise endpointDescription=" //$NON-NLS-1$
							+ endpointDescription);
			return;
		}
		// Now unadvertise endpoint description using endpoint description
		// advertiser
		IStatus result = advertiser.unadvertise(endpointDescription);
		if (!result.isOK())
			logError("unadvertiseEndpointDescription", //$NON-NLS-1$
					"Unadvertise of endpointDescription=" + endpointDescription //$NON-NLS-1$
							+ " FAILED", result); //$NON-NLS-1$
	}

	protected void unadvertiseEndpointDescription(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser(endpointDescription);
		if (advertiser == null) {
			logError(
					"unadvertiseEndpointDescription", //$NON-NLS-1$
					"No endpoint description advertiser available to unadvertise endpointDescription=" //$NON-NLS-1$
							+ endpointDescription);
			return;
		}
		// Now unadvertise endpoint description using endpoint description
		// advertiser
		IStatus result = advertiser.unadvertise(endpointDescription);
		if (!result.isOK())
			logError("unadvertiseEndpointDescription", //$NON-NLS-1$
					"Unadvertise of endpointDescription=" + endpointDescription //$NON-NLS-1$
							+ " FAILED", result); //$NON-NLS-1$
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
	protected void handleECFEndpointAdded(EndpointDescription endpointDescription) {
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

	private String isInterested(Object scopeobj, org.osgi.service.remoteserviceadmin.EndpointDescription description) {
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
			StringTokenizer st = new StringTokenizer((String)scopeobj, " "); //$NON-NLS-1$
			for (; st.hasMoreTokens();) {
				String filter = st.nextToken();
				if (description.matches(filter)) {
					return filter;
				}
			}
		}
		return null;
	}

	private void notifyOtherEndpointListeners(EndpointListener exceptEndpointListener, org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription, boolean added) {
		ServiceReference[] listeners = null;
		try {
			listeners = context.getServiceReferences(
					EndpointListener.class.getName(),
					"(" + EndpointListener.ENDPOINT_LISTENER_SCOPE + "=*)");  //$NON-NLS-1$//$NON-NLS-2$
		} catch (InvalidSyntaxException e) {
			// Should never happen
		}
		if (listeners != null) {
			for(int i=0; i < listeners.length; i++) {
				EndpointListener listener = (EndpointListener) getContext().getService(listeners[i]);
				if (listener != exceptEndpointListener) {
					Object scope = listeners[i].getProperty(EndpointListener.ENDPOINT_LISTENER_SCOPE);
					String matchedFilter = isInterested(scope, endpointDescription);
					if (matchedFilter != null) {
						if (added) listener.endpointAdded(endpointDescription, matchedFilter);
						else listener.endpointRemoved(endpointDescription, matchedFilter);
					}
				}
			}
		}
	}
	/**
	 * @since 3.0
	 */
	protected void handleNonECFEndpointAdded(EndpointListener listener,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		notifyOtherEndpointListeners(listener, endpointDescription, true);
	}

	/**
	 * @since 3.0
	 */
	protected void handleNonECFEndpointRemoved(EndpointListener listener,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		notifyOtherEndpointListeners(listener, endpointDescription, false);
	}

	protected void advertiseEndpointDescription(org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		// forward to all other endpoint listener
	IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser(endpointDescription);
	if (advertiser == null) {
		logWarning("handleOSGiEndpointAdded", //$NON-NLS-1$
				"No endpoint description advertiser available for endpointDescription=" //$NON-NLS-1$
						+ endpointDescription);
		return;
	}
	// Now advertise endpoint description using endpoint description
	// advertiser
	trace("handleOSGiEndpointAdded", //$NON-NLS-1$
			"advertising endpointDescription=" + endpointDescription //$NON-NLS-1$
					+ " with advertiser=" + advertiser); //$NON-NLS-1$
	IStatus result = advertiser.advertise(endpointDescription);
	if (!result.isOK())
		logError("handleOSGiEndpointAdded", //$NON-NLS-1$
				"Advertise of endpointDescription=" + endpointDescription //$NON-NLS-1$
						+ " FAILED", result); //$NON-NLS-1$
	}
	/**
	 * @since 3.0
	 */
	protected void handleNonECFEndpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			String matchedFilter) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser(endpointDescription);
		if (advertiser == null) {
			logWarning("handleOSGiEndpointRemoved", //$NON-NLS-1$
					"No endpoint description advertiser available for endpointDescription=" //$NON-NLS-1$
							+ endpointDescription);
			return;
		}
		// Now advertise endpoint description using endpoint description
		// advertiser
		trace("handleOSGiEndpointRemoved", //$NON-NLS-1$
				"Unadvertise endpointDescription=" + endpointDescription //$NON-NLS-1$
						+ " with advertiser=" + advertiser); //$NON-NLS-1$
		IStatus result = advertiser.unadvertise(endpointDescription);
		if (!result.isOK())
			logError("handleOSGiEndpointRemoved", //$NON-NLS-1$
					"Unadvertise of endpointDescription=" + endpointDescription //$NON-NLS-1$
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
		// Using OSGI 4.2 Chap 13 Remote Services spec, get the specified remote
		// interfaces for the given service reference
		String[] exportedInterfaces = PropertiesUtil
				.getExportedInterfaces(serviceReference);
		// If no remote interfaces set, then we don't do anything with it
		if (exportedInterfaces == null)
			return;

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

	/**
	 * @since 3.0
	 */
	protected void handleRemoteServiceAdminEvent(RemoteServiceAdminEvent event) {
		if (!(event instanceof RemoteServiceAdmin.RemoteServiceAdminEvent))
			return;

		int eventType = event.getType();
		RemoteServiceAdmin.RemoteServiceAdminEvent rsaEvent = (RemoteServiceAdmin.RemoteServiceAdminEvent) event;
		switch (eventType) {
		case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
			advertiseEndpointDescription(rsaEvent.getEndpointDescription());
			break;
		case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
			unadvertiseEndpointDescription(rsaEvent.getEndpointDescription());
			break;
		case RemoteServiceAdminEvent.EXPORT_ERROR:
			logError("handleExportError", "Export error with event=" + rsaEvent); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
			break;
		case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
			break;
		case RemoteServiceAdminEvent.IMPORT_ERROR:
			break;
		default:
			logWarning(
					"handleRemoteAdminEvent", "RemoteServiceAdminEvent=" + rsaEvent + " received with unrecognized type"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
}
