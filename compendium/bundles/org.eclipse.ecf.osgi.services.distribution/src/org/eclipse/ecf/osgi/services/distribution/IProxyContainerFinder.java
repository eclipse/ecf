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

import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.osgi.services.discovery.IRemoteServiceEndpointDescription;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;

/**
 * Service interface for customizing the finding of proxy remote service
 * containers. Services registered with this interfaces will be consulted when a
 * remote proxy is discovered, in order to select and/or connect
 * IRemoteServiceContainer instances to use to create proxies.
 */
public interface IProxyContainerFinder {

	/**
	 * 
	 * Find remote service containers. Implementers of this service will be
	 * consulted when a remote proxy is discovered, so that they may provide
	 * access to zero or more {@link IRemoteServiceContainer} instances to use
	 * for proxy creation and local publication in the service registry.
	 * 
	 * @param serviceID
	 *            the service ID exposed by the discovery provider. Will not be
	 *            <code>null</code>.
	 * @param endpointDescription
	 *            the endpoint description created from the discovered remote
	 *            service meta data. This endpointDescription may be used to
	 *            decide what IRemoteServiceContainer[] to return, as well as
	 *            whether or not to connect the IContainer to the targetID
	 *            (provided by
	 *            {@link IRemoteServiceEndpointDescription#getConnectTargetID()}
	 *            . Will not be <code>null</code>.
	 * 
	 * @return IRemoteServiceContainer[] the remote service containers that
	 *         should be used to get remote service references for the remote
	 *         service described by the endpointDescription. If no containers
	 *         are relevant, then an empty array should be returned rather than
	 *         <code>null</code>.
	 */
	public IRemoteServiceContainer[] findProxyContainers(IServiceID serviceID,
			IRemoteServiceEndpointDescription endpointDescription);

}
