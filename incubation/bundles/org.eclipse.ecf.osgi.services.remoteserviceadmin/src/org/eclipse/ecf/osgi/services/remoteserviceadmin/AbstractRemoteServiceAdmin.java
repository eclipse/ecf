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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.IDUtil;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.eclipse.ecf.remoteservice.IOSGiRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
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
						hostContainerSelector, (Dictionary) properties);
		// create and register default consumer container selector. Since this
		// is registered with minimum service ranking
		// others can override this default simply by registering a
		// IConsumerContainerSelector implementer
		consumerContainerSelector = new ConsumerContainerSelector(
				consumerAutoCreateContainer);
		defaultConsumerContainerSelectorRegistration = getContext()
				.registerService(IConsumerContainerSelector.class.getName(),
						consumerContainerSelector, (Dictionary) properties);
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
		logError(methodName, message, (Throwable) null);
	}

	protected Object getService(ServiceReference serviceReference) {
		return context.getService(serviceReference);
	}

	protected Object getPropertyValue(String propertyName,
			ServiceReference serviceReference, Map<String, Object> properties) {
		Object result = properties.get(propertyName);
		return (result == null) ? serviceReference.getProperty(propertyName)
				: result;
	}

	protected EndpointDescription createExportEndpointDescription(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties,
			String[] exportedInterfaces, String[] serviceIntents,
			IRemoteServiceRegistration rsRegistration,
			IRemoteServiceContainer rsContainer) {

		IContainer container = rsContainer.getContainer();
		ID containerID = container.getID();

		Map<String, Object> endpointDescriptionProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);

		// OSGi properties
		// OBJECTCLASS
		endpointDescriptionProperties.put(
				org.osgi.framework.Constants.OBJECTCLASS, exportedInterfaces);
		// ENDPOINT_ID
		String endpointId = (String) getPropertyValue(
				org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
				serviceReference, overridingProperties);
		if (endpointId == null)
			endpointId = containerID.getName();
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
						endpointId);
		// ENDPOINT_SERVICE_ID
		Long serviceId = (Long) getPropertyValue(
				org.osgi.framework.Constants.SERVICE_ID, serviceReference,
				overridingProperties);
		endpointDescriptionProperties.put(
				org.osgi.framework.Constants.SERVICE_ID, serviceId);
		// ENDPOINT_FRAMEWORK_ID
		String frameworkId = Activator.getDefault().getFrameworkUUID();
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
						frameworkId);
		// SERVICE_IMPORTED_CONFIGS...set to ECF constant
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
						RemoteConstants.ENDPOINT_SERVICE_IMPORTED_CONFIGS_VALUE);
		// SERVICE_INTENTS
		if (serviceIntents != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
							serviceIntents);
		// REMOTE_INTENTS_SUPPORTED
		String[] remoteIntentsSupported = getSupportedIntents(container);
		if (remoteIntentsSupported != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED,
							remoteIntentsSupported);
		// REMOTE_CONFIGS_SUPPORTED
		String[] remoteConfigsSupported = getSupportedConfigs(container);
		if (remoteConfigsSupported != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
							remoteConfigsSupported);

		// ECF properties
		// ENDPOINT_CONNECTTARGET_ID
		Object connectTarget = getPropertyValue(
				RemoteConstants.ENDPOINT_CONNECTTARGET_ID, serviceReference,
				overridingProperties);
		ID connectTargetID = null;
		if (connectTarget != null) {
			// Then we get the host container connected ID
			ID connectedID = rsContainer.getContainer().getConnectedID();
			if (connectedID != null && !connectedID.equals(containerID))
				connectTargetID = connectedID;
		}
		// ENDPOINT_IDFILTER_IDS
		ID[] idFilter = (ID[]) getPropertyValue(
				RemoteConstants.ENDPOINT_IDFILTER_IDS, serviceReference,
				overridingProperties);
		// ENDPOINT_REMOTESERVICE_FILTER
		String rsFilter = (String) getPropertyValue(
				RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER,
				serviceReference, overridingProperties);

		// copy remote registration properties
		PropertiesUtil.copyProperties(rsRegistration,
				endpointDescriptionProperties);
		// Remove ecf.robjectClass
		endpointDescriptionProperties
				.remove(org.eclipse.ecf.remoteservice.Constants.OBJECTCLASS);
		// finally create an ECF EndpointDescription
		return new EndpointDescription(serviceReference,
				endpointDescriptionProperties, containerID.getNamespace()
						.getName(), connectTargetID, idFilter, rsFilter);
	}

	protected Map<String, Object> copyNonReservedProperties(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties, Map<String, Object> target) {
		// copy all other properties...from service reference
		PropertiesUtil.copyNonReservedProperties(serviceReference, target);
		// And override with overridingProperties
		PropertiesUtil.copyNonReservedProperties(overridingProperties, target);
		return target;
	}

	protected Map<String, Object> copyNonReservedProperties(
			IRemoteServiceReference rsReference, Map<String, Object> target) {
		return PropertiesUtil.copyNonReservedProperties(rsReference, target);
	}

	protected ContainerTypeDescription getContainerTypeDescription(
			IContainer container) {
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

	protected String[] getImportedConfigs(IContainer container,
			String[] exporterSupportedConfigs) {
		ContainerTypeDescription ctd = getContainerTypeDescription(container);
		return (ctd == null) ? null : ctd
				.getImportedConfigs(exporterSupportedConfigs);
	}

	protected String[] getSupportedIntents(IContainer container) {
		ContainerTypeDescription ctd = getContainerTypeDescription(container);
		return (ctd == null) ? null : ctd.getSupportedIntents();
	}

	protected Collection<String> getInterfaces(
			EndpointDescription endpointDescription) {
		return endpointDescription.getInterfaces();
	}

	protected ID getEndpointID(EndpointDescription endpointDescription) {
		return IDUtil.createContainerID(endpointDescription);
	}

	protected ID getConnectTargetID(EndpointDescription endpointDescription) {
		return endpointDescription.getConnectTargetID();
	}

	protected ID[] getIDFilter(EndpointDescription endpointDescription,
			ID endpointID) {
		ID[] idFilter = endpointDescription.getIDFilter();
		// If it is null,
		return (idFilter == null) ? new ID[] { endpointID } : idFilter;
	}

	protected String getRemoteServiceFilter(
			EndpointDescription endpointDescription) {
		long rsId = endpointDescription.getRemoteServiceId();
		if (rsId == 0) {
			// It's not known...so we just return the 'raw' remote service
			// filter
			return endpointDescription.getRemoteServiceFilter();
		} else {
			String edRsFilter = endpointDescription.getRemoteServiceFilter();
			// It's a real remote service id...so we return
			StringBuffer result = new StringBuffer("(&(") //$NON-NLS-1$
					.append(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID)
					.append("=").append(rsId).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			if (edRsFilter != null)
				result.append(edRsFilter);
			result.append(")"); //$NON-NLS-1$
			return result.toString();
		}
	}

	protected ImportRegistration handleNonOSGiService(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer) {
		// With non-OSGi service id (service id=0), we log a warning and return
		// null;
		logWarning("doImportService",
				"OSGi remote service id is 0 for endpointDescription="
						+ endpointDescription);
		return null;
	}

	protected IRemoteServiceRegistration doRegisterRemoteService(
			IRemoteServiceContainerAdapter containerAdapter,
			String[] exportedInterfaces, ServiceReference serviceReference,
			Dictionary remoteServiceProperties) {
		trace("doRegisterRemoteService",
				"registerRemoteService exportedInterfaces="
						+ Arrays.asList(exportedInterfaces)
						+ ",serviceReference=" + serviceReference
						+ ",remoteServiceProperties=" + remoteServiceProperties);
		if (containerAdapter instanceof IOSGiRemoteServiceContainerAdapter) {
			IOSGiRemoteServiceContainerAdapter osgiContainerAdapter = (IOSGiRemoteServiceContainerAdapter) containerAdapter;
			return osgiContainerAdapter.registerRemoteService(
					exportedInterfaces, serviceReference,
					remoteServiceProperties);

		} else
			return containerAdapter.registerRemoteService(exportedInterfaces,
					getService(serviceReference), remoteServiceProperties);

	}

	protected ImportRegistration createAndRegisterProxy(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer,
			IRemoteServiceReference selectedRsReference) throws Exception {
		IRemoteServiceContainerAdapter containerAdapter = rsContainer
				.getContainerAdapter();
		ID rsContainerID = rsContainer.getContainer().getID();
		// First get IRemoteService for selectedRsReference
		IRemoteService rs = containerAdapter
				.getRemoteService(selectedRsReference);

		if (rs == null)
			throw new ECFException(
					"getRemoteService returned null for selectedRsReference="
							+ selectedRsReference + ",rsContainerID="
							+ rsContainerID);
		// Now get proxy from IRemoteService
		Object proxy = rs.getProxy();
		if (proxy == null)
			throw new ECFException("getProxy returned null for rsReference="
					+ selectedRsReference + ",rsContainerID=" + rsContainerID);

		Map proxyProperties = createProxyProperties(endpointDescription,
				rsContainer, selectedRsReference, rs, proxy);

		List<String> interfaces = endpointDescription.getInterfaces();

		ServiceRegistration proxyRegistration = getContext().registerService(
				(String[]) interfaces.toArray(new String[interfaces.size()]),
				proxy,
				(Dictionary) PropertiesUtil
						.createDictionaryFromMap(proxyProperties));

		IRemoteServiceListener rsListener = createRemoteServiceListener();
		
		return new ImportRegistration(rsContainer,rsListener,selectedRsReference,
				endpointDescription, proxyRegistration);
	}

	protected abstract IRemoteServiceListener createRemoteServiceListener();
	
	protected IRemoteServiceReference selectRemoteServiceReference(
			Collection<IRemoteServiceReference> rsRefs, ID targetID,
			ID[] idFilter, Collection<String> interfaces, String rsFilter,
			IRemoteServiceContainer rsContainer) {
		if (rsRefs.size() == 0)
			return null;
		if (rsRefs.size() > 1) {
			logWarning("selectRemoteServiceReference", "rsRefs=" + rsRefs
					+ ",targetID=" + targetID + ",idFilter=" + idFilter
					+ ",interfaces=" + interfaces + ",rsFilter=" + rsFilter
					+ ",rsContainer=" + rsContainer.getContainer().getID()
					+ " has " + rsRefs.size()
					+ " values.  Selecting the first element");
		}
		return rsRefs.iterator().next();
	}

	protected Map createProxyProperties(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer,
			IRemoteServiceReference rsReference, IRemoteService remoteService,
			Object proxy) {

		Map resultProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);
		copyNonReservedProperties(rsReference, resultProperties);
		// remove OBJECTCLASS
		resultProperties
				.remove(org.eclipse.ecf.remoteservice.Constants.OBJECTCLASS);
		// remove remote service id
		resultProperties
				.remove(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		// Set intents if there are intents
		Object intentsValue = PropertiesUtil
				.getStringPlusValue(endpointDescription.getIntents());
		if (intentsValue != null)
			resultProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
							intentsValue);
		// Set service.imported to IRemoteService
		resultProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED,
						remoteService);
		String[] exporterSupportedConfigs = (String[]) endpointDescription
				.getProperties()
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
		String[] importedConfigs = getImportedConfigs(
				rsContainer.getContainer(), exporterSupportedConfigs);
		// Set service.imported.configs
		resultProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
						importedConfigs);
		return resultProperties;
	}

	protected ExportRegistration doExportService(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties,
			String[] exportedInterfaces, String[] serviceIntents,
			IRemoteServiceContainer rsContainer) throws Exception {
		trace("doExportService",
				"serviceReference="
						+ serviceReference
						+ ",overridingProperties="
						+ overridingProperties
						+ ",exportedInterfaces="
						+ Arrays.asList(exportedInterfaces)
						+ ",serviceIntents="
						+ ((serviceIntents == null) ? "null" : Arrays.asList(
								serviceIntents).toString()) + ",rsContainerID="
						+ rsContainer.getContainer().getID());
		IRemoteServiceRegistration remoteRegistration = null;
		try {
			// Create remote service properties
			Map remoteServiceProperties = copyNonReservedProperties(
					serviceReference, overridingProperties,
					new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER));
			// Register remote service via ECF container adapter to create
			// remote service registration
			remoteRegistration = doRegisterRemoteService(
					rsContainer.getContainerAdapter(), exportedInterfaces,
					serviceReference,
					PropertiesUtil
							.createDictionaryFromMap(remoteServiceProperties));
			// Create EndpointDescription from remoteRegistration
			EndpointDescription endpointDescription = createExportEndpointDescription(
					serviceReference, overridingProperties, exportedInterfaces,
					serviceIntents, remoteRegistration, rsContainer);
			// Create ExportRegistration
			return createExportRegistration(remoteRegistration,
					serviceReference, endpointDescription);
		} catch (Exception e) {
			// If we actually created an IRemoteRegistration then unregister
			if (remoteRegistration != null)
				remoteRegistration.unregister();
			// rethrow
			throw e;
		}
	}

	protected ExportRegistration createExportRegistration(
			IRemoteServiceRegistration remoteRegistration,
			ServiceReference serviceReference,
			EndpointDescription endpointDescription) {
		return new ExportRegistration(remoteRegistration, serviceReference,
				endpointDescription);
	}

	protected ImportRegistration doImportService(
			EndpointDescription endpointDescription,
			IRemoteServiceContainer rsContainer) throws Exception {
		trace("doImportService", "endpointDescription=" + endpointDescription
				+ ",rsContainerID=" + rsContainer.getContainer().getID());
		long osgiServiceId = endpointDescription.getServiceId();
		if (osgiServiceId == 0)
			return handleNonOSGiService(endpointDescription, rsContainer);
		// Get interfaces from endpoint description
		Collection<String> interfaces = getInterfaces(endpointDescription);
		Assert.isNotNull(interfaces);
		Assert.isTrue(interfaces.size() > 0);
		// Get ECF endpoint ID...if this throws IDCreateException (because the
		// local system does not have
		// namespace for creating ID, or no namespace is present in
		// endpointDescription or endpoint id,
		// then it will be caught by the caller
		ID endpointID = getEndpointID(endpointDescription);
		Assert.isNotNull(endpointID);
		// Get connect target ID. May be null
		ID targetID = getConnectTargetID(endpointDescription);
		if (targetID == null)
			targetID = endpointID;
		// Get idFilter...also may be null
		ID[] idFilter = getIDFilter(endpointDescription, endpointID);
		// Get remote service filter
		String rsFilter = getRemoteServiceFilter(endpointDescription);
		// IRemoteServiceReferences from query
		Collection<IRemoteServiceReference> rsRefs = new ArrayList<IRemoteServiceReference>();
		// Get IRemoteServiceContainerAdapter
		IRemoteServiceContainerAdapter containerAdapter = rsContainer
				.getContainerAdapter();
		// rsContainerID
		ID rsContainerID = rsContainer.getContainer().getID();
		// For all given interfaces
		for (String intf : interfaces) {
			// Get/lookup remote service references
			IRemoteServiceReference[] refs = containerAdapter
					.getRemoteServiceReferences(targetID, idFilter, intf,
							rsFilter);
			if (refs == null) {
				logWarning("doImportService",
						"getRemoteServiceReferences return null for targetID="
								+ targetID + ",idFilter=" + idFilter + ",intf="
								+ intf + ",rsFilter=" + rsFilter
								+ " on rsContainerID=" + rsContainerID);
				continue;
			}
			for (int i = 0; i < refs.length; i++)
				rsRefs.add(refs[i]);
		}
		IRemoteServiceReference selectedRsReference = selectRemoteServiceReference(
				rsRefs, targetID, idFilter, interfaces, rsFilter, rsContainer);

		if (selectedRsReference == null) {
			logWarning("doImportService",
					"selectRemoteServiceReference returned null for rsRefs="
							+ rsRefs + ",targetID=" + targetID + ",idFilter="
							+ idFilter + ",interfaces=" + interfaces
							+ ",rsFilter=" + rsFilter + ",rsContainerID="
							+ rsContainerID);
			return null;
		}

		return createAndRegisterProxy(endpointDescription, rsContainer,
				selectedRsReference);
	}

	protected IRemoteServiceReference[] doGetRemoteServiceReferences(
			IRemoteServiceContainerAdapter containerAdapter, ID targetID,
			ID[] idFilter, String intf, String rsFilter)
			throws ContainerConnectException, InvalidSyntaxException {
		trace("doGetRemoteServiceReferences",
				"getRemoteServiceReferences targetID=" + targetID
						+ ",idFilter=" + Arrays.asList(idFilter) + ",intf="
						+ intf + ",rsFilter=" + rsFilter);
		return containerAdapter.getRemoteServiceReferences(targetID, idFilter,
				intf, rsFilter);
	}

	public void close() {
		closeConsumerContainerSelector();
		closeHostContainerSelector();
		this.context = null;
	}
}
