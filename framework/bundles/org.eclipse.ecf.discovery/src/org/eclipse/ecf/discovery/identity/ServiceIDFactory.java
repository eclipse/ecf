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

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.internal.discovery.Messages;

/**
 * ServiceIDFactory implementation.
 */
public class ServiceIDFactory implements IServiceIDFactory {

	private static final IServiceIDFactory instance = new ServiceIDFactory();

	public static IServiceIDFactory getDefault() {
		return instance;
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.discovery.identity.IServiceIDFactory#createServiceTypeID(org.eclipse.ecf.core.identity.Namespace,
	 *      java.lang.String[], java.lang.String[], java.lang.String[],
	 *      java.lang.String)
	 */
	public IServiceTypeID createServiceTypeID(Namespace namespace, String serviceType) {
		return createServiceTypeID(namespace, new String[] { serviceType });
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.discovery.identity.IServiceIDFactory#createServiceTypeID(org.eclipse.ecf.core.identity.Namespace,
	 *      java.lang.String[], java.lang.String[], java.lang.String[],
	 *      java.lang.String)
	 */
	public IServiceTypeID createServiceTypeID(Namespace namespace, String[] serviceType) {
		return createServiceTypeID(namespace, serviceType, IServiceTypeID.DEFAULT_SCOPE, IServiceTypeID.DEFAULT_PROTO,
				IServiceTypeID.DEFAULT_NA);
	}
	
	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.discovery.identity.IServiceIDFactory#createServiceTypeID(org.eclipse.ecf.core.identity.Namespace, java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String)
	 */
	public IServiceTypeID createServiceTypeID(Namespace namespace, String[] serviceType, String[] scopes, String[] protocols, String namingAuthority) throws IDCreateException {
		try {
			IServiceTypeID aServiceType = new ServiceTypeID(namespace, serviceType, scopes, protocols, namingAuthority);
			return (IServiceTypeID) IDFactory.getDefault().createID(namespace, new Object[] {aServiceType});
		} catch (AssertionFailedException e) {
			throw new IDCreateException(Messages.ServiceTypeID_EXCEPTION_SERVICE_TYPE_ID_NOT_PARSEABLE, e);
		}
	}

	/**
	 * @see org.eclipse.ecf.discovery.identity.IServiceIDFactory#createServiceTypeID(org.eclipse.ecf.core.identity.Namespace, java.lang.String[], java.lang.String[])
	 * @since 3.0
	 */
	public IServiceTypeID createServiceTypeID(Namespace namespace, String[] serviceType, String[] protocols) throws IDCreateException {
		return this.createServiceTypeID(namespace, serviceType, IServiceTypeID.DEFAULT_SCOPE, protocols, IServiceTypeID.DEFAULT_NA);
	}

	/**
	 * @see org.eclipse.ecf.discovery.identity.IServiceIDFactory#createServiceTypeID(org.eclipse.ecf.core.identity.Namespace, org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 * @since 3.0
	 */
	public IServiceTypeID createServiceTypeID(Namespace namespace, IServiceTypeID aServiceTypeId) throws IDCreateException {
		return (IServiceTypeID) IDFactory.getDefault().createID(namespace, new Object[] {aServiceTypeId});
	}
}
