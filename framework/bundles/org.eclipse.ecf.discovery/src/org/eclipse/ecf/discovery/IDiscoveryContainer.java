/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery;

import java.io.IOException;

import org.eclipse.ecf.core.identity.ServiceID;

/**
 * Adapter interface for shared object containers that support lookup and discovery.
 * This interface exposes the ability to add/remove listeners for newly
 * discovered services and service types, register and unregister locally provided
 * services, and get (synch) and request (asynch) service info from a remote
 * service provider.  
 * <p>
 * This interface can be used by container provider implementations as an adapter
 * so that calls to ISharedObjectContainer.getAdapter(IDiscoveryContainer.class)
 * will return a non-null instance of a class that implements this interface.
 * Clients can then proceed to use this interface to interact with the given
 * discovery implementation.
 * 
 */
public interface IDiscoveryContainer {
	/**
	 * Add a service listener.  The given listener will have its method called
	 * when a service with a type matching that specified by the first parameter
	 * is discovered.
	 * 
	 * @param type the ServiceID of the desired type to listen for
	 * @param listener the listener to be notified
	 */
	public void addServiceListener(ServiceID type, IServiceListener listener);
	/**
	 * Remove a service listener.  Remove the listener associated with the type
	 * specified by the first parameter.
	 * 
	 * @param type the ServiceID of the desired type to remove the listener
	 * @param listener the listener to be removed
	 */
	public void removeServiceListener(ServiceID type, IServiceListener listener);
	/**
	 * Add a service type listener.  The given listener will have its method called
	 * when a service type is discovered.
	 * 
	 * @param listener the listener to be notified
	 */
	public void addServiceTypeListener(IServiceTypeListener listener);
	/**
	 * Remove a service type listener.  Remove the type listener.
	 * 
	 * @param listener the listener to be removed
	 */
	public void removeServiceTypeListener(IServiceTypeListener listener);
	/**
	 * Register the given service type.  This publishes the given service type to the
	 * underlying publishing mechanism
	 * 
	 * @param serviceType the ServiceID of the type to be published
	 */
	public void registerServiceType(ServiceID serviceType);
	/**
	 * Register the given service type.  This publishes the given service type to the
	 * underlying publishing mechanism
	 * 
	 * @param serviceType the String of the type to be published
	 */
	public void registerServiceType(String serviceType);
	/**
	 * Register the given service.  This publishes the service defined by the first parameter to the
	 * underlying publishing mechanism
	 * 
	 * @param serviceInfo the IServiceInfo of the service to be published
	 */
	public void registerService(IServiceInfo serviceInfo) throws IOException;
	/**
	 * Synchronously (within given timeout) retrieve info about the service defined by
	 * the first parameter.
	 * 
	 * @param service the ServiceID of the service to get info about
	 * @param timeout the time to wait for a response (in ms)
	 * @return IServiceInfo the service info retrieved.  Null if no information retrieved within timeout.
	 */
	public IServiceInfo getServiceInfo(ServiceID service, int timeout);
	/**
	 * Asynchronously (within given timeout) retrieve info about the service defined by
	 * the first parameter.  Sends a request for service information and returns.  Answers to
	 * such requests occur via the IServiceListener.resolveService() method.
	 * 
	 * @param service the ServiceID of the service to get info about
	 * @param timeout the time to wait for a response (in ms)
	 */
	public void requestServiceInfo(ServiceID service, int timeout);
	/**
	 * Unregister service defined by serviceInfo.
	 * 
	 * @param serviceInfo the info defining the service to unregister
	 */
	public void unregisterService(IServiceInfo serviceInfo);
	/**
	 * Unregister all services
	 *
	 */
	public void unregisterAllServices();
	/**
	 * Get service info about all known services of given service type
	 * 
	 * @param type the ServiceID defining the type of service we are interested in getting
	 * service info about
	 * @return IServiceInfo[] the resulting array of service info instances
	 */
	public IServiceInfo [] getServices(ServiceID type);
}
