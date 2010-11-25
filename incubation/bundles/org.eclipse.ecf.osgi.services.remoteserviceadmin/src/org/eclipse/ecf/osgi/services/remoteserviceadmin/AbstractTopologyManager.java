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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DiscoveryImpl;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractTopologyManager {

	public static final String SERVICE_EXPORTED_INTERFACES_WILDCARD = "*";

	private BundleContext context;

	private DiscoveryImpl discovery;

	private ServiceTracker hostContainerSelectorTracker;
	private Object hostContainerSelectorTrackerLock = new Object();

	private ServiceTracker consumerContainerSelectorTracker;
	private Object consumerContainerSelectorTrackerLock = new Object();

	private org.osgi.service.remoteserviceadmin.RemoteServiceAdmin remoteServiceAdmin;
	private Object remoteServiceAdminLock = new Object();

	private EndpointDescriptionAdvertiser endpointDescriptionAdvertiser;
	private ServiceRegistration defaultEndpointDescriptionAdvertiserRegistration;
	private ServiceTracker endpointDescriptionAdvertiserTracker;
	private Object endpointDescriptionAdvertiserTrackerLock = new Object();

	public AbstractTopologyManager(BundleContext context,
			DiscoveryImpl discovery) {
		this.context = context;
		this.discovery = discovery;
	}

	public void start() throws Exception {
		// Create default publisher
		endpointDescriptionAdvertiser = new EndpointDescriptionAdvertiser(
				getDiscovery());
		// Register with minimum service ranking so others can customize
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		defaultEndpointDescriptionAdvertiserRegistration = getContext()
				.registerService(
						IEndpointDescriptionAdvertiser.class.getName(),
						endpointDescriptionAdvertiser, properties);
	}

	protected BundleContext getContext() {
		return context;
	}

	protected DiscoveryImpl getDiscovery() {
		return discovery;
	}

	protected IHostContainerSelector getHostContainerSelector() {
		synchronized (hostContainerSelectorTrackerLock) {
			if (hostContainerSelectorTracker == null) {
				hostContainerSelectorTracker = new ServiceTracker(context,
						IHostContainerSelector.class.getName(), null);
				hostContainerSelectorTracker.open();
			}
		}
		return (IHostContainerSelector) hostContainerSelectorTracker
				.getService();
	}

	protected IConsumerContainerSelector getConsumerContainerSelector() {
		synchronized (consumerContainerSelectorTrackerLock) {
			if (consumerContainerSelectorTracker == null) {
				consumerContainerSelectorTracker = new ServiceTracker(context,
						IConsumerContainerSelector.class.getName(), null);
				consumerContainerSelectorTracker.open();
			}
		}
		return (IConsumerContainerSelector) consumerContainerSelectorTracker
				.getService();
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
		synchronized (endpointDescriptionAdvertiserTrackerLock) {
			if (endpointDescriptionAdvertiserTracker != null) {
				endpointDescriptionAdvertiserTracker.close();
				endpointDescriptionAdvertiserTracker = null;
			}
		}
		if (defaultEndpointDescriptionAdvertiserRegistration != null) {
			defaultEndpointDescriptionAdvertiserRegistration.unregister();
			defaultEndpointDescriptionAdvertiserRegistration = null;
		}
		if (endpointDescriptionAdvertiser != null) {
			endpointDescriptionAdvertiser.close();
			endpointDescriptionAdvertiser = null;
		}
		discovery = null;
		context = null;
	}

	protected org.osgi.service.remoteserviceadmin.RemoteServiceAdmin selectRemoteServiceAdmin(
			ServiceReference serviceReference, String[] exportedInterfaces,
			String[] exportedConfigs, String[] serviceIntents,
			IRemoteServiceContainer[] rsContainers) {
		synchronized (remoteServiceAdminLock) {
			if (remoteServiceAdmin == null)
				remoteServiceAdmin = new RemoteServiceAdmin(getContext());
		}
		return remoteServiceAdmin;
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

	protected String[] getServiceIntents(ServiceReference serviceReference) {
		List results = new ArrayList();
		String[] intents = getStringArrayFromPropertyValue(serviceReference
				.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS));
		if (intents != null)
			results.addAll(Arrays.asList(intents));
		String[] exportedIntents = getStringArrayFromPropertyValue(serviceReference
				.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS));
		if (exportedIntents != null)
			results.addAll(Arrays.asList(exportedIntents));
		String[] extraIntents = getStringArrayFromPropertyValue(serviceReference
				.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
		if (extraIntents != null)
			results.addAll(Arrays.asList(extraIntents));
		if (results.size() == 0)
			return null;
		return (String[]) results.toArray(new String[] {});
	}

	protected void logWarning(String methodName, String debugOption,
			Class clazz, String message) {
		// xxx todo
	}

	protected void publishExportedRegistrations(
			Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> registrations) {
		for (org.osgi.service.remoteserviceadmin.ExportRegistration reg : registrations) {
			if (reg instanceof ExportRegistration) {
				publishExportedRegistration((ExportRegistration) reg);
			}
		}
	}

	private void publishExportedRegistration(ExportRegistration reg) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser();
		if (advertiser == null) {
			logError("advertiseExportedRegistration",
					"No endpoint description advertiser available to advertise ExportRegistration="
							+ reg);
			return;
		}
		// Now advertise endpoint description using endpoint description
		// advertiser
		IStatus result = advertiser.advertise((EndpointDescription) reg
				.getExportReference().getExportedEndpoint());
		if (!result.isOK())
			logError("advertiseExportedRegistration",
					"Advertise of ExportRegistration=" + reg + " FAILED",
					result);
	}

	protected void logError(String method, String message, IStatus result) {
		// TODO Auto-generated method stub
		logError(method, method);

	}

	protected void trace(String method, String message) {
		// TODO Auto-generated method stub
		System.out.println("TopologyManager." + method + ": " + message);
	}

	protected void logWarning(String string) {
		System.out.println(string);
	}

	protected void logError(String method, String message) {
		// TODO Auto-generated method stub

	}

}
