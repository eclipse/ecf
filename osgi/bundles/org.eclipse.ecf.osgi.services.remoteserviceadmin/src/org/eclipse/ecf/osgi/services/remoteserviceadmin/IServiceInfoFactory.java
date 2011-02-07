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

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;

/**
 * Service info factory service contract. A service info factory is used by the
 * {@link EndpointDescriptionAdvertiser} to convert {@link EndpointDescription}s
 * to {@link IServiceInfo} instances (via
 * {@link #createServiceInfo(IDiscoveryAdvertiser, EndpointDescription)}. The
 * resulting IServiceInfo instances are then used to by the
 * {@link EndpointDescriptionAdvertiser} to publish the EndpointDescription
 * metadata to a {@link IDiscoveryAdvertiser}.
 * <p>
 * <p>
 * If no other instances of this service have been registered, a default
 * instance of {@link ServiceInfoFactory} will be used by the
 * {@link EndpointDescriptionAdvertiser}. Note that this default instance is
 * registered with the lowest possible priority, so that if other
 * {@link IServiceInfoFactory} instances are registered, they will be
 * preferred/used over the default. This means that Those wishing to
 * customize/control this process of converting {@link EndpointDescription}s to
 * {@link IServiceInfo} must
 * <ul>
 * <li>create their own implementation of {@link IServiceInfoFactory}</li>
 * <li>register it with the OSGi service registry with a priority
 * ({org.osgi.framework.Constants#SERVICE_RANKING}) higher than
 * {@link Integer#MIN_VALUE}</li>
 * <ul>
 * Then at runtime, when needed by the {@link EndpointDescriptionAdvertiser},
 * the new service info factory will be used.
 * 
 * @see IDiscoveredEndpointDescriptionFactory
 */
public interface IServiceInfoFactory {

	/**
	 * Create an service info instance to represent the given
	 * endpointDescription for discovery using the given discovery advertiser.
	 * 
	 * @param advertiser
	 *            the advertiser to use for creating the service info result.
	 *            Must not be <code>null</code>.
	 * @param endpointDescription
	 *            the endpoint description that the service info is to
	 *            represent. Must not be <code>null</code>.
	 * @return IServiceInfo to use to publish the endpointDescription for
	 *         discovery (via
	 *         {@link IDiscoveryAdvertiser#registerService(IServiceInfo)}. If a
	 *         service info instance has previously been created for the given
	 *         endpointDescription by this service info factory, then that
	 *         serviceInfo will be returned in favor of creating a new one.
	 *         Otherwise, a new service info will be created and returned. If
	 *         some error occurs in the creation of the serviceInfo,
	 *         <code>null</code> will be returned.
	 */
	public IServiceInfo createServiceInfo(IDiscoveryAdvertiser advertiser,
			EndpointDescription endpointDescription);

	/**
	 * Remove any previously created service info that is associated with the
	 * given endpointDescription (and advertiser).
	 * 
	 * @param advertiser
	 *            the advertiser associated with the service info previously
	 *            created. Must not be <code>null</code>.
	 * @param endpointDescription
	 *            the endpoint description that the service info was previously
	 *            created for. Must not be <code>null</code>.
	 * @return IServiceInfo to use to unpublish the endpointDescription for
	 *         discovery (via
	 *         {@link IDiscoveryAdvertiser#unregisterService(IServiceInfo)). If
	 *         <code>null</code> no service info exists that had previously been
	 *         created for the given endpointDescription and advertiser.
	 */
	public IServiceInfo removeServiceInfo(IDiscoveryAdvertiser advertiser,
			EndpointDescription endpointDescription);

}
