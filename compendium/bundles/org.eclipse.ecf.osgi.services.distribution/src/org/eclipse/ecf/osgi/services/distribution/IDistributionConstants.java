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

import org.osgi.service.distribution.DistributionConstants;

public interface IDistributionConstants {

	/**
	 * This service registration property indicates that the provided service is
	 * to be made available remotely, which implies that it is suitable for
	 * remote invocations. This property lists a subset of service interface
	 * names specified in the <tt>BundleContext.registerService</tt> call,
	 * denoting the interfaces that are suitable for remoting. If the list
	 * contains only one value, which is set to "*", all of the interfaces
	 * specified in the <tt>BundleContext.registerService</tt> call are being
	 * exposed remotely.
	 * <p>
	 * The value of this property is of type String, String[] or Collection of
	 * String.
	 */
	public static final String REMOTE_INTERFACES = DistributionConstants.REMOTE_INTERFACES;

	/**
	 * This optional service registration property contains a list of intents
	 * that should be satisfied when publishing this service remotely. If a
	 * Distribution Provider implementation cannot satisfy these intents when
	 * exposing the service remotely, it should not expose the service.
	 * <p>
	 * The value of this property is of type String, String[] or Collection of
	 * String.
	 */
	public static final String REMOTE_REQUIRES_INTENTS = DistributionConstants.REMOTE_REQUIRES_INTENTS;

	/**
	 * This optional service registration property identifies the metadata type
	 * of additional metadata associated with the service provider or consumer,
	 * e.g. "sca" Multiple types and thus sets of additional metadata may be
	 * provided.
	 * <p>
	 * The value of this property is of type String, String[] or Collection of
	 * String.
	 */
	public static final String REMOTE_CONFIGURATION_TYPE = DistributionConstants.REMOTE_CONFIGURATION_TYPE;

	/**
	 * This optional service registration property contains a list of intents
	 * provided by the service itself. The property advertises capabilities of
	 * the service implementation and can be used by the service consumer in the
	 * lookup filter to only select a service that provides certain qualities of
	 * service.
	 * <p>
	 * These service intents may be interpreted by other framework components
	 * for example to take them into account when exposing that service
	 * remotely.
	 * <p>
	 * In case of proxies to remote services the value of this property is a
	 * union of the value specified by the service provider, plus its
	 * remote-specific intents (see {@link #REMOTE_REQUIRES_INTENTS} ), plus any
	 * intents which the Distribution Software adds to describe characteristics
	 * of the distribution mechanism. Therefore the value of this property can
	 * vary between the client side proxy and the server side service.
	 * <p>
	 * The value of this property is of type String, String[] or Collection of
	 * String.
	 */
	public static final String DEPLOYMENT_INTENTS = DistributionConstants.DEPLOYMENT_INTENTS;

	/**
	 * This service registration property is set on client side service proxies
	 * registered in the OSGi Service Registry. This allows service consumers to
	 * identify remote services if needed.
	 * <p>
	 * The value of this property is undefined. The simple fact that the
	 * property is set denotes that the service is running remotely.
	 */
	public static final String REMOTE = DistributionConstants.REMOTE;

	public static final String REMOTE_INTERFACES_WILDCARD = "*";

	public static final String ECF_REMOTE_CONFIGURATION_TYPE = "ecf";

}
