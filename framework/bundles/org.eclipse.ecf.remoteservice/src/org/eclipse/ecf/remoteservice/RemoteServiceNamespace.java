/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.core.identity.*;

/**
 * @since 8.3
 */
public class RemoteServiceNamespace extends Namespace {

	private static final long serialVersionUID = 5697857375725279019L;

	private static final String REMOTE_SERVICE_SCHEME = "remoteservice"; //$NON-NLS-1$

	public static final String NAME = "ecf.namespace.remoteservice"; //$NON-NLS-1$

	public RemoteServiceNamespace() {
		// nothing
	}

	public RemoteServiceNamespace(String name, String desc) {
		super(name, desc);
	}

	public ID createInstance(Object[] parameters) throws IDCreateException {
		if (parameters == null || parameters.length != 2)
			throw new IDCreateException("Parameters incorrect for remote ID creation"); //$NON-NLS-1$
		try {
			return new RemoteServiceID(this, (ID) parameters[0], ((Long) parameters[1]).longValue());
		} catch (Exception e) {
			throw new IDCreateException("Exception creating remoteID", e); //$NON-NLS-1$
		}
	}

	public String getScheme() {
		return REMOTE_SERVICE_SCHEME;
	}

	/**
	 * @since 8.12
	 */
	@Override
	public Class<?>[][] getSupportedParameterTypes() {
		return new Class<?>[][] {{String.class, Long.class}};
	}
}
