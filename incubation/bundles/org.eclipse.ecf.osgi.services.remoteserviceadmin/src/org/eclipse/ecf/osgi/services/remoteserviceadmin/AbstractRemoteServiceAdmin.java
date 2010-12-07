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

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractRemoteServiceAdmin {

	private BundleContext context;

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

	public AbstractRemoteServiceAdmin(BundleContext context) {
		this.context = context;
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
	}

	protected BundleContext getContext() {
		return context;
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

	protected void logError(String methodName, String message, IStatus status) {
		LogUtility.logError(methodName, DebugOptions.REMOTE_SERVICE_ADMIN,
				this.getClass(), status);
	}

	protected void trace(String methodName, String message) {
		LogUtility.trace(methodName, DebugOptions.REMOTE_SERVICE_ADMIN,
				this.getClass(), message);
	}

	protected void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.REMOTE_SERVICE_ADMIN,
				this.getClass(), message);
	}

	protected void logError(String methodName, String message, Throwable t) {
		LogUtility.logError(methodName, DebugOptions.REMOTE_SERVICE_ADMIN,
				this.getClass(), message, t);
	}

	protected void logError(String methodName, String message) {
		logError(methodName,message,(Throwable)null);
	}
	
	protected Object getService(ServiceReference serviceReference) {
		return context.getService(serviceReference);
	}

	protected Object getPropertyValue(String propertyName,
			ServiceReference serviceReference, Map<String, Object> properties) {
		Object result = properties.get(propertyName);
		return (result == null)?serviceReference.getProperty(propertyName):result;
	}

	protected EndpointDescription createExportEndpointDescription(
			ServiceReference serviceReference, Map<String, Object> properties,
			String[] exportedInterfaces,
			String[] serviceIntents,
			IRemoteServiceRegistration rsRegistration,
			IRemoteServiceContainer rsContainer) {

		IContainer container = rsContainer.getContainer();
		ID containerID = container.getID();
		
		Map<String, Object> endpointDescriptionProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);
		// ENDPOINT_SERVICE_ID
		Long serviceId = (Long) getPropertyValue(org.osgi.framework.Constants.SERVICE_ID, serviceReference, properties);
		endpointDescriptionProperties.put(org.osgi.framework.Constants.SERVICE_ID, serviceId);
		// ENDPOINT_FRAMEWORK_ID
		String frameworkId = Activator.getDefault().getFrameworkUUID();
		endpointDescriptionProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID, frameworkId);
		// OBJECTCLASS
		endpointDescriptionProperties.put(org.osgi.framework.Constants.OBJECTCLASS, exportedInterfaces);
		// ENDPOINT_ID
		String endpointId = (String) getPropertyValue(
				org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
				serviceReference, properties);
		if (endpointId == null)
			endpointId = containerID.getName();
		endpointDescriptionProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID, endpointId);
		
		// SERVICE_IMPORTED_CONFIGS...set to ECF constant
		String serviceImportedConfigs = "ecf.service.imported.configs.default";
		endpointDescriptionProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS, serviceImportedConfigs);
		
		// SERVICE_INTENTS
		if (serviceIntents != null) endpointDescriptionProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS, serviceIntents);
		
		// REMOTE_INTENTS_SUPPORTED 
		String[] remoteIntentsSupported = getSupportedIntents(container);
		if (remoteIntentsSupported != null) endpointDescriptionProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED, remoteIntentsSupported);
		// REMOTE_CONFIGS_SUPPORTED
		String[] remoteConfigsSupported = getSupportedConfigs(container);
		if (remoteConfigsSupported != null) endpointDescriptionProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED, remoteConfigsSupported);
		
		// If connectTarget is set
		Object connectTarget = getPropertyValue(
				RemoteConstants.ENDPOINT_CONNECTTARGET_ID, serviceReference,
				properties);
		ID connectTargetID = null;
		if (connectTarget != null) {
			// Then we get the host container connected ID
			ID connectedID = rsContainer.getContainer().getConnectedID();
			if (connectedID != null && !connectedID.equals(containerID))
				connectTargetID = connectedID;
		}
		
		ID[] idFilter = (ID[]) getPropertyValue(
				RemoteConstants.ENDPOINT_IDFILTER_IDS, serviceReference,
				properties);
		
		String rsFilter = (String) getPropertyValue(
				RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER,
				serviceReference, properties);
		
		// fill out all other properties
		String[] srKeys = serviceReference.getPropertyKeys();
		if (srKeys != null) {
			for(int i=0; i < srKeys.length; i++) {
				if (!PropertiesUtil.isStandardProperty(srKeys[i])) endpointDescriptionProperties.put(srKeys[i], serviceReference.getProperty(srKeys[i]));
			}
		}
		for(String key: properties.keySet()) {
			if (!PropertiesUtil.isStandardProperty(key)) endpointDescriptionProperties.put(key, properties.get(key));
		}
		
		return new EndpointDescription(endpointDescriptionProperties,
				containerID.getNamespace().getName(),
				rsRegistration.getID().getContainerRelativeID(), connectTargetID, idFilter,
				rsFilter);
	}

	protected ContainerTypeDescription getContainerTypeDescription(
			IContainer container) {
		if (container == null)
			return null;
		IContainerManager containerManager = Activator.getDefault()
				.getContainerManager();
		if (containerManager == null)
			return null;
		return containerManager.getContainerTypeDescription(container.getID());
	}

	protected String[] getSupportedConfigs(IContainer container) {
		ContainerTypeDescription ctd = getContainerTypeDescription(container);
		return (ctd == null) ? null : ctd.getSupportedConfigs();
	}

	protected String[] getSupportedIntents(IContainer container) {
		ContainerTypeDescription ctd = getContainerTypeDescription(container);
		return (ctd == null) ? null : ctd.getSupportedIntents();
	}

	protected void getNonStandardProperties(ServiceReference serviceReference, Map<String,Object> properties, Map result) {
		String[] srKeys = serviceReference.getPropertyKeys();
		if (srKeys != null) {
			for(int i=0; i < srKeys.length; i++) {
				if (!PropertiesUtil.isStandardProperty(srKeys[i])) result.put(srKeys[i], serviceReference.getProperty(srKeys[i]));
			}
		}
		for(String key: properties.keySet()) {
			if (!PropertiesUtil.isStandardProperty(key)) result.put(key, properties.get(key));
		}
	}
	
	protected Dictionary createRemoteServiceProperties(
			ServiceReference serviceReference, Map<String, Object> properties, IRemoteServiceContainer rsContainer) {
		Map<String, Object> result = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);
		getNonStandardProperties(serviceReference,properties,result);
		return PropertiesUtil.createDictionaryFromMap(result);
	}

	public void close() {
		closeConsumerContainerSelector();
		closeHostContainerSelector();
		this.context = null;
	}
}
