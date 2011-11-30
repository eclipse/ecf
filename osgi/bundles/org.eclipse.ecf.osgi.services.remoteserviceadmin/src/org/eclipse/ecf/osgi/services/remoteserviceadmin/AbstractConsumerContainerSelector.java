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

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;

/**
 * Abstract superclass for consumer container selectors...i.e. implementers of
 * {@link IConsumerContainerSelector}).
 * 
 */
public class AbstractConsumerContainerSelector extends
		AbstractContainerSelector {

	protected IRemoteServiceContainer selectExistingConsumerContainer(
			ID endpointID, String[] remoteSupportedConfigs, ID connectTargetID) {

		// Get all containers available
		IContainer[] containers = getContainers();
		// If none then return null
		if (containers == null)
			return null;

		for (int i = 0; i < containers.length; i++) {
			// Do *not* include containers with same ID as endpointID
			if (matchContainerID(containers[i], endpointID))
				continue;

			IRemoteServiceContainerAdapter adapter = hasRemoteServiceContainerAdapter(containers[i]);
			// Container must have adapter
			if (adapter != null
					// And it must match the connect namespace
					&& matchConnectNamespace(containers[i], endpointID,
							connectTargetID)
					// and it must match the configs
					&& matchSupportedConfigs(containers[i],
							remoteSupportedConfigs)
					// and the container should either not be connected or
					// already be connected to the desired endpointID
					&& matchNotConnected(containers[i], endpointID,
							connectTargetID)) {
				trace("selectExistingConsumerContainer", //$NON-NLS-1$
						"MATCH of existing remote service container id=" //$NON-NLS-1$
								+ containers[i].getID()
								+ " endpointID=" //$NON-NLS-1$
								+ endpointID
								+ " remoteSupportedConfigs=" //$NON-NLS-1$
								+ ((remoteSupportedConfigs == null) ? "[]" //$NON-NLS-1$
										: Arrays.asList(remoteSupportedConfigs)
												.toString()));
				return new RemoteServiceContainer(containers[i], adapter);
			} else {
				trace("selectExistingConsumerContainer", //$NON-NLS-1$
						"No match of existing remote service container id=" //$NON-NLS-1$
								+ containers[i].getID()
								+ " endpointID=" //$NON-NLS-1$
								+ endpointID
								+ " remoteSupportedConfigs=" //$NON-NLS-1$
								+ ((remoteSupportedConfigs == null) ? "[]" //$NON-NLS-1$
										: Arrays.asList(remoteSupportedConfigs)
												.toString()));
			}
		}
		return null;
	}

	protected boolean matchNotConnected(IContainer container, ID endpointID,
			ID connectTargetID) {
		// if the container is not connected, OR it's connected to the desired
		// endpointID already then we've got a match
		ID connectedID = container.getConnectedID();
		if (connectedID == null || connectedID.equals(endpointID)
				|| connectedID.equals(connectTargetID))
			return true;
		return false;
	}

	protected boolean matchSupportedConfigs(IContainer container,
			String[] remoteSupportedConfigs) {
		if (remoteSupportedConfigs == null)
			return false;
		ContainerTypeDescription description = getContainerTypeDescription(container);
		if (description == null)
			return false;
		return description.getImportedConfigs(remoteSupportedConfigs) != null;
	}

	protected void connectContainerToTarget(
			IRemoteServiceContainer rsContainer, ID connectTargetID) {
		if (connectTargetID == null)
			return;
		IContainer container = rsContainer.getContainer();
		ID connectedID = container.getConnectedID();
		// Only connect the container to the connect target when
		// it's not already connected
		if (connectedID == null) {
			// connect to target
			try {
				connectContainer(container, connectTargetID,
						getConnectContext(container, connectTargetID));
			} catch (ContainerConnectException e) {
				logException("Exception connecting container id=" //$NON-NLS-1$
						+ container.getID() + " to connectTargetID=" //$NON-NLS-1$
						+ connectTargetID, e);
			}
		}
	}

	protected IConnectContext getConnectContext(IContainer container,
			ID connectTargetID) {
		return null;
	}

	protected IRemoteServiceContainer createAndConfigureConsumerContainer(
			String[] remoteSupportedConfigs, Map remoteExportedProperties)
			throws SelectContainerException {
		if (remoteSupportedConfigs == null
				|| remoteSupportedConfigs.length == 0)
			return null;
		// Get container factory
		IContainerFactory containerFactory = getContainerFactory();
		if (containerFactory == null)
			return null;
		// Get all container type descriptions from factory
		List containerTypeDescriptions = containerFactory.getDescriptions();
		if (containerTypeDescriptions == null)
			return null;

		// Go through all containerTypeDescriptions
		for (Iterator i = containerTypeDescriptions.iterator(); i.hasNext();) {
			ContainerTypeDescription desc = (ContainerTypeDescription) i.next();
			// For each one, get the localImportedConfigs for the remote
			// supported configs
			String[] localImportedConfigs = desc
					.getImportedConfigs(remoteSupportedConfigs);
			// If their are some local imported configs for this description
			if (localImportedConfigs != null) {
				// Then get the imported config properties
				Dictionary importedConfigProperties = desc
						.getPropertiesForImportedConfigs(
								localImportedConfigs,
								PropertiesUtil
										.createDictionaryFromMap(remoteExportedProperties));
				// Then select a specific local imported config (typically the
				// first on in the array)
				String selectedConfig = selectLocalImportedConfig(
						localImportedConfigs, importedConfigProperties);
				// If we have one to use, then create the container
				if (selectedConfig != null) {
					IRemoteServiceContainer rsContainer = createContainer(
							desc,
							selectedConfig,
							PropertiesUtil
									.createMapFromDictionary(importedConfigProperties));
					if (rsContainer != null) {
						trace("createAndConfigureProxyContainers", //$NON-NLS-1$
								"created new proxy container with config type=" //$NON-NLS-1$
										+ selectedConfig + " and id=" //$NON-NLS-1$
										+ rsContainer.getContainer().getID());
						return rsContainer;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @since 2.0
	 */
	protected IRemoteServiceContainer createContainer(
			ContainerTypeDescription containerTypeDescription,
			String containerTypeDescriptionName, Map properties)
			throws SelectContainerException {
		try {
			IContainer container = (properties == null) ? getContainerFactory()
					.createContainer(containerTypeDescriptionName)
					: getContainerFactory().createContainer(
							containerTypeDescriptionName, properties);
			IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) container
					.getAdapter(IRemoteServiceContainerAdapter.class);
			if (adapter == null)
				throw new SelectContainerException(
						"Container does not implement IRemoteServiceContainerAdapter", null, containerTypeDescription); //$NON-NLS-1$
			return new RemoteServiceContainer(container);
		} catch (ContainerCreateException e) {
			String message = "Cannot create container with container type description name=" //$NON-NLS-1$
					+ containerTypeDescriptionName;
			logException(message, e);
			throw new SelectContainerException(message, e,
					containerTypeDescription);
		}
	}

	protected String selectLocalImportedConfig(String[] localConfigTypes,
			Dictionary importedConfigProperties) {
		if (localConfigTypes == null || localConfigTypes.length == 0)
			return null;
		// By default, we'll select the first config to use...
		return localConfigTypes[0];
	}

}
