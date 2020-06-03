/****************************************************************************
 * Copyright (c) 2009 Versant Corp and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.discovery.ui.userinput;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;

public class UserInputNamespace extends Namespace {

	public static final String NAME = "ecf.namespace.UserInputNamespace"; //$NON-NLS-1$
	private static final long serialVersionUID = 607013788248925596L;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object[])
	 */
	public ID createInstance(Object[] parameters) throws IDCreateException {
		if(parameters == null || parameters.length == 0 || parameters.length > 2) {
			throw new IDCreateException(Messages.UserInputNameSpace_INVALID_PARAMS);
		} else{
			throw new IDCreateException(Messages.UserInputNameSpace_INVALID_PARAMS);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
	public String getScheme() {
		return "userinput"; //$NON-NLS-1$
	}
}
