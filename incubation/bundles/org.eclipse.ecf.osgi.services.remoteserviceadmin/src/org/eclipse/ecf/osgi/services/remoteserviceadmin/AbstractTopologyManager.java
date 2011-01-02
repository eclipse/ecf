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
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractTopologyManager {

	public static final String SERVICE_EXPORTED_INTERFACES_WILDCARD = "*";

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
		String filterString = "(&("
				+ org.osgi.framework.Constants.OBJECTCLASS
				+ "="
				+ org.osgi.service.remoteserviceadmin.RemoteServiceAdmin.class
						.getName()
				+ ")("
				+ org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.SERVICE_PROP
				+ "=*))";
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
				remoteServiceAdminTracker.open(true);
			}
		}
		return (org.osgi.service.remoteserviceadmin.RemoteServiceAdmin) remoteServiceAdminTracker
				.getService();
	}

	protected void advertiseEndpointDescription(
			EndpointDescription endpointDescription) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser();
		if (advertiser == null) {
			logWarning("advertiseExportedRegistration",
					"No endpoint description advertiser available for endpointDescription="
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
