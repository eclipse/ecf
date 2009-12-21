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
import java.util.Collection;
import java.util.List;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.osgi.services.discovery.IRemoteServiceEndpointDescription;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;

/**
 * Default implementation of IProxyContainerFinder.
 * 
 */
public class DefaultProxyContainerFinder extends AbstractContainerFinder
		implements IProxyContainerFinder {

	public IRemoteServiceContainer[] findProxyContainers(IServiceID serviceID,
			IRemoteServiceEndpointDescription endpointDescription) {

		trace("findProxyContainers", "serviceID=" + serviceID
				+ " endpointDescription=" + endpointDescription);

		// Get the endpointID from the endpointDescription
		ID endpointID = endpointDescription.getEndpointAsID();
		// Get the remote supported configs
		String[] remoteSupportedConfigs = endpointDescription
				.getSupportedConfigs();

		// Find any/all existing containers for the proxy that
		// match the endpointID namespace and the remoteSupportedConfigs
		Collection rsContainers = findExistingProxyContainers(endpointID,
				remoteSupportedConfigs);

		// If no containers found, then create one/ones
		if (rsContainers.size() == 0)
			rsContainers = createAndConfigureProxyContainers(serviceID,
					endpointDescription, endpointID, remoteSupportedConfigs);
		/*
		 * ID connectTargetID = endpointDescription.getConnectTargetID();
		 * IRemoteServiceContainer[] connectedContainers = (connectTargetID ==
		 * null) ? rsContainers : connectRemoteServiceContainers(rsContainers,
		 * connectTargetID, monitor); if (connectedContainers == null) {
		 * logWarning("findProxyContainers",
		 * "No remote service containers found after connect"); return
		 * EMPTY_REMOTE_SERVICE_CONTAINER_ARRAY; } trace("findProxyContainers",
		 * "connectRemoteServiceContainers.size=" + rsContainers.size());
		 */
		return (IRemoteServiceContainer[]) rsContainers
				.toArray(new IRemoteServiceContainer[] {});

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
				// XXX trace here
				results.add(new RemoteServiceContainer(containers[i], adapter));
			} else {
				// XXX trace here
			}
			// make sure that the namespaces match
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
		// XXX for now do an automatic match...eventually we will compare with
		// return value from getImportedConfigs to determine if their is a match
		// return (description.getImportedConfigs(remoteSupportedConfigs) !=
		// null);
		return true;
	}

	protected Collection createAndConfigureProxyContainers(
			IServiceID serviceID,
			IRemoteServiceEndpointDescription endpointDescription,
			ID endpointID, String[] remoteSupportedConfigs) {
		// TODO Auto-generated method stub
		return null;
	}

}
