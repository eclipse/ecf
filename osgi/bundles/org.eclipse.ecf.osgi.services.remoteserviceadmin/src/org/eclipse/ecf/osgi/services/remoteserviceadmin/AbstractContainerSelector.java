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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.provider.ContainerIntentException;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.IDUtil;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * Abstract superclass for both host and consumer container selectors...i.e.
 * implementers of {@link IHostContainerSelector} or
 * {@link IConsumerContainerSelector}.
 * 
 */
public abstract class AbstractContainerSelector {

	public static final IRemoteServiceContainer[] EMPTY_REMOTE_SERVICE_CONTAINER_ARRAY = new IRemoteServiceContainer[] {};

	protected IContainerManager getContainerManager() {
		return Activator.getDefault().getContainerManager();
	}

	protected IContainerFactory getContainerFactory() {
		return getContainerManager().getContainerFactory();
	}

	protected ContainerTypeDescription[] getContainerTypeDescriptions() {
		return (ContainerTypeDescription[]) getContainerFactory()
				.getDescriptions().toArray(new ContainerTypeDescription[] {});
	}

	protected IContainer[] getContainers() {
		return getContainerManager().getAllContainers();
	}

	protected IRemoteServiceContainerAdapter hasRemoteServiceContainerAdapter(
			IContainer container) {
		return (IRemoteServiceContainerAdapter) container
				.getAdapter(IRemoteServiceContainerAdapter.class);
	}

	protected ContainerTypeDescription getContainerTypeDescription(
			IContainer container) {
		return getContainerManager().getContainerTypeDescription(
				container.getID());
	}

	protected IRemoteServiceContainer[] getRemoteServiceContainers(
			IContainer[] containers) {
		List results = new ArrayList();
		for (int i = 0; i < containers.length; i++) {
			IRemoteServiceContainerAdapter adapter = hasRemoteServiceContainerAdapter(containers[i]);
			if (adapter != null)
				results.add(new RemoteServiceContainer(containers[i], adapter));
		}
		return (IRemoteServiceContainer[]) results
				.toArray(new IRemoteServiceContainer[] {});
	}

	protected boolean includeContainerWithConnectNamespace(
			IContainer container, String connectNamespaceName) {
		if (connectNamespaceName != null) {
			Namespace namespace = container.getConnectNamespace();
			if (namespace != null
					&& namespace.getName().equals(connectNamespaceName))
				return true;
		}
		return false;
	}

	protected void connectContainer(IContainer container, ID connectTargetID,
			IConnectContext connectContext) throws ContainerConnectException {
		trace("connectContainer", "Connecting container=" + container.getID() //$NON-NLS-1$ //$NON-NLS-2$
				+ " to connectTargetID=" + connectTargetID); //$NON-NLS-1$
		container.connect(connectTargetID, connectContext);
	}

	protected String[] getSupportedConfigTypes(
			ContainerTypeDescription containerTypeDescription) {
		String[] supportedConfigs = containerTypeDescription
				.getSupportedConfigs();
		return (supportedConfigs == null) ? new String[0] : supportedConfigs;
	}

	protected String[] getSupportedIntents(
			ContainerTypeDescription containerTypeDescription) {
		String[] supportedIntents = containerTypeDescription
				.getSupportedIntents();
		return (supportedIntents == null) ? new String[0] : supportedIntents;
	}

	/**
	 * @param serviceReference serviceReference
	 * @param properties overriding properties
	 * @param containerTypeDescription containerTypeDescription
	 * @return IContainer created container.  Should not be <code>null</code>
	 * @throws SelectContainerException thrown if some create or configure failure
	 * @since 2.0
	 */
	protected IContainer createContainer(ServiceReference serviceReference,
			Map<String, Object> properties,
			ContainerTypeDescription containerTypeDescription)
			throws SelectContainerException {
		
		return createContainer(serviceReference, properties, containerTypeDescription, null);
	}
	
	/**
	 * @param serviceReference serviceReference
	 * @param properties overriding properties
	 * @param containerTypeDescription containerTypeDescription
	 * @param intents intents
	 * @return IContainer created container.  May be null if instance cannot be created (e.g. because of intent requirements)
	 * @throws SelectContainerException thrown if some create or configure failure
	 * @since 4.6
	 */
	protected IContainer createContainer(ServiceReference serviceReference,
			Map<String, Object> properties,
			ContainerTypeDescription containerTypeDescription, String[] intents)
			throws SelectContainerException {

		IContainerFactory containerFactory = getContainerFactory();

		final Object containerFactoryArguments = getContainerFactoryArguments(
				serviceReference, properties, containerTypeDescription, intents);

		try {
			if (containerFactoryArguments instanceof String) {
				return containerFactory.createContainer(
						containerTypeDescription,
						(String) containerFactoryArguments);
			} else if (containerFactoryArguments instanceof ID) {
				return containerFactory.createContainer(
						containerTypeDescription,
						(ID) containerFactoryArguments);
			} else if (containerFactoryArguments instanceof Object[]) {
				return containerFactory.createContainer(
						containerTypeDescription,
						(Object[]) containerFactoryArguments);
			} else if (containerFactoryArguments instanceof Map) {
				return containerFactory.createContainer(
						containerTypeDescription,
						(Map) containerFactoryArguments);
			}
			return containerFactory.createContainer(containerTypeDescription);
		} catch (ContainerCreateException e) {
			if (e instanceof ContainerIntentException) {
				LogUtility.logError("createContainer", DebugOptions.CONTAINER_SELECTOR, this.getClass(), "Container creation does not satisfy required intent="+((ContainerIntentException) e).getIntentName(), e); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			} else
				throw new SelectContainerException(
					"Exception creating or configuring container", e, //$NON-NLS-1$
					containerTypeDescription);
		}
	}


