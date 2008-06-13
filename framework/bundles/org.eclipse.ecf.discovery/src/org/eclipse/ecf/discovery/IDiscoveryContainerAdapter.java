/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.discovery.identity.*;

/**
 * Entry point discovery container adapter. This interface exposes the ability
 * to add/remove listeners for newly discovered services and service types,
 * register and unregister locally provided services, and get (synch) and
 * request (asynchronous) service info from a remote service provider.
 * <p>
 * This interface can be used by container provider implementations as an
 * adapter so that calls to
 * IContainer.getAdapter(IDiscoveryContainerAdapter.class) will return a
 * non-null instance of a class that implements this interface. Clients can then
 * proceed to use this interface to interact with the given discovery
 * implementation.
 * 
 */
public interface IDiscoveryContainerAdapter extends IAdaptable {

	/** ECF Service Property Names **/
	public static final String CONTAINER_FACTORY_NAME_PROPERTY = "org.eclipse.ecf.containerFactoryName"; //$NON-NLS-1$
	public static final String CONTAINER_CONNECT_TARGET = "org.eclipse.ecf.connectTarget"; //$NON-NLS-1$
	public static final String CONTAINER_CONNECT_TARGET_PROTOCOL = "org.eclipse.ecf.connectTargetProtocol"; //$NON-NLS-1$
	public static final String CONTAINER_CONNECT_TARGET_PATH = "org.eclipse.ecf.connectTargetPath"; //$NON-NLS-1$
	public static final String CONTAINER_CONNECT_REQUIRES_PASSWORD = "org.eclipse.ecf.connectContextRequiresPassword"; //$NON-NLS-1$

	/**
	 * Add a service listener. The given listener will have its method called
	 * when a service is discovered.
	 * 
	 * @param listener
	 *            IServiceListener to be notified. Must not be <code>null</code>.
	 */
	public void addServiceListener(IServiceListener listener);

	/**
	 * Add a service listener. The given listener will have its method called
	 * when a service with a type matching that specified by the first parameter
	 * is discovered.
	 * 
	 * @param type
	 *            String type to listen for. Must not be <code>null</code>.
	 *            Must be formatted according to this specific IDiscoveryContainer
	 * @param listener
	 *            IServiceListener to be notified. Must not be <code>null</code>.
	 */
	public void addServiceListener(IServiceTypeID type, IServiceListener listener);

	/**
	 * Add a service type listener. The given listener will have its method
	 * called when a service type is discovered.
	 * 
	 * @param listener
	 *            the listener to be notified. Must not be <code>null</code>.
	 */
	public void addServiceTypeListener(IServiceTypeListener listener);

	/**
	 * Synchronously retrieve info about the service
	 * 
	 * @param service
	 *            IServiceID of the service to get info about. Must not be
	 *            <code>null</code>.
	 * @return IServiceInfo the service info retrieved. <code>null</code> if
	 *         no information retrievable.
	 */
	public IServiceInfo getServiceInfo(IServiceID service);

	/**
	 * Synchronously get service info about all known services
	 * 
	 * @return IServiceInfo[] the resulting array of service info instances.
	 *         Will not be <code>null</code>. May be of length 0.
	 */
	public IServiceInfo[] getServices();

	/**
	 * Synchronously get service info about all known services of given service type
	 * 
	 * @param type
	 *            IServiceTypeID defining the type of service we are interested in
	 *            getting service info about. Must not be <code>null</code>
	 * @return IServiceInfo[] the resulting array of service info instances.
	 *         Will not be <code>null</code>. May be of length 0.
	 */
	public IServiceInfo[] getServices(IServiceTypeID type);

	/**
	 * Get a Namespace for services associated with this discovery container adapter.  The given Namespace
	 * may be used via {@link ServiceIDFactory} to create IServiceIDs rather than simple IDs.  For example:
	 * <pre>
	 * IServiceID serviceID = ServiceIDFactory.getDefault().createServiceID(container.getServicesNamespace(),serviceType,serviceName);
	 * </pre>
	 * 
	 * @return Namespace for creating service IDs.  Will not be <code>null</code>.
	 */
	public Namespace getServicesNamespace();

	/**
	 * Synchronously get service info about all known services of given service type
	 * 
	 * @return IServiceTypeID[] the resulting array of service type IDs.
	 *         Will not be <code>null</code>. May be of length 0.
	 */
	public IServiceTypeID[] getServiceTypes();

	/**
	 * Register the given service. This publishes the service defined by the
	 * first parameter to the underlying publishing mechanism
	 * 
	 * @param serviceInfo
	 *            IServiceInfo of the service to be published. Must not be
	 *            <code>null</code>.
	 * @throws ECFException
	 *             if service info cannot be registered with this service
	 */
	public void registerService(IServiceInfo serviceInfo) throws ECFException;

	/**
	 * Remove a service listener. Remove the listener from this container
	 * 
	 * @param listener
	 *            IServiceListener listener to be removed. Must not be
	 *            <code>null</code>.
	 */
	public void removeServiceListener(IServiceListener listener);

	/**
	 * Remove a service listener. Remove the listener associated with the type
	 * specified by the first parameter.
	 * 
	 * @param type
	 *            String of the desired type to remove the listener. Must not be
	 *            <code>null</code>.
	 *            Must be formatted according to this specific IDiscoveryContainer
	 * @param listener
	 *            IServiceListener listener to be removed. Must not be
	 *            <code>null</code>.
	 */
	public void removeServiceListener(IServiceTypeID type, IServiceListener listener);

	/**
	 * Remove a service type listener. Remove the type listener.
	 * 
	 * @param listener
	 *            IServiceTypeListener to be removed. Must not be
	 *            <code>null</code>.
	 */
	public void removeServiceTypeListener(IServiceTypeListener listener);

	/**
	 * Unregister a previously registered service defined by serviceInfo.
	 * 
	 * @param serviceInfo
	 *            IServiceInfo defining the service to unregister. Must not be
	 *            <code>null</code>.
	 * @throws ECFException
	 *             if service info cannot be unregistered with this service
	 */
	public void unregisterService(IServiceInfo serviceInfo) throws ECFException;
}
