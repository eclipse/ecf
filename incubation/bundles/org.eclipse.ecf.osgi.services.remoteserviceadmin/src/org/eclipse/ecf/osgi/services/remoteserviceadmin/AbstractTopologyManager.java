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
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
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

	private boolean hostAutoCreateContainer = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.remoteserviceadmin.hostAutoCreateContainer",
					"true")).booleanValue();
	private String[] hostDefaultConfigTypes = new String[] { System
			.getProperty(
					"org.eclipse.ecf.osgi.services.remoteserviceadmin.hostDefaultConfigType",
					"ecf.generic.server") };

	private boolean consumerAutoCreateContainer = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.remoteserviceadmin.consumerAutoCreateContainer",
					"true")).booleanValue();

	private HostContainerSelector hostContainerSelector;
	private ServiceRegistration defaultHostContainerSelectorRegistration;
	private ServiceTracker hostContainerSelectorTracker;
	private Object hostContainerSelectorTrackerLock = new Object();

	private ConsumerContainerSelector consumerContainerSelector;
	private ServiceRegistration defaultConsumerContainerSelectorRegistration;
	private ServiceTracker consumerContainerSelectorTracker;
	private Object consumerContainerSelectorTrackerLock = new Object();

	private EndpointDescriptionAdvertiser endpointDescriptionAdvertiser;
	private ServiceRegistration defaultEndpointDescriptionAdvertiserRegistration;
	private ServiceTracker endpointDescriptionAdvertiserTracker;
	private Object endpointDescriptionAdvertiserTrackerLock = new Object();

	private org.osgi.service.remoteserviceadmin.RemoteServiceAdmin remoteServiceAdmin;
	private Object remoteServiceAdminLock = new Object();

	public AbstractTopologyManager(BundleContext context,
			DiscoveryImpl discovery) {
		this.context = context;
		this.discovery = discovery;
	}

	public void setHostAutoCreateContainer(boolean value) {
		this.hostAutoCreateContainer = value;
	}

	public void setHostDefaultConfigTypes(String[] configTypes) {
		this.hostDefaultConfigTypes = configTypes;
	}

	public void setConsumerContainerSelector(boolean value) {
		this.consumerAutoCreateContainer = value;
	}

	public void start() throws Exception {
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		// create and register default host container selector. Since this is
		// registered with minimum service ranking
		// others can override this default simply by registering a
		// IHostContainerSelector implementer
		hostContainerSelector = new HostContainerSelector(
				hostDefaultConfigTypes, hostAutoCreateContainer);
		defaultHostContainerSelectorRegistration = getContext()
				.registerService(IHostContainerSelector.class.getName(),
						hostContainerSelector, properties);
		// create and register default consumer container selector. Since this
		// is registered with minimum service ranking
		// others can override this default simply by registering a
		// IConsumerContainerSelector implementer
		consumerContainerSelector = new ConsumerContainerSelector(
				consumerAutoCreateContainer);
		defaultConsumerContainerSelectorRegistration = getContext()
				.registerService(IConsumerContainerSelector.class.getName(),
						consumerContainerSelector, properties);
		// create and register default endpoint description advertiser. Since
		// this is registered with minimum service ranking
		// others can override this default simply by registering a
		// IEndpointDescriptionAdvertiser implementer
		endpointDescriptionAdvertiser = new EndpointDescriptionAdvertiser(
				getDiscovery());
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

	private void closeEndpointDescriptionAdvertiser() {
		// tracker
		synchronized (endpointDescriptionAdvertiserTrackerLock) {
			if (endpointDescriptionAdvertiserTracker != null) {
				endpointDescriptionAdvertiserTracker.close();
				endpointDescriptionAdvertiserTracker = null;
			}
		}
		// registration
		if (defaultEndpointDescriptionAdvertiserRegistration != null) {
			defaultEndpointDescriptionAdvertiserRegistration.unregister();
			defaultEndpointDescriptionAdvertiserRegistration = null;
		}
		// default
		if (endpointDescriptionAdvertiser != null) {
			endpointDescriptionAdvertiser.close();
			endpointDescriptionAdvertiser = null;
		}
	}

	private void closeConsumerContainerSelector() {
		synchronized (consumerContainerSelectorTrackerLock) {
			if (consumerContainerSelectorTracker != null) {
				consumerContainerSelectorTracker.close();
				consumerContainerSelectorTracker = null;
			}
		}
		if (defaultConsumerContainerSelectorRegistration != null) {
			defaultConsumerContainerSelectorRegistration.unregister();
			defaultConsumerContainerSelectorRegistration = null;
		}
		if (consumerContainerSelector != null) {
			consumerContainerSelector.close();
			consumerContainerSelector = null;
		}
	}

	private void closeHostContainerSelector() {
		synchronized (hostContainerSelectorTrackerLock) {
			if (hostContainerSelectorTracker != null) {
				hostContainerSelectorTracker.close();
				hostContainerSelectorTracker = null;
			}
		}
		if (defaultHostContainerSelectorRegistration != null) {
			defaultHostContainerSelectorRegistration.unregister();
			defaultHostContainerSelectorRegistration = null;
		}
		if (hostContainerSelector != null) {
			hostContainerSelector.close();
			hostContainerSelector = null;
		}
	}

	public void close() {
		closeEndpointDescriptionAdvertiser();
		closeConsumerContainerSelector();
		closeHostContainerSelector();
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
						"getExportedInterfaces", "Service Exported Interfaces Wildcard does not accept String[\"*\"]"); //$NON-NLS-1$
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

	protected void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.TOPOLOGY_MANAGER,
				this.getClass(), message);
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
