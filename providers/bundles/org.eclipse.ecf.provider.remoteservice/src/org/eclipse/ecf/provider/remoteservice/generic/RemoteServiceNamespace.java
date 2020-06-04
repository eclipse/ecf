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
package org.eclipse.ecf.provider.remoteservice.generic;

import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.remoteservice.RemoteServiceID;

/**
 * @since 3.0
 */
public class RemoteServiceNamespace extends Namespace {

	private static final long serialVersionUID = 1389616654436118037L;
	private static final String REMOTE_SERVICE_SCHEME = "remoteservice"; //$NON-NLS-1$

	public static final String NAME = "ecf.namespace.generic.remoteservice"; //$NON-NLS-1$

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

	@Override
	public Class<?>[][] getSupportedParameterTypes() {
		return new Class<?>[][] {{String.class, Long.class}};
	}
}
