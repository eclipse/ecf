/****************************************************************************
 * Copyright (c) 2006, 2007 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.msn;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;

public class MSNContainerInstantiator implements IContainerInstantiator {

	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		try {
			return new MSNContainer();
		} catch (IDCreateException e) {
			throw new ContainerCreateException(e);
		}
	}

	public String[] getSupportedAdapterTypes(
			ContainerTypeDescription description) {
		return new String[] { "org.eclipse.ecf.presence.IPresenceContainerAdapter" }; //$NON-NLS-1$
	}

	public Class[][] getSupportedParameterTypes(
			ContainerTypeDescription description) {
		return null;
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}

}
