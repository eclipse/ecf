/*******************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.discovery;

import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

public class DiscoveryNamespace extends Namespace {

	private static final long serialVersionUID = 6474091408790223505L;
	public static final String NAME = "ecf.namespace.discovery"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object
	 * [])
	 */
	public ID createInstance(Object[] parameters) throws IDCreateException {
		if (parameters != null && parameters.length == 1
				&& parameters[0] instanceof IServiceTypeID) {
			return (ID) parameters[0];
		}
		throw new IDCreateException("Parameters must be of type IServiceTypeID"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
	public String getScheme() {
		return "discovery"; //$NON-NLS-1$
	}
}
