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

package org.eclipse.ecf.internal.examples.provider.trivial.container;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;

/**
 *
 */
public class TrivialContainerInstantiator extends BaseContainerInstantiator {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.BaseContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
	 */
	@Override
	public IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException {
		try {
			return new TrivialContainer();
		} catch (IDCreateException e) {
			throw new ContainerCreateException("Exception creating trivial container", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.BaseContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	@Override
	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		// TODO Return String [] with adapter types supported for the given description
		return super.getSupportedAdapterTypes(description);
	}
}
