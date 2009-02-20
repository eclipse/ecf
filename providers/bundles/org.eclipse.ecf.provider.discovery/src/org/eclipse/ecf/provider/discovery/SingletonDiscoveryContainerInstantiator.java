/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.discovery;

import java.util.Iterator;
import java.util.List;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;

/**
 * 
 */
public class SingletonDiscoveryContainerInstantiator implements IContainerInstantiator {

	private static IContainer INSTANCE;

	private static synchronized IContainer getInstance(String containerName) throws ContainerCreateException {
		if (INSTANCE == null) {
			IContainerFactory factory = ContainerFactory.getDefault();
			List list = factory.getDescriptions();
			for (Iterator itr = list.iterator(); itr.hasNext();) {
				ContainerTypeDescription ctd = (ContainerTypeDescription) itr.next();
				String name = ctd.getName();
				if (name.equals(containerName)) {
					IContainer createContainer = factory.createContainer(ctd.getName());
					INSTANCE = new SingletonDiscoveryContainer(createContainer);
					return INSTANCE;
				}
			}
			if (INSTANCE == null) {
				throw new ContainerCreateException("Unknown Container Name"); //$NON-NLS-1$
			}
		}
		return INSTANCE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
	 */
	public IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException {
		if (parameters != null && parameters.length == 1 && parameters[0] instanceof String) {
			String containerName = (String) parameters[0];
			return getInstance(containerName);
		}
		throw new ContainerCreateException("Missing parameter"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		return new String[] {IDiscoveryContainerAdapter.class.getName()};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		return new Class[][] {{String.class}};
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}
}
