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

import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceIDFactory;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.remoteservice.IRemoteService;

/**
 * ECF Remote Service Admin RemoteConstants. These are constants for ECF's RSA
 * implementation-specific meta-data. For OSGi Remote constant see
 * {@link org.osgi.service.remoteserviceadmin.RemoteConstants} and the <a
 * href="http://www.osgi.org/download/r4v42/r4.enterprise.pdf">OSGI 4.2 Remote
 * Service Admin specification (chap 122)</a>.
 */
public class RemoteConstants {

	private RemoteConstants() {
		// not instantiable
	}

	/**
	 * ECF discovery service type for Remote Service Admin. All ECF remote
	 * services published by Remote Service Admin advertisers should have this
	 * value as one of the entries in the list returned from
	 * {@link IServiceTypeID#getServices()}.
	 */
	public static final String DISCOVERY_SERVICE_TYPE = "ecf.osgirsvc"; //$NON-NLS-1$
	/**
	 * ECF discovery scope property. Value type is String+. If set, the value
	 * will be used by the {@link IServiceInfoFactory} during
	 * {@link IServiceInfoFactory#createServiceInfo(org.eclipse.ecf.discovery.IDiscoveryAdvertiser, EndpointDescription)}
	 * to create an {@link IServiceTypeID} via
	 * {@link IServiceIDFactory#createServiceTypeID(org.eclipse.ecf.core.identity.Namespace, String[], String[], String[], String)}
	 * . The scopes value determines the third parameter. If not explicitly set,
	 * the {@link IServiceTypeID#DEFAULT_SCOPE} is used.
	 */
	public static final String DISCOVERY_SCOPE = "ecf.endpoint.discovery.scope"; //$NON-NLS-1$
	/**
	 * ECF discovery protocols property. Value type is String+. If set, the
	 * value will be used by the {@link IServiceInfoFactory} during
	 * {@link IServiceInfoFactory#createServiceInfo(org.eclipse.ecf.discovery.IDiscoveryAdvertiser, EndpointDescription)}
	 * to create an {@link IServiceTypeID} via
	 * {@link IServiceIDFactory#createServiceTypeID(org.eclipse.ecf.core.identity.Namespace, String[], String[], String[], String)}
	 * . The protocols value determines the fourth parameter. If not explicitly
	 * set, the {@link IServiceTypeID#DEFAULT_PROTO} is used.
	 */
	public static final String DISCOVERY_PROTOCOLS = "ecf.endpoint.discovery.protocols"; //$NON-NLS-1$
	/**
	 * ECF discovery naming authority property. Value type is String. If set,
	 * the value will be used by the {@link IServiceInfoFactory} during
	 * {@link IServiceInfoFactory#createServiceInfo(org.eclipse.ecf.discovery.IDiscoveryAdvertiser, EndpointDescription)}
	 * to create an {@link IServiceTypeID} via
	 * {@link IServiceIDFactory#createServiceTypeID(org.eclipse.ecf.core.identity.Namespace, String[], String[], String[], String)}
	 * . The protocols value determines the fifth parameter. If not explicitly
	 * set, the {@link IServiceTypeID#DEFAULT_NA} is used.
	 */
	public static final String DISCOVERY_NAMING_AUTHORITY = "ecf.endpoint.discovery.namingauthority"; //$NON-NLS-1$
	/**
	 * ECF discovery service name property. Value type is String. If set, the
	 * value will be used by the {@link IServiceInfoFactory} during
	 * {@link IServiceInfoFactory#createServiceInfo(org.eclipse.ecf.discovery.IDiscoveryAdvertiser, EndpointDescription)}
	 * to create an {@link IServiceInfo} with a given name. The default is a
	 * globally unique identifier. Note that if this value is explicitly set,
	 * care should be taken to not have the name conflict with other remote
	 * service names.
	 */
	public static final String DISCOVERY_SERVICE_NAME = "ecf.endpoint.discovery.servicename"; //$NON-NLS-1$
	/**
	 * ECF service name default prefix. If the DISCOVERY_SERVICE_NAME is
	 * <b>not</b> set, this prefix will be the precede the unique identifier.
	 */
	public static final String DISCOVERY_DEFAULT_SERVICE_NAME_PREFIX = "osgirsvc_"; //$NON-NLS-1$

