/****************************************************************************
 * Copyright (c) 2004-2011 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.remoteservice;

/**
 * Remote service API constants.
 */
public interface Constants {

	/**
	 * Remote service property identifying all (named &quot;ecf.robjectClass&quot;) 
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
	 * service's remote registration number (of type <code>java.lang.Long</code>).
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
	 * Service property (named &quot;ecf.rsvc.ranking&quot;) identifying a
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
	 * Remote service property that defines the remote service container ID.
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the <code>BundleContext.registerService</code> method.
	 * @since 3.0
	 */
	public static final String SERVICE_CONTAINER_ID = "ecf.rsvc.cid"; //$NON-NLS-1$

	/**
	 * Remote service property used on service registration to indicate that a service proxy 
	 * should be created rather than using the given service object (which may be null
	 * when this service property is set).
	 * @since 4.0
	 */
	public static final String SERVICE_REGISTER_PROXY = "ecf.rsvc.proxy"; //$NON-NLS-1$

	/**
	 * Remote service property used to prevent the automatic addition of the IRemoteServiceProxy
	 * interface to the proxy returned from IRemoteService.getProxy.  If this service
	 * property is set (to any non-null Object value), it will prevent the the IRemoteServiceProxy from
	 * being added to the set of interfaces exposed by the proxy returned from IRemoteService.getProxy.
	 * @since 6.0
	 */
	public static final String SERVICE_PREVENT_RSPROXY = "ecf.rsvc.norsproxy"; //$NON-NLS-1$

	/**
	 * This constant allows the fully qualified async remote service proxy to be specified
	 * as a service property.  For example, if the remote service interface is as so:
	 * <pre>
	 * package foo;
	 * 
	 * public interface Bar {
	 *    String doStuff();
	 * }
	 * </pre>
	 * then by default, the async remote service proxy interface class would be expected 
	 * to be the following:
	 * <pre>
	 * package foo;
	 * import org.eclipse.ecf.remoteservice.IAsyncRemoteServiceProxy;
	 * import org.eclipse.equinox.concurrent.future.IFuture;
	 *
	 * public interface BarAsync extends IAsyncRemoteServiceProxy {
	 *    IFuture doStuffAsync();
	 * }
	 * </pre>
	 * This property allows a new class to be associated with the
	 * original service interface, so that rather than looking for the foo.BarAsync class
	 * when a proxy is created, the class specified by the value of the property will
	 * be used instead.  For example, assume the existance of another async
	 * remote service interface:
	 * <pre>
	 * package gogo;
	 * import org.eclipse.ecf.remoteservice.IAsyncRemoteServiceProxy;
	 * import org.eclipse.equinox.concurrent.future.IFuture;
	 * 
	 * public interface MyBar extends IAsyncRemoteServiceProxy {
	 *    IFuture doStuffAsync();
	 * }
	 * </pre>
	 * Further assume that when the remote service was registered, that a service property 
	 * was specified:
	 * <pre>
	 * serviceProps.put("ecf.rsvc.async.proxy_&lt;fq classname&gt;","&lt;fq substitute&gt;");
	 * </pre>
	 * <pre>
	 * serviceProps.put("ecf.rsvc.async.proxy_foo.Bar","gogo.MyBar");
	 * </pre>
	 * Then, when a Bar proxy is created, if the 'gogo.MyBar' interface is available on 
	 * the client, an async remote service proxy will be added to the proxy, and
	 * client will be able to asynchronously call MyBar.doStuffAsync() on the proxy.
	 */
	public static final String SERVICE_ASYNC_RSPROXY_CLASS_ = "ecf.rsvc.async.proxy_"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	public static final String SERVICE_CONNECT_ID = "ecf.rsvc.cnct.id"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	public static final String SERVICE_CONNECT_ID_NAMESPACE = "ecf.rsvc.cnct.id.ns"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	public static final String SERVICE_IDFILTER_NAMESPACE = "ecf.rsvc.idfltr.ns"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	public static final String SERVICE_IDFILTER_ID = "ecf.rsvc.idfltr.id"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	public static final String SERVICE_OBJECTCLASS = "ecf.rsvc.robjectclass"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	public static final String SERVICE_FILTER_PROPERTY = "ecf.rsvc.fltr"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	public static final String SERVICE_CONTAINER_FACTORY_NAME = "ecf.rsvc.cfn"; //$NON-NLS-1$
}
