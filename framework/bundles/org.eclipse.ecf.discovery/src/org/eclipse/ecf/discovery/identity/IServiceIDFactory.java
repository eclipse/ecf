/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.discovery.identity;

import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;

/**
 * ServiceID factory contract.  
 * 
 * @see ServiceIDFactory
 */
public interface IServiceIDFactory {

	/**
	 * Create an IServiceID.  Creates an immutable IServiceID with a non-<code>null</code> {@link IServiceTypeID}
	 * and a potentially <code>null</code> service name. 
	 * @param namespace the Namespace instance to create the service ID with.  Must not be <code>null</code>.
	 * @param serviceType the service type to create the service ID with.  Must not be <code>null</code>.
	 * @param serviceName the service name for the service ID.  May be <code>null</code>.
	 * @return IServiceID created.  Will not be <code>null</code>.
	 * @throws IDCreateException if some problem creating the new IServiceID.
	 */
	public IServiceID createServiceID(Namespace namespace, IServiceTypeID serviceType, String serviceName) throws IDCreateException;

	/**
	 * Create an IServiceID.  Creates an immutable IServiceID with a non-<code>null</code> {@link IServiceTypeID}
	 * and a potentially <code>null</code> service name. 
	 * @param namespace the Namespace instance to create the service ID with.  Must not be <code>null</code>.
	 * @param serviceType the service type to create the service ID with.  Must not be <code>null</code>.
	 * @return IServiceID created.  Will not be <code>null</code>.
	 * @throws IDCreateException if some problem creating the new IServiceID.
	 */
	public IServiceID createServiceID(Namespace namespace, IServiceTypeID serviceType) throws IDCreateException;

	/**
	 * Create an IServiceID.  Creates an immutable IServiceID with a non-<code>null</code> {@link IServiceTypeID}
	 * and a potentially <code>null</code> service name. 
	 * @param namespace the Namespace instance to create the service ID with.  Must not be <code>null</code>.
	 * @param serviceType the service type to create the service ID with.  Must not be <code>null</code>.
	 * @param serviceName the service name for the service ID.  May be <code>null</code>.
	 * @return IServiceID created.  Will not be <code>null</code>.
	 * @throws IDCreateException if some problem creating the new IServiceID.
	 */
	public IServiceID createServiceID(Namespace namespace, String serviceType, String serviceName) throws IDCreateException;

	/**
	 * Create an IServiceID.  Creates an immutable IServiceID with a non-<code>null</code> {@link IServiceTypeID}
	 * and a potentially <code>null</code> service name. 
	 * @param namespace the Namespace instance to create the service ID with.  Must not be <code>null</code>.
	 * @param serviceType the service type to create the service ID with.  Must not be <code>null</code>.
	 * @return IServiceID created.  Will not be <code>null</code>.
	 * @throws IDCreateException if some problem creating the new IServiceID.
	 */
	public IServiceID createServiceID(Namespace namespace, String serviceType) throws IDCreateException;

}
