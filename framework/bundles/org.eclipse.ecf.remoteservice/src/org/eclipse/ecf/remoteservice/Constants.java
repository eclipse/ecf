/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.core.IContainer;

/**
 * Remote service API constants.
 */
public interface Constants {

	/**
	 * Remote service property (named &quot;remote.objectClass&quot;) identifying all
	 * of the class names under which a service was registered in the remote
	 * services API (of type <code>java.lang.String[]</code>).
	 * 
	 * <p>
	 * This property is set by the remote services API when a service is
	 * registered.
	 */
	public static final String OBJECTCLASS = "ecf.robjectClass"; //$NON-NLS-1$

	/**
	 * Remote service property (named &quot;ecf.rsvc.id&quot;) identifying a
	 * service's registration number (of type <code>java.lang.Long</code>).
	 * 
	 * <p>
	 * The value of this property is assigned by the remote services API when a
	 * service is registered. The remote services API assigns a unique value
	 * that is larger than all previously assigned values since the remote
	 * services API was started. These values are NOT persistent across restarts
	 * of the remote services API.
	 */
	public static final String SERVICE_ID = "ecf.rsvc.id"; //$NON-NLS-1$

	/**
	 * Service property (named &quot;remote.service.ranking&quot;) identifying a
	 * service's ranking number (of type <code>java.lang.Integer</code>).
	 * 
	 * <p>
	 * This property may be supplied in the <code>properties
	 * Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 * 
	 * <p>
	 * The service ranking is used by the remote services API to determine the
	 * <i>default </i> service to be returned from a call to the
	 * {@link IRemoteServiceContainerAdapter#getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID[], String, String)}
	 * method: If more than one service implements the specified class, the
	 * <code>RemoteServiceReference</code> object with the highest ranking is
	 * returned.
	 * 
	 * <p>
	 * The default ranking is zero (0). A service with a ranking of
	 * <code>Integer.MAX_VALUE</code> is very likely to be returned as the
	 * default service, whereas a service with a ranking of
	 * <code>Integer.MIN_VALUE</code> is very unlikely to be returned.
	 * 
	 * <p>
	 * If the supplied property value is not of type
	 * <code>java.lang.Integer</code>, it is deemed to have a ranking value
	 * of zero.
	 */
	public static final String SERVICE_RANKING = "ecf.rsvc.ranking"; //$NON-NLS-1$

	/**
	 * Service property (named &quot;remote.service.vendor&quot;) identifying a
	 * service's vendor.
	 * 
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 */
	public static final String SERVICE_VENDOR = "ecf.rsvc.vendor"; //$NON-NLS-1$

	/**
	 * Service property (named &quot;remoteservice.description&quot;)
	 * identifying a service's description.
	 * 
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 */
	public static final String SERVICE_DESCRIPTION = "ecf.rsvc.desc"; //$NON-NLS-1$

	/**
	 * Service property (named &quot;remoteservice.description&quot;)
	 * identifying a a registration's target for receiving the service. The
	 * value of the property MUST be either a non-<code>null</code> instance
	 * of org.eclipse.ecf.core.identity.ID OR an ID[].
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 */
	public static final String SERVICE_REGISTRATION_TARGETS = "ecf.rsvc.reg.targets"; //$NON-NLS-1$

	/**
	 * Remote service property that defines the container factory name.
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 * @since 3.0
	 */
	public static final String SERVICE_CONTAINER_FACTORY_NAME = "ecf.rsvc.cfn"; //$NON-NLS-1$

	/**
	 * Service property that defines the container target for connection.
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 * @since 3.0
	 */
	public static final String SERVICE_CONTAINER_TARGET = "ecf.rsvc.target"; //$NON-NLS-1$

	/**
	 * Service property that defines the remote service container ID factory name.
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the <code>BundleContext.registerService</code> method.
	 * @since 3.0
	 */
	public static final String SERVICE_CONTAINER_ID_FACTORY = "ecf.rsvc.cidf"; //$NON-NLS-1$

	/**
	 * Remote service property that defines the remote service container ID.
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the <code>BundleContext.registerService</code> method.
	 * @since 3.0
	 */
	public static final String SERVICE_CONTAINER_ID = "ecf.rsvc.cid"; //$NON-NLS-1$

