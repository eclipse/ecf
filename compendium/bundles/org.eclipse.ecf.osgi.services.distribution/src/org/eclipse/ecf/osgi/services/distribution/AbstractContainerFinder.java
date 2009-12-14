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

	protected Collection findExistingContainers(
			ServiceReference serviceReference,
			String[] serviceExportedInterfaces,
			String[] serviceExportedConfigs, String[] serviceIntents) {
		List results = new ArrayList();
		// Get all existing containers
		IContainer[] containers = getContainers();
		// If nothing there, then return empty array
		if (containers == null || containers.length == 0)
			return results;

		for (int i = 0; i < containers.length; i++) {
			// Check to make sure it's a rs container adapter. If it's not go
			// onto next one
			IRemoteServiceContainerAdapter adapter = hasRemoteServiceContainerAdapter(containers[i]);
			if (adapter == null)
				continue;
			// Get container type description and intents
			ContainerTypeDescription description = getContainerTypeDescription(containers[i]);
			// If it has no description go onto next
			if (description == null)
				continue;

			if (matchExistingContainerToConfigsAndIntents(serviceReference,
					containers[i], adapter, description,
					serviceExportedConfigs, serviceIntents)) {
				trace("matchExistingContainerToConfigsAndIntents",
						"INCLUDING containerID="
								+ containers[i].getID()
								+ "configs="
								+ ((serviceExportedConfigs == null) ? "null"
										: Arrays.asList(serviceExportedConfigs)
												.toString())
								+ "intents="
								+ ((serviceIntents == null) ? "null" : Arrays
										.asList(serviceIntents).toString()));
				results.add(new RemoteServiceContainer(containers[i], adapter));
			} else {
				trace("matchExistingContainerToConfigsAndIntents",
						"EXCLUDING containerID="
								+ containers[i].getID()
								+ "configs="
								+ ((serviceExportedConfigs == null) ? "null"
										: Arrays.asList(serviceExportedConfigs)
												.toString())
								+ "intents="
								+ ((serviceIntents == null) ? "null" : Arrays
										.asList(serviceIntents).toString()));
			}
		}
		return results;
	}

	protected String[] getSupportedConfigTypes(
			ContainerTypeDescription containerTypeDescription) {
		// XXX with the new ContainerTypeDescription.getSupportedConfigTypes()
		// API, the supportedConfigTypes list should be created from
		// ContainerTypeDescription.getSupportedConfigTypes()
		// But the ContainerTypeDescription.getSupportedConfigTypes()
		return new String[] { containerTypeDescription.getName() };
	}

	protected String[] getSupportedIntents(
			ContainerTypeDescription containerTypeDescription) {
		String[] supportedIntents = containerTypeDescription
				.getSupportedIntents();
		return (supportedIntents == null) ? new String[0] : supportedIntents;
	}

	protected boolean matchExistingContainerToConfigsAndIntents(
			ServiceReference serviceReference, IContainer container,
			IRemoteServiceContainerAdapter adapter,
			ContainerTypeDescription description, String[] requiredConfigTypes,
			String[] requiredServiceIntents) {
		// If the exported config is null/empty, and serviceIntents is also
		// null then this container is an appropriate provider
		if (requiredConfigTypes == null && requiredServiceIntents == null)
			return true;

		if (requiredConfigTypes != null
				&& matchConfigTypes(requiredConfigTypes,
						getSupportedConfigTypes(description))) {
			return matchIntents(requiredServiceIntents,
					getSupportedIntents(description));
		}
		return false;
	}

	protected boolean matchConfigTypes(String[] serviceRequiredConfigTypes,
			String[] supportedConfigTypes) {
		if (supportedConfigTypes == null)
			return false;
		List supportedConfigTypeList = Arrays.asList(supportedConfigTypes);
		boolean result = true;
		for (int i = 0; i < serviceRequiredConfigTypes.length; i++)
			result = result
					&& supportedConfigTypeList
							.contains(serviceRequiredConfigTypes[i]);
		return result;
	}

	protected boolean matchIntents(String[] serviceRequiredIntents,
			String[] supportedIntents) {

		if (supportedIntents == null)
			return false;

		List supportedIntentsList = Arrays.asList(supportedIntents);

		boolean result = true;
		for (int i = 0; i < serviceRequiredIntents.length; i++)
			result = result
					&& supportedIntentsList.contains(serviceRequiredIntents[i]);

		return result;
	}

	protected Collection createAndConfigureContainers(
			ServiceReference serviceReference,
			String[] serviceExportedInterfaces, String[] requiredConfigs,
			String[] requiredIntents) {

		List results = new ArrayList();
		ContainerTypeDescription[] descriptions = getContainerTypeDescriptions();
		if (descriptions == null)
			return results;

		for (int i = 0; i < descriptions.length; i++) {
			IRemoteServiceContainer rsContainer = createMatchingContainer(
					descriptions[i], serviceReference,
					serviceExportedInterfaces, requiredConfigs, requiredIntents);
			if (rsContainer != null)
				results.add(rsContainer);
		}
		return results;
	}

	protected IRemoteServiceContainer createMatchingContainer(
			ContainerTypeDescription containerTypeDescription,
			ServiceReference serviceReference,
			String[] serviceExportedInterfaces, String[] requiredConfigs,
			String[] requiredIntents) {

		// If there are no required configs, we don't know what to do/create
		if (requiredConfigs != null) {
			if (matchConfigTypes(requiredConfigs,
					getSupportedConfigTypes(containerTypeDescription))) {
				if (matchIntents(requiredIntents,
						getSupportedIntents(containerTypeDescription))) {
					try {
						IContainer container = createContainer(
								serviceReference, containerTypeDescription);
						return new RemoteServiceContainer(container);
					} catch (Exception e) {
						logException(
								"Exception creating container from ContainerTypeDescription="
										+ containerTypeDescription, e);
					}

				}
			}
		}
		return null;
	}

	private IContainer createContainer(ServiceReference serviceReference,
			ContainerTypeDescription containerTypeDescription)
			throws ContainerCreateException {

		IContainerFactory containerFactory = getContainerFactory();
		if (containerFactory == null)
			throw new ContainerCreateException(
					"container factory must not be null");

		Object containerFactoryArguments = serviceReference
				.getProperty(IDistributionConstants.CONTAINER_FACTORY_ARGUMENTS);
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

}
