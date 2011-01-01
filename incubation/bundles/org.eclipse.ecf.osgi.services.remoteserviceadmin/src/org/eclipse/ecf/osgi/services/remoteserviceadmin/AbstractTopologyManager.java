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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractTopologyManager {

	public static final String SERVICE_EXPORTED_INTERFACES_WILDCARD = "*";

	private BundleContext context;

	private ServiceTracker endpointDescriptionAdvertiserTracker;
	private Object endpointDescriptionAdvertiserTrackerLock = new Object();

	private RemoteServiceAdmin remoteServiceAdmin;
	private Object remoteServiceAdminLock = new Object();

	public AbstractTopologyManager(BundleContext context) {
		this.context = context;
	}

	public void start() throws Exception {
	}

	protected BundleContext getContext() {
		return context;
	}

	protected IEndpointDescriptionAdvertiser getEndpointDescriptionAdvertiser() {
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
		context = null;
	}

	protected org.osgi.service.remoteserviceadmin.RemoteServiceAdmin selectExportRemoteServiceAdmin(
			ServiceReference serviceReference, String[] exportedInterfaces) {
		synchronized (remoteServiceAdminLock) {
			if (remoteServiceAdmin == null)
				remoteServiceAdmin = new RemoteServiceAdmin(getContext());
		}
		return remoteServiceAdmin;
	}

	protected RemoteServiceAdmin selectUnexportRemoteServiceAdmin(
			ServiceReference serviceReference) {
		synchronized (remoteServiceAdminLock) {
			return remoteServiceAdmin;
		}
	}

	protected RemoteServiceAdmin selectImportRemoteServiceAdmin(
			EndpointDescription endpoint) {
		synchronized (remoteServiceAdminLock) {
			if (remoteServiceAdmin == null)
				remoteServiceAdmin = new RemoteServiceAdmin(getContext());
		}
		return remoteServiceAdmin;
	}

	protected RemoteServiceAdmin selectUnimportRemoteServiceAdmin(
			EndpointDescription endpoint) {
		synchronized (remoteServiceAdminLock) {
			if (remoteServiceAdmin == null)
				remoteServiceAdmin = new RemoteServiceAdmin(getContext());
		}
		return remoteServiceAdmin;
	}

	protected void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message);
	}

	protected void advertiseEndpointDescription(
			EndpointDescription endpointDescription) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser();
		if (advertiser == null) {
			logError(
					"advertiseExportedRegistration",
					"No endpoint description advertiser available to advertise endpointDescription="
							+ endpointDescription);
			return;
		}
		// Now advertise endpoint description using endpoint description
		// advertiser
		IStatus result = advertiser.advertise(endpointDescription);
		if (!result.isOK())
			logError("advertiseExportedRegistration",
					"Advertise of endpointDescription=" + endpointDescription
							+ " FAILED", result);
	}

	protected void unadvertiseEndpointDescription(
			EndpointDescription endpointDescription) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser();
		if (advertiser == null) {
			logError(
					"unadvertiseEndpointDescription",
					"No endpoint description advertiser available to unadvertise endpointDescription="
							+ endpointDescription);
			return;
		}
		// Now unadvertise endpoint description using endpoint description
		// advertiser
		IStatus result = advertiser.unadvertise(endpointDescription);
		if (!result.isOK())
			logError("unadvertiseEndpointDescription",
					"Unadvertise of endpointDescription=" + endpointDescription
							+ " FAILED", result);
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

}
