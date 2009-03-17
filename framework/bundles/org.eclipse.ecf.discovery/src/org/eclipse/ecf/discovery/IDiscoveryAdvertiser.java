/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ecf.discovery;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.Namespace;

/**
 * @since 3.0
 */
public interface IDiscoveryAdvertiser extends IAdaptable {
	/**
	 * The name of the discovery container under which it is registered with the
	 * OSGi runtime as a service property
	 */
	public static final String CONTAINER_NAME = "org.eclipse.ecf.discovery.containerName"; //$NON-NLS-1$

	/**
	 * Register the given service. This publishes the service defined by the
	 * first parameter to the underlying publishing mechanism
	 * 
	 * @param serviceInfo
	 *            IServiceInfo of the service to be published. Must not be
	 *            <code>null</code>.
	 * @return IFuture indicates whether service registration has been
	 *         successfully
	 */
	public void registerService(IServiceInfo serviceInfo);

	/**
	 * Unregister a previously registered service defined by serviceInfo.
	 * 
	 * @param serviceInfo
	 *            IServiceInfo defining the service to unregister. Must not be
	 *            <code>null</code>.
	 * @return IFuture indicates whether service deregistration has been
	 *         successfully
	 */
	public void unregisterService(IServiceInfo serviceInfo);

	/**
	 * Unregister all previously registered service.
	 * 
	 * @return IFuture indicates whether service deregistration has been
	 *         successfully
	 */
	public void unregisterAllServices();

	/**
	 * Get a Namespace for services associated with this discovery container
	 * adapter. The given Namespace may be used via IServiceIDFactory to create
	 * IServiceIDs rather than simple IDs. For example:
	 * 
	 * <pre>
	 * IServiceID serviceID = ServiceIDFactory.getDefault().createServiceID(
	 * 		container.getServicesNamespace(), serviceType, serviceName);
	 * </pre>
	 * 
	 * @return Namespace for creating service IDs. Will not be <code>null</code>
	 *         .
	 */
	public Namespace getServicesNamespace();
}
