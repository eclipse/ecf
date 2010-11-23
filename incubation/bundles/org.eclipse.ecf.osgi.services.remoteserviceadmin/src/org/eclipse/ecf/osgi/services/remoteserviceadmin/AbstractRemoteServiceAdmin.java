/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.Collection;

import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractRemoteServiceAdmin {

	public static final String SERVICE_EXPORTED_INTERFACES_WILDCARD = "*";

	private BundleContext context;
	
	private ServiceTracker hostContainerSelectorTracker;
	private Object hostContainerSelectorTrackerLock = new Object();
	
	private ServiceTracker consumerContainerSelectorTracker;
	private Object consumerContainerSelectorTrackerLock = new Object();
	
	public AbstractRemoteServiceAdmin(BundleContext context) {
		this.context = context;
	}
	
	protected BundleContext getContext() {
		return context;
	}
	
	protected IHostContainerSelector getHostContainerSelector() {
		synchronized (hostContainerSelectorTrackerLock) {
			if (hostContainerSelectorTracker == null) {
				hostContainerSelectorTracker = new ServiceTracker(context,IHostContainerSelector.class.getName(),null);
				hostContainerSelectorTracker.open();
			}
		}
		return (IHostContainerSelector) hostContainerSelectorTracker.getService();
	}
	
	protected IConsumerContainerSelector getConsumerContainerSelector() {
		synchronized (consumerContainerSelectorTrackerLock) {
			if (consumerContainerSelectorTracker == null) {
				consumerContainerSelectorTracker = new ServiceTracker(context,IConsumerContainerSelector.class.getName(),null);
				consumerContainerSelectorTracker.open();
			}
		}
		return (IConsumerContainerSelector) consumerContainerSelectorTracker.getService();
	}
	
	public void close() {
		synchronized (hostContainerSelectorTrackerLock) {
			if (hostContainerSelectorTracker != null) {
				hostContainerSelectorTracker.close();
				hostContainerSelectorTracker = null;
			}
		}
		synchronized (consumerContainerSelectorTrackerLock) {
			if (consumerContainerSelectorTracker != null) {
				consumerContainerSelectorTracker.close();
				consumerContainerSelectorTracker = null;
			}
		}

	}

	protected String[] getStringArrayFromPropertyValue(Object value) {
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

	protected String[] getExportedInterfaces(ServiceReference serviceReference) {
		// Get the OSGi 4.2 specified required service property value
		Object propValue = serviceReference
				.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES);
		// If the required property is not set then it's not being registered
		// as a remote service so we return null
		if (propValue == null)
			return null;
		boolean wildcard = propValue
				.equals(SERVICE_EXPORTED_INTERFACES_WILDCARD);
		if (wildcard)
			return (String[]) serviceReference
					.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
		else {
			final String[] stringValue = getStringArrayFromPropertyValue(propValue);
			if (stringValue != null
					&& stringValue.length == 1
					&& stringValue[0]
							.equals(SERVICE_EXPORTED_INTERFACES_WILDCARD)) {
				logWarning(
								"getExportedInterfaces", //$NON-NLS-1$
								DebugOptions.REMOTE_SERVICE_ADMIN, this.getClass(),
								"Service Exported Interfaces Wildcard does not accept String[\"*\"]"); //$NON-NLS-1$
			}
			return stringValue;
		}
	}

	protected void logWarning(String methodName, String debugOption, Class clazz,
			String message) {
				// xxx todo
			}
}
