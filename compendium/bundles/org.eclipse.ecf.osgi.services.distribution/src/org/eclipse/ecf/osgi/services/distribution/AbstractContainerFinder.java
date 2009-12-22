/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.distribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.osgi.services.distribution.Activator;
import org.eclipse.ecf.internal.osgi.services.distribution.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.distribution.LogUtility;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;
import org.osgi.framework.ServiceReference;

/**
 * Abstract superclass for IHostContainerFinders and IProxyContainerFinders.
 * 
 */
public abstract class AbstractContainerFinder {

	public static final IRemoteServiceContainer[] EMPTY_REMOTE_SERVICE_CONTAINER_ARRAY = new IRemoteServiceContainer[] {};

	protected IIDFactory getIDFactory() {
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		return activator.getIDFactory();
	}

	protected IContainerManager getContainerManager() {
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		return activator.getContainerManager();
	}

	protected IContainerFactory getContainerFactory() {
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		IContainerManager containerManager = getContainerManager();
		if (containerManager == null)
			return null;
		return containerManager.getContainerFactory();
	}

	protected ContainerTypeDescription[] getContainerTypeDescriptions() {
		IContainerFactory containerFactory = getContainerFactory();
		if (containerFactory == null)
			return null;
		return (ContainerTypeDescription[]) containerFactory.getDescriptions()
				.toArray(new ContainerTypeDescription[] {});
	}

	protected IContainer[] getContainers() {
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		IContainerManager containerManager = activator.getContainerManager();
		if (containerManager == null)
			return null;
		return containerManager.getAllContainers();
	}

	protected IRemoteServiceContainerAdapter hasRemoteServiceContainerAdapter(
			IContainer container) {
		return (IRemoteServiceContainerAdapter) container
				.getAdapter(IRemoteServiceContainerAdapter.class);
	}

	protected ContainerTypeDescription getContainerTypeDescription(
			IContainer container) {
		IContainerManager containerManager = getContainerManager();
		if (containerManager == null)
			return null;
		return containerManager.getContainerTypeDescription(container.getID());
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
		trace("connectContainer", "Connecting container=" + container.getID()
				+ " to connectTargetID=" + connectTargetID);
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

	protected boolean matchHostSupportedIntents(
			String[] serviceRequiredIntents,
			ContainerTypeDescription containerTypeDescription) {
		// If there are no required intents then we have a match
		if (serviceRequiredIntents == null)
			return true;

		String[] supportedIntents = getSupportedIntents(containerTypeDescription);

		if (supportedIntents == null)
			return false;

		List supportedIntentsList = Arrays.asList(supportedIntents);

		boolean result = true;
		for (int i = 0; i < serviceRequiredIntents.length; i++)
			result = result
					&& supportedIntentsList.contains(serviceRequiredIntents[i]);

		return result;
	}

	protected IContainer createContainer(ServiceReference serviceReference,
			ContainerTypeDescription containerTypeDescription)
			throws ContainerCreateException {

		IContainerFactory containerFactory = getContainerFactory();
		if (containerFactory == null)
			throw new ContainerCreateException(
					"container factory must not be null");

		Object containerFactoryArguments = serviceReference
				.getProperty(IDistributionConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGUMENTS);
		if (containerFactoryArguments instanceof String) {
			return containerFactory.createContainer(containerTypeDescription,
					(String) containerFactoryArguments);
		} else if (containerFactoryArguments instanceof ID) {
			return containerFactory.createContainer(containerTypeDescription,
					(ID) containerFactoryArguments);
		} else if (containerFactoryArguments instanceof Object[]) {
			return containerFactory.createContainer(containerTypeDescription,
					(Object[]) containerFactoryArguments);
		}
		return containerFactory.createContainer(containerTypeDescription);
	}

	protected ID createTargetID(IContainer container, Object target) {
		ID targetID = null;
		if (target instanceof String) {
			targetID = getIDFactory().createID(container.getConnectNamespace(),
					(String) target);
		} else if (target instanceof Object[]) {
			targetID = getIDFactory().createID(container.getConnectNamespace(),
					(Object[]) target);
		}
		return targetID;
	}

	protected void disconnectContainer(IContainer container) {
		container.disconnect();
	}

	protected IConnectContext createConnectContext(
			ServiceReference serviceReference, IContainer container,
			Object context) {
		if (context instanceof IConnectContext)
			return (IConnectContext) context;
		return null;
	}

	protected void logException(String string, Exception e) {
		Activator.getDefault().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, string, e));
	}

	protected void trace(String methodName, String message) {
		LogUtility.trace(methodName, DebugOptions.CONTAINERFINDER, this
				.getClass(), message);
	}

	protected void traceException(String methodName, String message, Throwable t) {
		LogUtility.traceException(methodName, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), message, t);
	}

	protected void logError(String methodName, String message, Throwable t) {
		LogUtility.logError(methodName, DebugOptions.CONTAINERFINDER, this
				.getClass(), message, t);
	}

	protected void logError(String methodName, String message) {
		LogUtility.logError(methodName, DebugOptions.CONTAINERFINDER, this
				.getClass(), message);
	}

	protected void logWarning(String methodName, String message) {
		LogUtility.logWarning(methodName, DebugOptions.CONTAINERFINDER, this
				.getClass(), message);
	}

	protected boolean matchConnectNamespace(IContainer container, ID endpointID) {
		if (endpointID == null)
			return false;
		return endpointID.getNamespace().getName().equals(
				container.getConnectNamespace().getName());
	}

	protected boolean matchContainerID(IContainer container, ID endpointID) {
		if (endpointID == null)
			return false;
		return endpointID.equals(container.getID());
	}

	protected Collection findExistingProxyContainers(ID endpointID,
			String[] remoteSupportedConfigs) {

		List results = new ArrayList();
		// Get all containers available
		IContainer[] containers = getContainers();
		// If none then return null
		if (containers == null)
			return results;

		for (int i = 0; i < containers.length; i++) {
			// Do *not* include containers with same ID as endpoint ID
			if (matchContainerID(containers[i], endpointID))
				continue;

			IRemoteServiceContainerAdapter adapter = hasRemoteServiceContainerAdapter(containers[i]);
			// Container must have adapter
			if (adapter != null
			// And it must match the connect namespace
					&& matchConnectNamespace(containers[i], endpointID)
					// and it must match the configs
					&& matchProxySupportedConfigs(containers[i],
							remoteSupportedConfigs)) {
				trace("findExistingProxyContainers",
						"MATCH of existing remote service container id="
								+ containers[i].getID()
								+ " endpointID="
								+ endpointID
								+ " remoteSupportedConfigs="
								+ ((remoteSupportedConfigs == null) ? "[]"
										: Arrays.asList(remoteSupportedConfigs)
												.toString()));
				results.add(new RemoteServiceContainer(containers[i], adapter));
			} else {
				trace("findExistingProxyContainers",
						"No match of existing remote service container id="
								+ containers[i].getID()
								+ " endpointID="
								+ endpointID
								+ " remoteSupportedConfigs="
								+ ((remoteSupportedConfigs == null) ? "[]"
										: Arrays.asList(remoteSupportedConfigs)
												.toString()));
			}
		}
		return results;
	}

	protected boolean matchProxySupportedConfigs(IContainer container,
			String[] remoteSupportedConfigs) {
		if (remoteSupportedConfigs == null)
			return false;
		ContainerTypeDescription description = getContainerTypeDescription(container);
		if (description == null)
			return false;
		return description.getImportedConfigs(remoteSupportedConfigs) != null;
	}

}
