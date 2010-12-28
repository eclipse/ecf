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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
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
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceUnregisteredEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.remoteserviceadmin.ImportReference;
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
	private ServiceRegistration hostContainerSelectorRegistration;
	private ServiceTracker hostContainerSelectorTracker;
	private Object hostContainerSelectorTrackerLock = new Object();

	private ConsumerContainerSelector consumerContainerSelector;
	private ServiceRegistration consumerContainerSelectorRegistration;
	private ServiceTracker consumerContainerSelectorTracker;
	private Object consumerContainerSelectorTrackerLock = new Object();

	private ServiceTracker packageAdminTracker;
	private Object packageAdminTrackerLock = new Object();

	private PackageVersionComparator packageVersionComparator;
	private ServiceRegistration packageVersionComparatorRegistration;
	private Object packageVersionComparatorTrackerLock = new Object();
	private ServiceTracker packageVersionComparatorTracker;

	protected Collection<ExportRegistration> exportedRegistrations = new ArrayList<ExportRegistration>();

	protected Collection<ImportRegistration> importedRegistrations = new ArrayList<ImportRegistration>();

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
		hostContainerSelectorRegistration = getContext().registerService(
				IHostContainerSelector.class.getName(), hostContainerSelector,
				(Dictionary) properties);
		// create and register default consumer container selector. Since this
		// is registered with minimum service ranking
		// others can override this default simply by registering a
		// IConsumerContainerSelector implementer
		consumerContainerSelector = new ConsumerContainerSelector(
				consumerAutoCreateContainer);
		consumerContainerSelectorRegistration = getContext().registerService(
				IConsumerContainerSelector.class.getName(),
				consumerContainerSelector, (Dictionary) properties);

		// create and register a default package version comparator
		packageVersionComparator = new PackageVersionComparator();
		packageVersionComparatorRegistration = getContext().registerService(
				IPackageVersionComparator.class.getName(),
				packageVersionComparator, (Dictionary) properties);
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

	private void closePackageAdminTracker() {
		synchronized (packageAdminTrackerLock) {
			if (packageAdminTracker != null) {
				packageAdminTracker.close();
				packageAdminTracker = null;
			}
		}
	}

	private void closeConsumerContainerSelector() {
		synchronized (consumerContainerSelectorTrackerLock) {
			if (consumerContainerSelectorTracker != null) {
				consumerContainerSelectorTracker.close();
				consumerContainerSelectorTracker = null;
			}
		}
		if (consumerContainerSelectorRegistration != null) {
			consumerContainerSelectorRegistration.unregister();
			consumerContainerSelectorRegistration = null;
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
		if (hostContainerSelectorRegistration != null) {
			hostContainerSelectorRegistration.unregister();
			hostContainerSelectorRegistration = null;
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

	private Version getPackageVersion(Bundle registeringBundle,
			String packageName) {
		ExportedPackage[] exportedPackages = getPackageAdmin()
				.getExportedPackages(registeringBundle);
		if (exportedPackages == null)
			return null;
		for (int i = 0; i < exportedPackages.length; i++)
			if (exportedPackages[i].getName().equals(packageName))
				return exportedPackages[i].getVersion();
		return null;
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
		String[] interfaces = (String[]) PropertiesUtil.getPropertyValue(null,
				overridingProperties, org.osgi.framework.Constants.OBJECTCLASS);
		if (interfaces == null)
			interfaces = exportedInterfaces;
		endpointDescriptionProperties.put(
				org.osgi.framework.Constants.OBJECTCLASS, interfaces);

		// Service interface versions
		for (int i = 0; i < exportedInterfaces.length; i++) {
			String packageName = getPackageName(exportedInterfaces[i]);
			String packageVersionKey = org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_PACKAGE_VERSION_
					+ packageName;
			// If it's pre-set...by registration or by overridingProperties,
			// then use that value
			String packageVersion = (String) PropertiesUtil.getPropertyValue(
					serviceReference, overridingProperties, packageVersionKey);
			if (packageVersion == null) {
				Version version = getPackageVersion(
						serviceReference.getBundle(), packageName);
				if (version != null && !version.equals(Version.emptyVersion))
					packageVersion = version.toString();
				else
					logWarning("createExportEndpointDescription",
							"No or empty version specified for exported service interface="
									+ exportedInterfaces[i]);
			}
			// Only set the package version if we have a non-null value
			if (packageVersion != null)
				endpointDescriptionProperties.put(packageVersionKey,
						packageVersion);

		}

		// ENDPOINT_ID
		String endpointId = (String) PropertiesUtil
				.getPropertyValue(
						serviceReference,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID);
		if (endpointId == null)
			endpointId = containerID.getName();
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
						endpointId);

		// ENDPOINT_SERVICE_ID
		Long serviceId = (Long) PropertiesUtil.getPropertyValue(
				serviceReference, overridingProperties,
				org.osgi.framework.Constants.SERVICE_ID);
		endpointDescriptionProperties.put(
				org.osgi.framework.Constants.SERVICE_ID, serviceId);

		// ENDPOINT_FRAMEWORK_ID
		String frameworkId = (String) PropertiesUtil
				.getPropertyValue(
						serviceReference,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID);
		if (frameworkId == null)
			frameworkId = Activator.getDefault().getFrameworkUUID();
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
						frameworkId);

		// SERVICE_IMPORTED_CONFIGS...set to ECF constant
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
						RemoteConstants.ENDPOINT_SERVICE_IMPORTED_CONFIGS_VALUE);

		// SERVICE_INTENTS
		String[] intents = (String[]) PropertiesUtil
				.getPropertyValue(
						null,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS);
		if (intents == null)
			intents = serviceIntents;
		if (intents != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
							intents);

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
		// ID namespace
		String idNamespace = containerID.getNamespace().getName();
		endpointDescriptionProperties.put(RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE, idNamespace);
		
		// ENDPOINT_CONNECTTARGET_ID
		String connectTarget = (String) PropertiesUtil.getPropertyValue(
				serviceReference, overridingProperties,
				RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		if (connectTarget == null) {
			ID connectedID = rsContainer.getContainer().getConnectedID();
			if (connectedID != null && !connectedID.equals(containerID))
				connectTarget = connectedID.getName();
		}
		if (connectTarget != null)
			endpointDescriptionProperties.put(
					RemoteConstants.ENDPOINT_CONNECTTARGET_ID, connectTarget);

		// ENDPOINT_IDFILTER_IDS
		String[] idFilter = (String[]) PropertiesUtil.getPropertyValue(
				serviceReference, overridingProperties,
				RemoteConstants.ENDPOINT_IDFILTER_IDS);
		if (idFilter != null && idFilter.length > 0)
			endpointDescriptionProperties.put(
					RemoteConstants.ENDPOINT_IDFILTER_IDS, idFilter);

		// ENDPOINT_REMOTESERVICE_FILTER
		String rsFilter = (String) PropertiesUtil.getPropertyValue(
		serviceReference, overridingProperties,
				RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
		if (rsFilter != null)
			endpointDescriptionProperties.put(
					RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER, rsFilter);

		// copy remote registration properties
		PropertiesUtil.copyProperties(rsRegistration,
				endpointDescriptionProperties);
		// Remove ecf.robjectClass
		endpointDescriptionProperties
				.remove(org.eclipse.ecf.remoteservice.Constants.OBJECTCLASS);
		// finally create an ECF EndpointDescription
		return new EndpointDescription(serviceReference,
				endpointDescriptionProperties);
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
		return IDUtil.createID(endpointDescription);
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
			throw new NullPointerException(
					"getRemoteService returned null for selectedRsReference="
							+ selectedRsReference + ",rsContainerID="
							+ rsContainerID);

		Map proxyProperties = createProxyProperties(endpointDescription,
				rsContainer, selectedRsReference, rs);

		List<String> interfaces = endpointDescription.getInterfaces();

		IRemoteServiceListener rsListener = createRemoteServiceListener();

		ServiceRegistration proxyRegistration = getContext().registerService(
				(String[]) interfaces.toArray(new String[interfaces.size()]),
				createProxyServiceFactory(endpointDescription, rs),
				(Dictionary) PropertiesUtil
						.createDictionaryFromMap(proxyProperties));

		return new ImportRegistration(rsContainer, rsListener,
				selectedRsReference, endpointDescription, proxyRegistration);
	}

	protected ServiceFactory createProxyServiceFactory(
			EndpointDescription endpointDescription,
			IRemoteService remoteService) {
		return new ProxyServiceFactory(
				endpointDescription.getInterfaceVersions(), remoteService);
	}

	protected Collection<Class> loadServiceInterfacesViaBundle(Bundle bundle,
			String[] interfaces) {
		List<Class> result = new ArrayList<Class>();
		for (int i = 0; i < interfaces.length; i++) {
			try {
				result.add(bundle.loadClass(interfaces[i]));
			} catch (ClassNotFoundException e) {
				logError("loadInterfacesViaBundle", "interface="
						+ interfaces[i] + " cannot be loaded by bundle="
						+ bundle.getSymbolicName(), e);
				continue;
			} catch (IllegalStateException e) {
				logError("loadInterfacesViaBundle", "interface="
						+ interfaces[i]
						+ " cannot be loaded since bundle is in illegal state",
						e);
				continue;
			}
		}
		return result;
	}

	class ProxyServiceFactory implements ServiceFactory {
		private IRemoteService remoteService;
		private Map<String, Version> interfaceVersions;

		public ProxyServiceFactory(Map<String, Version> interfaceVersions,
				IRemoteService remoteService) {
			this.interfaceVersions = interfaceVersions;
			this.remoteService = remoteService;
		}

		public Object getService(Bundle bundle, ServiceRegistration registration) {
			return createProxy(bundle, registration.getReference(),
					remoteService, interfaceVersions);
		}

		public void ungetService(Bundle bundle,
				ServiceRegistration registration, Object service) {
			this.remoteService = null;
			this.interfaceVersions = null;
		}
	}

	protected Object createProxy(Bundle requestingBundle,
			ServiceReference serviceReference, IRemoteService remoteService,
			Map<String, Version> interfaceVersions) {
		// Get symbolicName once for possible use below
		String bundleSymbolicName = requestingBundle.getSymbolicName();
		// Get String[] via OBJECTCLASS constant property
		String[] serviceClassnames = (String[]) serviceReference
				.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
		// Load as many of the serviceInterface classes as possible
		Collection<Class> serviceInterfaceClasses = loadServiceInterfacesViaBundle(
				requestingBundle, serviceClassnames);
		// There has to be at least one serviceInterface that the bundle can
		// load...otherwise the service can't be accessed
		if (serviceInterfaceClasses.size() < 1)
			throw new RuntimeException(
					"ProxyServiceFactory cannot load any serviceInterfaces="
							+ serviceInterfaceClasses
							+ " for serviceReference=" + serviceReference
							+ " via bundle=" + bundleSymbolicName);

		// Now verify that the classes are of valid versions
		verifyServiceInterfaceVersionsForProxy(requestingBundle,
				serviceInterfaceClasses, interfaceVersions);

		// Now create/get class loader for proxy. This will typically
		// be an instance of ProxyClassLoader
		ClassLoader cl = getClassLoaderForProxy(requestingBundle,
				serviceInterfaceClasses);
		try {
			return remoteService.getProxy(cl, (Class[]) serviceInterfaceClasses
					.toArray(new Class[serviceInterfaceClasses.size()]));
		} catch (ECFException e) {
			throw new ServiceException(
					"ProxyServiceFactory cannot create proxy for bundle="
							+ bundleSymbolicName + " from serviceReference="
							+ serviceReference, e);
		}

	}

	protected ClassLoader getClassLoaderForProxy(Bundle bundle,
			Collection<Class> serviceInterfaceClasses) {
		return new ProxyClassLoader(bundle);
	}

	protected class ProxyClassLoader extends ClassLoader {
		private Bundle loadingBundle;

		public ProxyClassLoader(Bundle loadingBundle) {
			this.loadingBundle = loadingBundle;
		}

		public Class loadClass(String name) throws ClassNotFoundException {
			return loadingBundle.loadClass(name);
		}
	}

	private PackageAdmin getPackageAdmin() {
		synchronized (packageAdminTrackerLock) {
			if (packageAdminTracker == null) {
				packageAdminTracker = new ServiceTracker(getContext(),
						PackageAdmin.class.getName(), null);
				packageAdminTracker.open();
			}
		}
		return (PackageAdmin) packageAdminTracker.getService();
	}

	protected IPackageVersionComparator getPackageVersionComparator() {
		synchronized (packageVersionComparatorTrackerLock) {
			if (packageVersionComparatorTracker == null) {
				packageVersionComparatorTracker = new ServiceTracker(
						getContext(),
						IPackageVersionComparator.class.getName(), null);
				packageVersionComparatorTracker.open();
			}
		}
		return (IPackageVersionComparator) packageVersionComparatorTracker
				.getService();
	}

	private void closePackageVersionComparatorTracker() {
		synchronized (packageVersionComparatorTrackerLock) {
			if (packageVersionComparatorTracker != null) {
				packageVersionComparatorTracker.close();
				packageVersionComparatorTracker = null;
			}
		}
		if (packageVersionComparatorRegistration != null) {
			packageVersionComparatorRegistration.unregister();
			packageVersionComparatorRegistration = null;
		}
	}

	private ExportedPackage getExportedPackageForClass(
			PackageAdmin packageAdmin, Class clazz) {
		String packageName = getPackageName(clazz.getName());
		// Get all exported packages with given package name
		ExportedPackage[] exportedPackagesWithName = packageAdmin
				.getExportedPackages(packageName);
		// If none then we return null
		if (exportedPackagesWithName == null)
			return null;
		// Get the bundle for the previously loaded interface class
		Bundle classBundle = packageAdmin.getBundle(clazz);
		if (classBundle == null)
			return null;
		for (int i = 0; i < exportedPackagesWithName.length; i++) {
			Bundle packageBundle = exportedPackagesWithName[i]
					.getExportingBundle();
			if (packageBundle == null)
				continue;
			if (packageBundle.equals(classBundle))
				return exportedPackagesWithName[i];
		}
		return null;
	}

	private String getPackageName(String className) {
		int lastDotIndex = className.lastIndexOf(".");
		if (lastDotIndex == -1)
			return "";
		return className.substring(0, lastDotIndex);
	}

	protected void verifyServiceInterfaceVersionsForProxy(Bundle bundle,
			Collection<Class> classes, Map<String, Version> interfaceVersions) {
		IPackageVersionComparator packageVersionComparator = getPackageVersionComparator();
		if (packageVersionComparator == null) {
			logError(
					"verifyServiceInterfaceVersionsForProxy",
					"No package version comparator available, skipping package version comparison for service classes="
							+ classes);
			return;
		}
		// For all service interface classes
		for (Class clazz : classes) {
			String className = clazz.getName();
			String packageName = getPackageName(className);
			ExportedPackage exportedPackage = getExportedPackageForClass(
					getPackageAdmin(), clazz);
			if (exportedPackage == null)
				throw new NullPointerException(
						"No exported package found for class=" + className);
			// Now lookup version from specification
			Version remotePackageVersion = interfaceVersions.get(className);
			if (remotePackageVersion == null)
				throw new NullPointerException("Remote package=" + packageName
						+ " has no Version");
			Version localPackageVersion = exportedPackage.getVersion();
			if (localPackageVersion == null)
				throw new NullPointerException("Local package=" + packageName
						+ " has no Version");

			// Now do compare via package version comparator service
			packageVersionComparator.comparePackageVersions(packageName,
					remotePackageVersion, localPackageVersion);
		}
	}

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
			IRemoteServiceReference rsReference, IRemoteService remoteService) {

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
				.convertToStringPlusValue(endpointDescription.getIntents());
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

			IRemoteServiceContainerAdapter containerAdapter = rsContainer
					.getContainerAdapter();
			// Register remote service via ECF container adapter to create
			// remote service registration
			trace("doRegisterRemoteService",
					"registerRemoteService exportedInterfaces="
							+ Arrays.asList(exportedInterfaces)
							+ ",serviceReference=" + serviceReference
							+ ",remoteServiceProperties="
							+ remoteServiceProperties);
			if (containerAdapter instanceof IOSGiRemoteServiceContainerAdapter) {
				IOSGiRemoteServiceContainerAdapter osgiContainerAdapter = (IOSGiRemoteServiceContainerAdapter) containerAdapter;
				remoteRegistration = osgiContainerAdapter
						.registerRemoteService(
								exportedInterfaces,
								serviceReference,
								PropertiesUtil
										.createDictionaryFromMap(remoteServiceProperties));
			} else
				remoteRegistration = containerAdapter
						.registerRemoteService(
								exportedInterfaces,
								getContext().getService(serviceReference),
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
		closePackageVersionComparatorTracker();
		closeConsumerContainerSelector();
		closeHostContainerSelector();
		closePackageAdminTracker();
		synchronized (exportedRegistrations) {
			exportedRegistrations.clear();
		}
		synchronized (importedRegistrations) {
			importedRegistrations.clear();
		}
		this.context = null;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> exportService(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties) {
		trace("exportService", "serviceReference=" + serviceReference
				+ ",properties=" + overridingProperties);
		String[] exportedInterfaces = (String[]) PropertiesUtil
				.getPropertyValue(

						serviceReference,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES);
		String[] exportedConfigs = (String[]) PropertiesUtil
				.getPropertyValue(

						serviceReference,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS);
		String[] serviceIntents = (String[]) PropertiesUtil
				.getPropertyValue(

						serviceReference,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS);
		// Get a host container selector
		IHostContainerSelector hostContainerSelector = getHostContainerSelector();
		if (hostContainerSelector == null) {
			logError("handleServiceRegistering",
					"No hostContainerSelector available");
			return Collections.EMPTY_LIST;
		}
		// select ECF remote service containers that match given exported
		// interfaces, configs, and intents
		IRemoteServiceContainer[] rsContainers = hostContainerSelector
				.selectHostContainers(serviceReference, exportedInterfaces,
						exportedConfigs, serviceIntents);
		// If none found, log a warning and we're done
		if (rsContainers == null || rsContainers.length == 0) {
			logWarning(
					"handleServiceRegistered", "No remote service containers found for serviceReference=" //$NON-NLS-1$
							+ serviceReference
							+ ". Remote service NOT EXPORTED"); //$NON-NLS-1$
			return Collections.EMPTY_LIST;
		}
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> results = new ArrayList<org.osgi.service.remoteserviceadmin.ExportRegistration>();
		synchronized (exportedRegistrations) {
			for (int i = 0; i < rsContainers.length; i++) {
				ExportRegistration rsRegistration = null;
				try {
					rsRegistration = doExportService(serviceReference,
							overridingProperties, exportedInterfaces,
							serviceIntents, rsContainers[i]);
				} catch (Exception e) {
					logError("exportService",
							"Exception exporting serviceReference="
									+ serviceReference + " with properties="
									+ overridingProperties + " rsContainerID="
									+ rsContainers[i].getContainer().getID(), e);
					rsRegistration = new ExportRegistration(serviceReference, e);
				}
				results.add(rsRegistration);
				exportedRegistrations.add(rsRegistration);
			}
		}
		return results;
	}

	public org.osgi.service.remoteserviceadmin.ImportRegistration importService(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		trace("importService", "endpointDescription=" + endpointDescription);
		// First check to see whether it's one of ECF's endpoint descriptions
		if (endpointDescription instanceof EndpointDescription) {
			EndpointDescription ed = (EndpointDescription) endpointDescription;
			// Now get IConsumerContainerSelector, to select the ECF container
			// for the given endpointDescription
			IConsumerContainerSelector consumerContainerSelector = getConsumerContainerSelector();
			// If there is none, then we can go no further
			if (consumerContainerSelector == null) {
				logError("importService",
						"No consumerContainerSelector available");
				return null;
			}
			// Select the rsContainer to handle the endpoint description
			IRemoteServiceContainer rsContainer = consumerContainerSelector
					.selectConsumerContainer(ed);
			// If none found, log a warning and we're done
			if (rsContainer == null) {
				logWarning(
						"importService", "No remote service container selected for endpoint=" //$NON-NLS-1$
								+ endpointDescription
								+ ". Remote service NOT IMPORTED"); //$NON-NLS-1$
				return null;
			}
			// If one selected then import the service to create an import
			// registration
			ImportRegistration importRegistration = null;
			synchronized (importedRegistrations) {
				try {
					importRegistration = doImportService(ed, rsContainer);
				} catch (Exception e) {
					logError("importService",
							"Exception importing endpointDescription=" + ed
									+ rsContainer.getContainer().getID(), e);
					importRegistration = new ImportRegistration(rsContainer, e);
				} catch (NoClassDefFoundError e) {
					logError("importService",
							"NoClassDefFoundError importing endpointDescription="
									+ ed + rsContainer.getContainer().getID(),
							e);
					importRegistration = new ImportRegistration(rsContainer, e);
				}
				// If we actually created an importRegistration...whether
				// successful or not, add it to the
				// set of imported registrations
				if (importRegistration != null)
					importedRegistrations.add(importRegistration);
			}
			// Finally, return the importRegistration. It may be null or not.
			return importRegistration;
		} else {
			logWarning("importService", "endpointDescription="
					+ endpointDescription
					+ " is not ECFEndpointDescription...ignoring");
			return null;
		}
	}

	public Collection<ImportRegistration> unimportService(
			IRemoteServiceID remoteServiceID) {
		trace("unimport", "remoteServiceID=" + remoteServiceID);
		List<ImportRegistration> removedRegistrations = new ArrayList<ImportRegistration>();
		synchronized (importedRegistrations) {
			for (Iterator<ImportRegistration> i = importedRegistrations
					.iterator(); i.hasNext();) {
				ImportRegistration importRegistration = i.next();
				IRemoteServiceReference rsReference = importRegistration
						.getRemoteServiceReference();
				if (rsReference != null) {
					IRemoteServiceID regID = rsReference.getID();
					if (regID.equals(remoteServiceID)) {
						removedRegistrations.add(importRegistration);
						i.remove();
					}
				}
			}
		}
		// Now close all of them
		for (ImportRegistration removedReg : removedRegistrations)
			removedReg.close();
		return removedRegistrations;
	}

	protected class RemoteServiceListener implements IRemoteServiceListener {
		public void handleServiceEvent(IRemoteServiceEvent event) {
			if (event instanceof IRemoteServiceUnregisteredEvent) {
				Collection<ImportRegistration> removedRegistrations = unimportService(event
						.getReference().getID());
				trace("RemoteServiceListener.handleServiceEvent",
						"Removed importRegistrations=" + removedRegistrations
								+ " via event=" + event);
			}
		}
	}

	protected IRemoteServiceListener createRemoteServiceListener() {
		return new RemoteServiceListener();
	}

	public Collection<ImportRegistration> unimportService(
			EndpointDescription endpointDescription) {
		trace("unimportService", "endpointDescription=" + endpointDescription);
		List<ImportRegistration> removedRegistrations = new ArrayList<ImportRegistration>();
		synchronized (importedRegistrations) {
			for (Iterator<ImportRegistration> i = importedRegistrations
					.iterator(); i.hasNext();) {
				ImportRegistration reg = i.next();
				ImportReference importReference = null;
				try {
					importReference = reg.getImportReference();
					if (importReference != null) {
						org.osgi.service.remoteserviceadmin.EndpointDescription importedDescription = importReference
								.getImportedEndpoint();
						if (importedDescription != null
								&& importedDescription
										.equals(endpointDescription)) {
							removedRegistrations.add(reg);
							i.remove();
						}
					}
				} catch (IllegalStateException e) {
					// no export ref because ExportRegistration not
					// initialized properly
					logWarning("unimportService",
							"IllegalStateException accessing export reference for importRegistration="
									+ reg);
				}
			}
		}
		// Now close all of them
		for (ImportRegistration removedReg : removedRegistrations)
			removedReg.close();
		return removedRegistrations;
	}

	public Collection<org.eclipse.ecf.osgi.services.remoteserviceadmin.ExportRegistration> getExportedRegistrations() {
		Collection<org.eclipse.ecf.osgi.services.remoteserviceadmin.ExportRegistration> results = new ArrayList<org.eclipse.ecf.osgi.services.remoteserviceadmin.ExportRegistration>();
		synchronized (exportedRegistrations) {
			results.addAll(exportedRegistrations);
		}
		return results;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ExportReference> getExportedServices() {
		Collection<org.osgi.service.remoteserviceadmin.ExportReference> results = new ArrayList<org.osgi.service.remoteserviceadmin.ExportReference>();
		synchronized (exportedRegistrations) {
			for (ExportRegistration reg : exportedRegistrations) {
				results.add(reg.getExportReference());
			}
		}
		return results;
	}

	public Collection<org.eclipse.ecf.osgi.services.remoteserviceadmin.ImportRegistration> getImportedRegistrations() {
		Collection<org.eclipse.ecf.osgi.services.remoteserviceadmin.ImportRegistration> results = new ArrayList<org.eclipse.ecf.osgi.services.remoteserviceadmin.ImportRegistration>();
		synchronized (importedRegistrations) {
			results.addAll(importedRegistrations);
		}
		return results;
	}

	public Collection<org.osgi.service.remoteserviceadmin.ImportReference> getImportedEndpoints() {
		Collection<org.osgi.service.remoteserviceadmin.ImportReference> results = new ArrayList<org.osgi.service.remoteserviceadmin.ImportReference>();
		synchronized (importedRegistrations) {
			for (ImportRegistration reg : importedRegistrations) {
				results.add(reg.getImportReference());
			}
		}
		return results;
	}

	private ExportRegistration[] findExportRegistrations(
			ServiceReference serviceReference) {
		List<ExportRegistration> results = new ArrayList<ExportRegistration>();
		for (ExportRegistration exportReg : exportedRegistrations)
			if (exportReg.matchesServiceReference(serviceReference))
				results.add(exportReg);
		return results.toArray(new ExportRegistration[results.size()]);
	}

	public EndpointDescription[] unexportService(
			ServiceReference serviceReference) {
		List<EndpointDescription> endpointDescriptions = new ArrayList<EndpointDescription>();
		synchronized (exportedRegistrations) {
			ExportRegistration[] exportRegs = findExportRegistrations(serviceReference);
			if (exportRegs != null) {
				for (int i = 0; i < exportRegs.length; i++) {
					org.osgi.service.remoteserviceadmin.ExportReference exportRef = null;
					try {
						exportRef = exportRegs[i].getExportReference();
						if (exportRef != null) {
							org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription = exportRef
									.getExportedEndpoint();
							if (endpointDescription != null
									&& endpointDescription instanceof EndpointDescription) {
								endpointDescriptions
										.add((EndpointDescription) endpointDescription);
							}
							exportRegs[i].close();
							exportedRegistrations.remove(exportRegs[i]);
						}
					} catch (IllegalStateException e) {
						// no export ref because ExportRegistration not
						// initialized properly
						logWarning("unexportService",
								"IllegalStateException accessing export reference for exportRegistration="
										+ exportRegs[i]);
					}
				}
			}
		}
		return endpointDescriptions
				.toArray(new EndpointDescription[endpointDescriptions.size()]);
	}
}
