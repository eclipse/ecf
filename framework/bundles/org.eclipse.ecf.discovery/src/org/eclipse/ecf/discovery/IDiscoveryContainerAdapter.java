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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.discovery.identity.IServiceID;

/**
 * Entry point discovery container adapter. This interface exposes the ability
 * to add/remove listeners for newly discovered services and service types,
 * register and unregister locally provided services, and get (synch) and
 * request (asynch) service info from a remote service provider.
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
	/**
	 * Add a service listener. The given listener will have its method called
	 * when a service with a type matching that specified by the first parameter
	 * is discovered.
	 * 
	 * @param type
	 *            String type to listen for. Must not be <code>null</code>.
	 * @param listener
	 *            IServiceListener to be notified. Must not be <code>null</code>.
	 */
	public void addServiceListener(String type, IServiceListener listener);

	/**
	 * Remove a service listener. Remove the listener associated with the type
	 * specified by the first parameter.
	 * 
	 * @param type
	 *            String of the desired type to remove the listener. Must not be
	 *            <code>null</code>.
	 * @param listener
	 *            IServiceListener listener to be removed. Must not be
	 *            <code>null</code>.
	 */
	public void removeServiceListener(String type, IServiceListener listener);

	/**
	 * Add a service type listener. The given listener will have its method
	 * called when a service type is discovered.
	 * 
	 * @param listener
	 *            the listener to be notified. Must not be <code>null</code>.
	 */
	public void addServiceTypeListener(IServiceTypeListener listener);

	/**
	 * Remove a service type listener. Remove the type listener.
	 * 
	 * @param listener
	 *            IServiceTypeListener to be removed. Must not be
	 *            <code>null</code>.
	 */
	public void removeServiceTypeListener(IServiceTypeListener listener);

	/**
	 * Register the given service type. This publishes the given service type to
	 * the underlying publishing mechanism
	 * 
	 * @param serviceType
	 *            String of the serviceType to be published. Must not be
	 *            <code>null</code>.
	 */
	public void registerServiceType(String serviceType);

	/**
	 * Register the given service. This publishes the service defined by the
	 * first parameter to the underlying publishing mechanism
	 * 
	 * @param serviceInfo
	 *            IServiceInfo of the service to be published. Must not be
	 *            <code>null</code>.
	 */
	public void registerService(IServiceInfo serviceInfo) throws IOException;

	/**
	 * Synchronously (within given timeout) retrieve info about the service
	 * defined by the first parameter.
	 * 
	 * @param service
	 *            IServiceID of the service to get info about. Must not be
	 *            <code>null</code>.
	 * @param timeout
	 *            int time to wait for a response (in ms)
	 * @return IServiceInfo the service info retrieved. <code>null</code> if
	 *         no information retrieved within timeout.
	 */
	public IServiceInfo getServiceInfo(IServiceID service, int timeout);

	/**
	 * Asynchronously (within given timeout) retrieve info about the service
	 * defined by the first parameter. Sends a request for service information
	 * and returns. Answers to such requests occur via {@link IServiceListener}'s
	 * {@link IServiceListener#serviceResolved(IServiceEvent)} method.
	 * 
	 * @param service
	 *            ServiceID of the service to get info about. Must not be
	 *            <code>null</code>.
	 * @param timeout
	 *            int time to wait for a response (in ms)
	 */
	public void requestServiceInfo(IServiceID service, int timeout);

	/**
	 * Unregister service defined by serviceInfo.
	 * 
	 * @param serviceInfo
	 *            IServiceInfo defining the service to unregister. Must not be
	 *            <code>null</code>.
	 */
	public void unregisterService(IServiceInfo serviceInfo);

	/**
	 * Get service info about all known services of given service type
	 * 
	 * @param type
	 *            String defining the type of service we are interested in
	 *            getting service info about. Must not be <code>null</code>
	 * @return IServiceInfo[] the resulting array of service info instances.
	 *         Will not be <code>null</code>. May be of length 0.
	 */
	public IServiceInfo[] getServices(String type);
}
