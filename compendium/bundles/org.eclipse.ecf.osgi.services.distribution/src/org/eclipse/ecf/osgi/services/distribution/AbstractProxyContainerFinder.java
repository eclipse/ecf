/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;

public abstract class AbstractProxyContainerFinder extends
		AbstractContainerFinder {

	protected Collection findExistingProxyContainers(ID endpointID,
			String[] remoteSupportedConfigs) {

		List results = new ArrayList();
		// Get all containers available
		IContainer[] containers = getContainers();
		// If none then return null
		if (containers == null)
			return results;

		for (int i = 0; i < containers.length; i++) {
			// Do *not* include containers with same ID as endpointID
			if (matchContainerID(containers[i], endpointID))
				continue;

			IRemoteServiceContainerAdapter adapter = hasRemoteServiceContainerAdapter(containers[i]);
			// Container must have adapter
			if (adapter != null
			// And it must match the connect namespace
					&& matchConnectNamespace(containers[i], endpointID)
					// and it must match the configs
					&& matchProxySupportedConfigs(containers[i],
							remoteSupportedConfigs)
					// and the container should either not be connected or
					// already be connected to the desired endpointID
					&& matchNotConnected(containers[i], endpointID)) {
				trace("findExistingProxyContainers", //$NON-NLS-1$
						"MATCH of existing remote service container id=" //$NON-NLS-1$
								+ containers[i].getID()
								+ " endpointID=" //$NON-NLS-1$
								+ endpointID
								+ " remoteSupportedConfigs=" //$NON-NLS-1$
								+ ((remoteSupportedConfigs == null) ? "[]" //$NON-NLS-1$
										: Arrays.asList(remoteSupportedConfigs)
												.toString()));
				results.add(new RemoteServiceContainer(containers[i], adapter));
			} else {
				trace("findExistingProxyContainers", //$NON-NLS-1$
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
		return results;
	}

	protected boolean matchNotConnected(IContainer container, ID endpointID) {
		// if the container is not connected, OR it's connected to the desired
		// endpointID already then we've got a match
		ID connectedID = container.getConnectedID();
		if (connectedID == null || connectedID.equals(endpointID))
			return true;
		return false;
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

	protected void connectContainersToTarget(Collection rsContainers,
			ID connectTargetID) {
		if (connectTargetID == null)
			return;
		for (Iterator i = rsContainers.iterator(); i.hasNext();) {
			IContainer container = ((IRemoteServiceContainer) i.next())
					.getContainer();
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
	}

	protected IConnectContext getConnectContext(IContainer container,
			ID connectTargetID) {
		return null;
	}

	protected Collection createAndConfigureProxyContainers(
			String[] remoteSupportedConfigs, Map remoteExportedProperties) {
		if (remoteSupportedConfigs == null
				|| remoteSupportedConfigs.length == 0)
			return Collections.EMPTY_LIST;
		// Get container factory
		IContainerFactory containerFactory = getContainerFactory();
		if (containerFactory == null)
			return Collections.EMPTY_LIST;
		// Get all container type descriptions from factory
		List containerTypeDescriptions = containerFactory.getDescriptions();
		if (containerTypeDescriptions == null)
			return Collections.EMPTY_LIST;

		List results = new ArrayList();
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
								createDictionaryFromMap(remoteExportedProperties));
				// Then select a specific local imported config (typically the
				// first on in the array)
				String selectedConfig = selectLocalImportedConfig(
						localImportedConfigs, importedConfigProperties);
				// If we have one to use, then create the container
				if (selectedConfig != null) {
					IRemoteServiceContainer rsContainer = createContainer(
							selectedConfig,
							createMapFromDictionary(importedConfigProperties));
					if (rsContainer != null) {
						trace("createAndConfigureProxyContainers", //$NON-NLS-1$
								"created new proxy container with config type=" //$NON-NLS-1$
										+ selectedConfig + " and id=" //$NON-NLS-1$
										+ rsContainer.getContainer().getID());
						results.add(rsContainer);
					}
				}
			}
		}
		return results;
	}

	private Map createMapFromDictionary(Dictionary input) {
		if (input == null)
			return null;
		Map result = new HashMap();
		for (Enumeration e = input.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object val = input.get(key);
			result.put(key, val);
		}
		return result;
	}

	private Dictionary createDictionaryFromMap(Map propMap) {
		if (propMap == null)
			return null;
		Dictionary result = new Properties();
		for (Iterator i = propMap.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Object val = propMap.get(key);
			result.put(key, val);
		}
		return result;
	}

	protected IRemoteServiceContainer createContainer(
			String containerTypeDescriptionName, Map properties) {
		try {
			IContainer container = (properties == null) ? getContainerFactory()
					.createContainer(containerTypeDescriptionName)
					: getContainerFactory().createContainer(
							containerTypeDescriptionName, properties);
			return new RemoteServiceContainer(container);
		} catch (Exception e) {
			logException(
					"Cannot create container with container type description name=" //$NON-NLS-1$
							+ containerTypeDescriptionName, e);
			return null;
		}
	}

	protected String selectLocalImportedConfig(String[] localConfigTypes,
			Dictionary importedConfigProperties) {
		// By default, we'll select the first config to use...
		return localConfigTypes[0];
	}

}