	/**
	 * Service property that determines whether a remote service proxy is automatically added to the local
	 * service registry.  This property can be used to expose remote services transparently
	 * to client (i.e. automatically putting a proxy into the client's local service registry).
	 * If this property is set in during service registration, then the the associated remote 
	 * service proxy should be added to the client's service registry by the implementing provider.  The value 
	 * of the property can be any non-<code>null</code> value.  
	 * <p></p>
	 * For example:
	 * <pre>
	 * final Dictionary props = new Hashtable();
	 * props.put(Constants.AUTOREGISTER_REMOTE_PROXY, "true");
	 * // Register
	 * adapters[0].registerRemoteService(new String[] {IConcatService.class.getName()}, serviceImpl, props);
	 * </pre>
	 * 
	 */
	public static final String AUTOREGISTER_REMOTE_PROXY = "ecf.rsvc.areg"; //$NON-NLS-1$

	// Constants for use with the ECF remote services API

	/**
	 * Discovery service property to specify a namespace name for creating a connect id.  Note that
	 * this property should be equal to the name of the namespace retrieved from {@link IContainer#getConnectNamespace()}.
	 * Note that this property is <b>optional</b>.
	 * @since 3.0
	 */
	public static final String SERVICE_CONNECT_ID_NAMESPACE = "ecf.rsvc.cnct.id.ns"; //$NON-NLS-1$

	/**
	 * Discovery service property to specify value for creating a connect id.  Note that
	 * this property should be equal to connectID retrieved from {@link IContainer#getConnectedID()}.
	 * Note that this property is <b>optional</b>.
	 * @since 3.0
	 */
	public static final String SERVICE_CONNECT_ID = "ecf.rsvc.cnct.id"; //$NON-NLS-1$

	/**
	 * Discovery service property to specify a namespace name for creating a target service ID.
	 * Note that this property is <b>optional</b>. It is 
	 * expected that clients will use the value of this property, along with the SERVICE_ID_PROPERTY
	 * to create an ID instance for the 'idFilter' parameter via
	 * remoteServicesContainerAdapter.getRemoteServiceReferences(ID [] idFilter, String clazz, String filter). 
	 * @since 3.0
	 */
	public static final String SERVICE_IDFILTER_NAMESPACE = "ecf.rsvc.idfltr.ns"; //$NON-NLS-1$

	/**
	 * Discovery service property for a 'remotesvcs' discovery type.  Note that this
	 * property is <b>optional</b>.  It is expected
	 * that clients will use the value of this property, along with the SERVICE_IDFILTER_NAMESPACE
	 * to create an ID instance for the 'idFilter' parameter via
	 * remoteServicesContainerAdapter.getRemoteServiceReferences(ID [] idFilter, String clazz, String filter). 
	 * @since 3.0
	 */
	public static final String SERVICE_IDFILTER_ID = "ecf.rsvc.idfltr.id"; //$NON-NLS-1$

	/**
	 * Discovery Service property specifying the clazz paramter in  
	 * remoteServiceContainerAdapter.getRemoteServiceReferences(ID [] idFilter, String clazz, String filter);
	 * @since 3.0
	 */
	public static final String SERVICE_OBJECTCLASS = "ecf.rsvc.robjectclass"; //$NON-NLS-1$

	/**
	 * Discovery service property for specifying the service lookup filter for
	 * client service lookup via 
	 * remoteServicesContainerAdapter.getRemoteServiceReferences(ID [] idFilter, String clazz, String filter).  
	 * Note that this
	 * property is <b>optional</b> if the DISCOVERY_SERVICE_TYPE is as given above.
	 * @since 3.0
	 */
	public static final String SERVICE_FILTER_PROPERTY = "ecf.rsvc.fltr"; //$NON-NLS-1$

	/**
	 * Discovery service property specifying the expected namespace name for corresponding
	 * to remoteServiceContainerAdapter.getRemoteServicesNamespace()
	 * @since 3.0
	 */
	public static final String SERVICE_NAMESPACE = "ecf.rsvc.ns"; //$NON-NLS-1$

	/**
	 * Service property used on service registration to indicate that a service proxy 
	 * should be created rather than using the given service object (which may be null
	 * when this service property is set).
	 * @since 4.0
	 */
	public static final String SERVICE_REGISTER_PROXY = "ecf.rsvc.proxy"; //$NON-NLS-1$
}
