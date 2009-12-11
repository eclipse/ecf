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

public interface IDistributionConstants {

	public static final String ECF_REMOTE_CONFIGURATION_TYPE = "ecf";

	// From OSGi 4.2 Compendium Specification, table 13.1 in r4.cmpn.pdf
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
	 * <b>Description</b>: Setting this property marks this service for export.
	 * It defines the interfaces under which this service can be exported. This
	 * list must be a subset of the types listed in the objectClass service
	 * property. The single value of an asterisk (’*’, \u002A) indicates all
	 * interfaces in the registration’s objectClass property and ignore the
	 * classes. It is strongly recommended to only export interfaces and not
	 * concrete classes due to the complexity of creating proxies for some type
	 * of concrete classes.
	 * </p>
	 */
	public static final String SERVICE_EXPORTED_INTERFACES = "service.exported.interfaces";

	public static final String SERVICE_EXPORTED_INTERFACES_WILDCARD = "*";

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
