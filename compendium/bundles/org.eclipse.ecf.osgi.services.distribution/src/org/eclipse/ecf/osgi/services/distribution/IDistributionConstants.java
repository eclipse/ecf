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
	public static final String REMOTE_INTERFACES = "osgi.remote.interfaces";

	/**
	 * This optional service registration property contains a list of intents
	 * that should be satisfied when publishing this service remotely. If a
	 * Distribution Provider implementation cannot satisfy these intents when
	 * exposing the service remotely, it should not expose the service.
	 * <p>
	 * The value of this property is of type String, String[] or Collection of
	 * String.
	 */
	public static final String REMOTE_REQUIRES_INTENTS = "osgi.remote.requires.intents";

	/**
	 * This optional service registration property identifies the metadata type
	 * of additional metadata associated with the service provider or consumer,
	 * e.g. "sca" Multiple types and thus sets of additional metadata may be
	 * provided.
	 * <p>
	 * The value of this property is of type String, String[] or Collection of
	 * String.
	 */
	public static final String REMOTE_CONFIGURATION_TYPE = "osgi.remote.configuration.type";

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
	public static final String DEPLOYMENT_INTENTS = "osgi.deployment.intents";

	/**
	 * This service registration property is set on client side service proxies
	 * registered in the OSGi Service Registry. This allows service consumers to
	 * identify remote services if needed.
	 * <p>
	 * The value of this property is undefined. The simple fact that the
	 * property is set denotes that the service is running remotely.
	 */
	public static final String REMOTE = "osgi.remote";

	public static final String REMOTE_INTERFACES_WILDCARD = "*";

	public static final String ECF_REMOTE_CONFIGURATION_TYPE = "ecf";

	// New distribution service properties... from r4.cmpn-draft-20090707.pdf

	// From table 13.1 in r4.cmpn-draft-20090707.pdf
	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: Registered by the distribution provider on one of its
	 * services to indicate the supported configuration types.
	 * </p>
	 */
	public static final String REMOTE_CONFIGS_SUPPORTED = "remote.configs.supported";

	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: Registered by the distribution provider on one of its
	 * services to indicate the vocabulary of implemented intent.
	 * </p>
	 */
	public static final String REMOTE_INTENTS_SUPPORTED = "remote.intents.supported";

	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: A list of configuration types that should be used to
	 * export the service. Each configuration type represents the configuration
	 * parameters for an endpoint. A distribution provider should create an
	 * endpoint for each configuration type that it supports.
	 * </p>
	 */
	public static final String SERVICE_EXPORTED_CONFIGS = "service.exported.configs";

	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: A list of intents that the distribution provider must
	 * implement to distribute the service. Intents listed in this property are
	 * reserved for intents that are critical for the code to function
	 * correctly, for example, ordering of messages. These intents should not be
	 * configurable.
	 * </p>
	 */
	public static final String SERVICE_EXPORTED_INTENTS = "service.exported.intents";

	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: This property is merged with the
	 * service.exported.intents property before the distribution provider
	 * interprets the listed intents; it has therefore the same semantics but
	 * the property should be configurable so the administrator can choose the
	 * intents based on the topology. Bundles should therefore make this
	 * property configurable, for example through the Configuration Admin
	 * service.
	 * </p>
	 */
	public static final String SERVICE_EXPORTED_INTENTS_EXTRA = "service.exported.intents.extra";

	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: This property is merged with the
	 * service.exported.intents property before the distribution provider
	 * interprets the listed intents; it has therefore the same semantics but
	 * the property should be configurable so the administrator can choose the
	 * intents based on the topology. Bundles should therefore make this
	 * property configurable, for example through the Configuration Admin
	 * service.
	 * </p>
	 */
	public static final String SERVICE_EXPORTED_INTERFACES = "service.exported.interfaces";

	/**
	 * <p>
	 * <b>Type</b>: *
	 * </p>
	 * <p>
	 * <b>Description</b>: Must be set by a distribution provider to any value
	 * when it registers the endpoint proxy as an imported service. A bundle can
	 * use this property to filter out imported services.
	 * </p>
	 */
	public static final String SERVICE_IMPORTED = "service.imported";

	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: The configuration information used to import this
	 * service, as described in service.exported.configs. Any associated
	 * properties for this configuration types must be properly mapped to the
	 * importing system. For example, a URL in these properties must point to a
	 * valid resource when used in the importing framework. If multiple
	 * configuration types are listed in this property, then they must be
	 * synonyms for exactly the same remote endpoint that is used to export this
	 * service.
	 * </p>
	 */
	public static final String SERVICE_IMPORTED_CONFIGS = "service.imported.configs";

	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: A list of intents that this service implements. This
	 * property has a dual purpose:
	 * <ul>
	 * <li>A bundle can use this service property to notify the distribution
	 * provider that these intents are already implemented by the exported
	 * service object.</li>
	 * <li>A distribution provider must use this property to convey the combined
	 * intents of:
	 * <ul>
	 * <li>The exporting service, and</li>
	 * <li>The intents that the exporting distribution provider adds.</li>
	 * <li>The intents that the importing distribution provider adds.</li>
	 * </ul>
	 * </ul>
	 * To export a service, a distribution provider must expand any qualified
	 * intents. Both the exporting and importing distribution providers must
	 * recognize all intents before a service can be distributed.
	 * </p>
	 */
	public static final String SERVICE_INTENTS = "service.intents";

	/**
	 * <p>
	 * <b>Type</b>: String+
	 * </p>
	 * <p>
	 * <b>Description</b>: Services that are exported should have a service.pid
	 * property. The service.pid (PID) is a unique persistent identity for the
	 * service, the PID is defined in Persistent Identifier (PID) on page 129 of
	 * the Core specification. This property enables a distribution provider to
	 * associate persistent proprietary data with a service registration.
	 * </p>
	 */
	public static final String SERVICE_PID = "service.pid";

}
