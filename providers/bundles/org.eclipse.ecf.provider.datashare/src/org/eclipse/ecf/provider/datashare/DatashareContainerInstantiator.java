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
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;

public class DatashareContainerInstantiator implements IContainerInstantiator {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
	 */
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		String[] argDefaults = description.getArgDefaults();
		try {
			ID newID = null;
			if (args != null && args.length != 0) {
				newID = getIDFromArg(args[0]);
			} else if (argDefaults != null && argDefaults.length != 0) {
				newID = getIDFromArg(description.getArgDefaults()[0]);
			} else {
				newID = IDFactory.getDefault().createGUID();
			}
			return new DatashareContainer(new DatashareContainerConfig(newID));
		} catch (IDCreateException e) {
			throw new ContainerCreateException(
					"Exception creating ID for container " + description, e);
		}
	}

	protected ID getIDFromArg(Object arg) throws IDCreateException {
		if (arg instanceof ID)
			return (ID) arg;
		if (arg instanceof String) {
			String val = (String) arg;
			if (val.equals("")) { //$NON-NLS-1$
				return IDFactory.getDefault().createGUID();
			} else
				return IDFactory.getDefault().createStringID((String) arg);
		} else if (arg instanceof Integer) {
			return IDFactory.getDefault()
					.createGUID(((Integer) arg).intValue());
		} else
			return IDFactory.getDefault().createGUID();
	}

}
