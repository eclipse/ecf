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

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.osgi.services.discovery.IRemoteServiceEndpointDescription;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;

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
		return (IRemoteServiceContainer[]) findExistingProxyContainers(
				endpointID, remoteSupportedConfigs).toArray(
				new IRemoteServiceContainer[] {});

	}

}
