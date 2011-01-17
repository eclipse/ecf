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

public class RemoteConstants {

	private RemoteConstants() {
		// not instantiable
	}
	
	public static final String SERVICE_TYPE = "osgirsvc";

	public static final String DISCOVERY_SCOPE = "ecf.endpoint.discovery.scope";
	public static final String DISCOVERY_PROTOCOLS = "ecf.endpoint.discovery.protocols";
	public static final String DISCOVERY_NAMING_AUTHORITY = "ecf.endpoint.discovery.namingauthority";
	public static final String DISCOVERY_SERVICE_NAME = "ecf.endpoint.discovery.servicename";
	public static final String DISCOVERY_DEFAULT_SERVICE_NAME_PREFIX = "osgirsvc_";

	// container id namespace. Value of type String
	public static final String ENDPOINT_CONTAINER_ID_NAMESPACE = "ecf.endpoint.id.ns";
	// target id. Value of type String
	public static final String ENDPOINT_CONNECTTARGET_ID = "ecf.endpoint.connecttarget.id";
	// id filter. Value of type String+
	public static final String ENDPOINT_IDFILTER_IDS = "ecf.endpoint.idfilter.ids";
	// remote service filter. Value of type String
	public static final String ENDPOINT_REMOTESERVICE_FILTER = "ecf.endpoint.rsfilter";

	// Value of
	// org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS
	// For ECF providers this is a constant
	public static final String ENDPOINT_SERVICE_IMPORTED_CONFIGS_VALUE = "org.eclipse.ecf.config";

	// Value of type String,ID,or Object[]
	public static final String SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS = "ecf.exported.containerfactoryargs";
	// Value of type IConnectContext
	public static final String SERVICE_EXPORTED_CONTAINER_CONNECT_CONTEXT = "ecf.exported.containerconnectcontext";
	// Value of type ID
	public static final String SERVICE_EXPORTED_CONTAINER_ID = "ecf.exported.containerid";

	public static final String SERVICE_IMPORTED_VALUETYPE = "ecf.imported.valuetype";
}
