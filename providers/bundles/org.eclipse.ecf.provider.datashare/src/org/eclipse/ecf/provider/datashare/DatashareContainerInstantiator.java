/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.datashare;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;

public class DatashareContainerInstantiator extends GenericContainerInstantiator {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
	 */
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		String[] argDefaults = description.getParameterDefaults();
		try {
			ID newID = null;
			if (args != null && args.length != 0) {
				newID = getIDFromArg(args[0]);
			} else if (argDefaults != null && argDefaults.length != 0) {
				newID = getIDFromArg(description.getParameterDefaults()[0]);
			} else {
				newID = IDFactory.getDefault().createGUID();
			}
			return new DatashareContainer(new DatashareContainerConfig(newID));
		} catch (IDCreateException e) {
			throw new ContainerCreateException(
					"Exception creating ID for container " + description, e);
		}
	}

}
