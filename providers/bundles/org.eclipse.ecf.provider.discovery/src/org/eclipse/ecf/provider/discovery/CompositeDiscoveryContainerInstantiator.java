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

import java.util.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.internal.provider.discovery.Messages;

public class CompositeDiscoveryContainerInstantiator implements IContainerInstantiator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription,
	 *      java.lang.Object[])
	 */
	public IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException {

		try {
			IContainerFactory factory = ContainerFactory.getDefault();
			List containers = new ArrayList();
			List list = factory.getDescriptions();
			for (Iterator itr = list.iterator(); itr.hasNext();) {
				ContainerTypeDescription ctd = (ContainerTypeDescription) itr.next();
				String name = ctd.getName();
				if (!name.equals("ecf.discovery.*") //$NON-NLS-1$
						&& name.startsWith("ecf.discovery.")) { //$NON-NLS-1$
					IContainer container = factory.createContainer(ctd.getName());
					containers.add(container);
				}
			}
			return new CompositeDiscoveryContainer(containers);
		} catch (IDCreateException e) {
			ContainerCreateException excep = new ContainerCreateException(Messages.CompositeDiscoveryContainerInstantiator);
			excep.setStackTrace(e.getStackTrace());
			throw excep;
		} catch (ContainerCreateException e) {
			ContainerCreateException excep = new ContainerCreateException(Messages.CompositeDiscoveryContainerInstantiator);
			excep.setStackTrace(e.getStackTrace());
			throw excep;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		return new String[] {IDiscoveryContainerAdapter.class.getName()};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		return new Class[0][0];
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}

}
