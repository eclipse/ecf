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

import org.eclipse.ecf.osgi.services.discovery.IRemoteServiceEndpointDescription;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Listener for proxy distribution events. Services registered with this as
 * their service interface will have their methods called when the distribution
 * implementation events occur.
 * 
 */
public interface IProxyDistributionListener {

	/**
	 * Event that indicates that an endpointDescription has been discovered and
	 * that the remote service references (specified by the endpointDescription)
	 * are being lookedup using the given remoteServiceContainer. If multiple
	 * remoteServiceContainers are found, this method will be called multiple
	 * times, each with a distinct remoteServiceContainer.
	 * 
	 * @param endpointDescription
	 *            the endpointDescription that describes the discovered endpoint
	 *            that exposes some remote services. Will not be
	 *            <code>null</code>.
	 * @param remoteServiceContainer
	 *            a remote service container that has been found (via the
	 *            {@link IProxyContainerFinder}) and will be used for remote
	 *            lookup.
	 */
	public void retrievingRemoteServiceReferences(
			IRemoteServiceEndpointDescription endpointDescription,
			IRemoteServiceContainer remoteServiceContainer);

	/**
	 * Event that indicates that the given endpointDescription and
	 * remoteServiceContainer have resulted in a remoteServiceReference that
	 * will be used to register a remote service.
	 * 
	 * @param endpointDescription
	 *            the endpointDescription that describes the discovered endpoint
	 *            that exposes some remote services. Will not be
	 *            <code>null</code>.
	 * @param remoteServiceContainer
	 *            a remote service container that has been found (via the
	 *            {@link IProxyContainerFinder}) and will be used for remote
	 *            lookup. Will not be <code>null</code>.
	 * @param remoteServiceReference
	 *            a remote service reference that has resulted from using the
	 *            endpointDescription meta-data and remoteServiceContainer to
	 *            successfully lookup this reference. Will not be
	 *            <code>null</code>.
	 */
	public void registering(
			IRemoteServiceEndpointDescription endpointDescription,
			IRemoteServiceContainer remoteServiceContainer,
			IRemoteServiceReference remoteServiceReference);

	/**
	 * Event that indicates that a remote service proxy has been successfully
	 * been looked up, created, and registered in the local service registry.
	 * 
	 * @param endpointDescription
	 *            the endpointDescription that describes the discovered endpoint
	 *            that exposes some remote services. Will not be
	 *            <code>null</code>.
	 * @param remoteServiceContainer
	 *            a remote service container that has been found (via the
	 *            {@link IProxyContainerFinder}) and will be used for remote
	 *            lookup. Will not be <code>null</code>.
	 * @param remoteServiceReference
	 *            a remote service reference that has resulted from using the
	 *            endpointDescription meta-data and remoteServiceContainer to
	 *            successfully lookup this reference. Will not be
	 *            <code>null</code>.
	 * @param proxyServiceRegistration
	 *            the local ServiceRegistration that was created when the local
	 *            proxy was successfully added to the service registry.
	 */
	public void registered(
			IRemoteServiceEndpointDescription endpointDescription,
			IRemoteServiceContainer remoteServiceContainer,
			IRemoteServiceReference remoteServiceReference,
			ServiceRegistration proxyServiceRegistration);

	/**
	 * Event that indicates that a remote service proxy has been unregistered
	 * from the local service registry.
	 * 
	 * @param endpointDescription
	 *            the endpointDescription that describes the discovered endpoint
	 *            that exposes some remote services. May be <code>null</code> if
	 *            the service is unregistered for some event other than
	 *            discovery (e.g. container disconnection).
	 * @param proxyServiceRegistration
	 *            the local ServiceRegistration that was created when the local
	 *            proxy was successfully added to the service registry.
	 */
	public void unregistered(
			IRemoteServiceEndpointDescription endpointDescription,
			ServiceRegistration proxyServiceRegistration);
}
