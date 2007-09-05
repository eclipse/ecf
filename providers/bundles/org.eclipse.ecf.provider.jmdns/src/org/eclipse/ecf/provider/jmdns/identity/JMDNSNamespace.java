/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.jmdns.identity;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.provider.jmdns.Messages;

public class JMDNSNamespace extends Namespace {

	private static final long serialVersionUID = -7220857203720337921L;

	private static final String JMDNS_SCHEME = "jmdns"; //$NON-NLS-1$

	public static final String NAME = "ecf.namespace.jmdns"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object[])
	*/
	public ID createInstance(Object[] parameters) throws IDCreateException {
		if (parameters == null || parameters.length < 1 || parameters.length > 2) {
			throw new IDCreateException(Messages.JMDNSNamespace_EXCEPTION_ID_WRONG_PARAM_COUNT);
		}
		String type = null;
		try {
			type = (String) parameters[0];
			if (type == null || type.equals("")) //$NON-NLS-1$
				throw new IDCreateException(Messages.JMDNSNamespace_EXCEPTION_ID_CREATE_SERVICE_TYPE_CANNOT_BE_EMPTY);
		} catch (final ClassCastException e) {
			throw new IDCreateException(Messages.JMDNSNamespace_EXCEPTION_TYPE_PARAM_NOT_STRING);
		}
		final JMDNSServiceTypeID stid = new JMDNSServiceTypeID(this, type);

		String name = null;
		if (parameters.length > 1) {
			try {
				name = (String) parameters[1];
			} catch (final ClassCastException e) {
				throw new IDCreateException(Messages.JMDNSNamespace_EXCEPTION_ID_PARAM_2_WRONG_TYPE);
			}
		}
		return new JMDNSServiceID(this, stid, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
	public String getScheme() {
		return JMDNS_SCHEME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedParameterTypesForCreateInstance()
	 */
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { {String.class}, {String.class, String.class}};
	}
}
