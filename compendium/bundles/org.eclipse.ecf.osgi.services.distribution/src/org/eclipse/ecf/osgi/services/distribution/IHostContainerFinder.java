/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.distribution;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.osgi.framework.ServiceReference;

/**
 * Service interface for customizing the finding of host remote service
 * containers. Services registered with this interfaces will be consulted when a
 * remote service host is registered, in order to select and/or connect
 * IRemoteServiceContainer instances to use to publish remote references.
 */
public interface IHostContainerFinder {

	/**
	 * Find remote service containers. Implementers of this service will be
	 * consulted when a remote service is registered, so that they may provide
	 * access to zero or more {@link IRemoteServiceContainer} instances to use
	 * for distribution and remote service publication and discovery.
	 * 
	 * @param serviceReference
	 *            the ServiceReference of the original service registration.
	 * @param remoteInterfaces
	 *            the remote interfaces specified by the remote service
	 *            registration. Will not be <code>null</code>.
	 * @param remoteConfigurationType
	 *            remote configuration type. May be <code>null</code>.
	 * @param remoteRequiresIntents
	 *            the remote requires intents. May be <code>null</code>.
	 * @return IRemoteServiceContainer[] the remote service containers that
	 *         should distribute and publish the remote service (specified by
	 *         the serviceReference) for remote access.
	 */
	public IRemoteServiceContainer[] findHostContainers(
			ServiceReference serviceReference, String[] remoteInterfaces,
			String[] remoteConfigurationType, String[] remoteRequiresIntents);

}
