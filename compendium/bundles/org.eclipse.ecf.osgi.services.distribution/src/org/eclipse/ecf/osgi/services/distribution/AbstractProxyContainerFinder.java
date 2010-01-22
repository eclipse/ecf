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

import org.eclipse.ecf.core.ContainerCreateException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
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
					logException("Exception connecting container id="
							+ container.getID() + " to connectTargetID="
							+ connectTargetID, e);
				}
			}
		}
	}

	protected IConnectContext getConnectContext(IContainer container,
			ID connectTargetID) {
		return null;
	}

	protected Collection createAndConfigureProxyContainers(String[] remoteSupportedConfigs) {
		List result = new ArrayList();
		if (remoteSupportedConfigs == null
				|| remoteSupportedConfigs.length == 0)
			return result;
		// Select config type from the remoteSupportedConfigs array. By default,
		// this is the first element of the array
		String selectedConfigType = selectConfigType(remoteSupportedConfigs);
		// If none selected we can't continue
		if (selectedConfigType == null)
			return result;
		// Now we create a new container instance given the selectedConfigType
		IRemoteServiceContainer container = createContainer(selectedConfigType);
		// If not successfully created, we can't continue
		if (container == null)
			return result;
		// Else we've created one and we'll report this to tracing
		trace("createAndConfigureProxyContainers",
				"created new proxy container with config type="
						+ selectedConfigType + " and id="
						+ container.getContainer().getID());
		result.add(container);
		return result;
	}

	protected IRemoteServiceContainer createContainer(String containerTypeDescriptionName) {
		try {
			IContainer container = getContainerFactory().createContainer(
					containerTypeDescriptionName);
			return new RemoteServiceContainer(container);
		} catch (ContainerCreateException e) {
			logException(
					"Cannot create container with container type description name="
							+ containerTypeDescriptionName, e);
			return null;
		}
	}

	protected String selectConfigType(String[] remoteSupportedConfigs) {
		// By default, we'll select the first config to use...
		return remoteSupportedConfigs[0];
	}

}