	/**
	 * Optional ECF {@link EndpointDescription} property (with value of type
	 * String) that defines the unique
	 * {@link org.eclipse.ecf.core.identity.Namespace} name. If present in the
	 * {@link EndpointDescription}, the value will be used to create the
	 * containerID for accessing a remote service. The Namespace name is
	 * optional because typically the ID protocol specifier (e.g. 'ecftcp' in ID
	 * with name: 'ecftcp://localhost:3282/server') can be used to unambiguously
	 * determine the appropriate {@link org.eclipse.ecf.core.identity.Namespace}
	 * used to create the container ID for remote service import.
	 */
	public static final String ENDPOINT_CONTAINER_ID_NAMESPACE = "ecf.endpoint.id.ns"; //$NON-NLS-1$
	/**
	 * Optional ECF {@link EndpointDescription} property (with value of type
	 * String) that defines a connect target ID. If set/non-<code>null</code>,
	 * this property can be used by remote service consumers to connect to a
	 * specific container, and access a remote service exposed by some
	 * <b>other</b> member of the group.
	 */
	public static final String ENDPOINT_CONNECTTARGET_ID = "ecf.endpoint.connecttarget.id"; //$NON-NLS-1$
	/**
	 * Optional ECF {@link EndpointDescription} property (with value of type
	 * String+) that defines one or more IDs used for filtering remote service
	 * references during
	 * {@link RemoteServiceAdmin#importService(org.osgi.service.remoteserviceadmin.EndpointDescription)}
	 * .
	 */
	public static final String ENDPOINT_IDFILTER_IDS = "ecf.endpoint.idfilter.ids"; //$NON-NLS-1$
	/**
	 * Optional ECF {@link EndpointDescription} property (with value of type
	 * String), that defines a remote services properties filter used during
	 * {@link RemoteServiceAdmin#importService(org.osgi.service.remoteserviceadmin.EndpointDescription)}
	 * .
	 */
	public static final String ENDPOINT_REMOTESERVICE_FILTER = "ecf.endpoint.rsfilter"; //$NON-NLS-1$

	/**
	 * Container factory arguments for exported remote service hosts. If
	 * specified as a service property upon remote service registration, this
	 * property allows ECF containers to be initialized and configured upon
	 * creation during the call to
	 * {@link HostContainerSelector#selectHostContainers(org.osgi.framework.ServiceReference, String[], String[], String[])}
	 * . The type of the value may be String, ID, or Object[]. The
	 * IContainerFactory.createContainer method is then selected based upon the
	 * type of the value...i.e.
	 * {@link IContainerFactory#createContainer(org.eclipse.ecf.core.ContainerTypeDescription, String)}
	 * ,
	 * {@link IContainerFactory#createContainer(org.eclipse.ecf.core.identity.ID)}
	 * , or {@link IContainerFactory#createContainer(String, Object[])}, and the
	 * value is passed in for container creation.
	 */
	public static final String SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS = "ecf.exported.containerfactoryargs"; //$NON-NLS-1$
	/**
	 * Container connect context for exported remote service hosts. If specified
	 * as a service property for remote service export, this property allows ECF
	 * containers to have given a connect context for authentication upon
	 * container connection by
	 * {@link HostContainerSelector#selectHostContainers(org.osgi.framework.ServiceReference, String[], String[], String[])}
	 * . The type of the value is {@link IConnectContext}.
	 */
	public static final String SERVICE_EXPORTED_CONTAINER_CONNECT_CONTEXT = "ecf.exported.containerconnectcontext"; //$NON-NLS-1$
	/**
	 * Container ID of the target host container for remote service export. If
	 * specified as a service property for remote service export, this property
	 * is used to match against the set of available containers in
	 * {@link HostContainerSelector#selectHostContainers(org.osgi.framework.ServiceReference, String[], String[], String[])}
	 * . The type of the value is {@link ID}.
	 */
	public static final String SERVICE_EXPORTED_CONTAINER_ID = "ecf.exported.containerid"; //$NON-NLS-1$

	/**
	 * Allows exporting ECF containers to determine the type of value associated
	 * with the
	 * {@link org.osgi.service.remoteserviceadmin.RemoteConstants#SERVICE_IMPORTED}
	 * property on the OSGi remote service consumer. For ECF, the default value
	 * type is {@link IRemoteService}. If set to some other value (e.g.
	 * {@link Boolean} by the exporting host container, then consumers can use
	 * the SERVICE_IMPORTED value appropriately.
	 */
	public static final String SERVICE_IMPORTED_VALUETYPE = "ecf.service.imported.valuetype"; //$NON-NLS-1$
	/**
	 * This property is set on the remote service proxy during
	 * {@link org.osgi.service.remoteserviceadmin.RemoteServiceAdmin#importService(org.osgi.service.remoteserviceadmin.EndpointDescription)}
	 * , before local service proxy registration. It's String value is set to
	 * the value returned from
	 * {@link org.osgi.service.remoteserviceadmin.EndpointDescription#getId()}.
	 * It allows those accessing the remote service proxy to get information
	 * about the endpoint id.
	 */
	public static final String SERVICE_IMPORTED_ENDPOINT_ID = "ecf.service.imported.endpoint.id"; //$NON-NLS-1$

	/**
	 * This property is set on the remote service proxy during
	 * {@link org.osgi.service.remoteserviceadmin.RemoteServiceAdmin#importService(org.osgi.service.remoteserviceadmin.EndpointDescription)}
	 * , before local service proxy registration. It's Long value is set to the
	 * value returned from
	 * {@link org.osgi.service.remoteserviceadmin.EndpointDescription#getServiceId()}
	 * . It allows those accessing the remote service proxy to get information
	 * about the endpoint remote service id.
	 */
	public static final String SERVICE_IMPORTED_ENDPOINT_SERVICE_ID = "ecf.service.imported.endpoint.service.id"; //$NON-NLS-1$
}
