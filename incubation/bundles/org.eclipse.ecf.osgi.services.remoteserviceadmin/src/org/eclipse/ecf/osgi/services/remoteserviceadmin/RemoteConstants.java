/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

public class RemoteConstants {

	public static final String SERVICE_TYPE = "osgirsvc";
	
	public static final String DISCOVERY_SCOPE = "ecf.endpoint.discovery.scope";
	public static final String DISCOVERY_PROTOCOLS = "ecf.endpoint.discovery.protocols";
	public static final String DISCOVERY_NAMING_AUTHORITY = "ecf.endpoint.discovery.nameingauthority";
	public static final String DISCOVERY_SERVICE_NAME = "ecf.endpoint.discovery.servicename";
	public static final String DISCOVERY_DEFAULT_SERVICE_NAME_PREFIX = "osgirsvc_";
	
	// ECF endpoint description properties <-> service info properties
	// container id external form
	public static final String ENDPOINT_ID = "ecf.endpoint.id";
	// container id namespace
	public static final String ENDPOINT_ID_NAMESPACE = "ecf.endpoint.id.ns";
	// remote service id
	public static final String ENDPOINT_REMOTESERVICE_ID = "ecf.endpoint.rs.id";
	
	public static final String ENDPOINT_TARGET_ID = "ecf.endpoint.target.id";
	public static final String ENDPOINT_TARGET_ID_NAMESPACE = "ecf.endpoint.target.id.ns";
	
// id filter external form
	public static final String ENDPOINT_IDFILTER_IDS = "ecf.endpoint.idfilter.ids";
	// id filter namespaces
	public static final String ENDPOINT_IDFILTER_NAMESPACES = "ecf.endpoint.idfilter.ids.ns";
	// remote service filter
	public static final String ENDPOINT_REMOTESERVICEFILTER = "ecf.endpoint.rsfilter";

}