	/**
	 * @param serviceReference serviceReference
	 * @param properties overriding properties
	 * @param containerTypeDescription containerTypeDescription
	 * @return Object container factory arguments to use
	 * @since 2.0
	 */
	protected Object getContainerFactoryArguments(
			ServiceReference serviceReference, Map<String, Object> properties,
			ContainerTypeDescription containerTypeDescription) {
		return getContainerFactoryArguments(serviceReference, properties, containerTypeDescription, null);
	}

	/**
	 * @param serviceReference serviceReference
	 * @param properties overriding properties
	 * @param containerTypeDescription containerTypeDescription
	 * @param intents intents
	 * @return Object container factory arguments to use
	 * @since 4.6
	 */
	protected Object getContainerFactoryArguments(
			ServiceReference serviceReference, Map<String, Object> properties,
			ContainerTypeDescription containerTypeDescription, String[] intents) {
		// If the RemoteConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS is set
		// than use it.
		final Object containerFactoryArguments = properties
				.get(RemoteConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS);
		if (containerFactoryArguments != null)
			return containerFactoryArguments;
		// Else we return Map as is required for RSA container creation
		Map<String, Object> results = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		// Put the service intents (e.g. osgi.private) into the results map
		if (intents != null)
			results.put(Constants.SERVICE_INTENTS, intents);
		// This iterates through the service Reference properties...if the property starts with
		// any of the service intent names (e.g. osgi.basic.timeout) then the property is
		// added to the results map
		if (intents != null)
			for (String propName : serviceReference.getPropertyKeys()) {
				Object value = null;
				for (String intent : intents) {
					if (propName.startsWith(intent + ".")) //$NON-NLS-1$
						value = serviceReference.getProperty(propName);
				}
				if (value != null)
					results.put(propName, value);
			}
		// This iterates through the properties map...if the property starts with
		// any of the service intent names (e.g. osgi.basic.timeout) then the property is
		// added to the results map
		if (intents != null)
			for (String propName : properties.keySet()) {
				Object value = null;
				for (String intent : intents) {
					if (propName.startsWith(intent + ".")) //$NON-NLS-1$
						value = properties.get(propName);
				}
				if (value != null)
					results.put(propName, value);
			}
		// Add any config properties
		String exportedConfig = containerTypeDescription.getName();
		// If not, then we look through the properties that start with
		// <containerTypeDescription.name>.
		Map<String, Object> configProperties = PropertiesUtil.getConfigProperties(exportedConfig, properties);
		results = PropertiesUtil.mergeProperties(results, configProperties);
		
		if (results.size() == 0)
			return null;
		
		return results;
	}

	protected ID createTargetID(IContainer container, String target) {
		return IDUtil.createID(container.getConnectNamespace(), target);
	}

	protected void disconnectContainer(IContainer container) {
		container.disconnect();
	}

	/**
	 * @param serviceReference serviceReference
	 * @param properties overriding properties
	 * @param container the container to create connect context for
	 * @param context a possible connect context]
	 * @return IConnectContext a connect context or null
	 * @since 2.0
	 */
	protected IConnectContext createConnectContext(
			ServiceReference serviceReference, Map<String, Object> properties,
			IContainer container, Object context) {
		if (context instanceof IConnectContext)
			return (IConnectContext) context;
		return null;
	}

	protected void logException(String string, Exception e) {
		Activator.getDefault().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, string, e));
	}

	protected void trace(String methodName, String message) {
		LogUtility.trace(methodName, DebugOptions.CONTAINER_SELECTOR,
				this.getClass(), message);
	}

	protected void traceException(String methodName, String message, Throwable t) {
		LogUtility.traceException(methodName, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), message, t);
	}

	protected void logError(String methodName, String message, Throwable t) {
		LogUtility.logError(methodName, DebugOptions.CONTAINER_SELECTOR,
				this.getClass(), message, t);
	}

	protected void logError(String methodName, String message) {
		LogUtility.logError(methodName, DebugOptions.CONTAINER_SELECTOR,
				this.getClass(), message);
	}

	protected void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.CONTAINER_SELECTOR,
				this.getClass(), message);
	}

	protected boolean matchConnectNamespace(IContainer container,
			ID endpointID, ID connectTargetID) {
		if (connectTargetID != null) {
			return connectTargetID.getNamespace().getName()
					.equals(container.getConnectNamespace().getName());
		}
		if (endpointID == null)
			return false;
		return endpointID.getNamespace().getName()
				.equals(container.getConnectNamespace().getName());
	}

	protected boolean matchContainerID(IContainer container, ID endpointID) {
		if (endpointID == null)
			return false;
		return endpointID.equals(container.getID());
	}

}
