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
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.internal.provider.discovery.Messages;

public class CompositeDiscoveryContainerInstantiator implements IContainerInstantiator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription,
	 *      java.lang.Object[])
	 */
	public IContainer createInstance(final ContainerTypeDescription description, final Object[] parameters) throws ContainerCreateException {

		try {
			final IContainerFactory factory = ContainerFactory.getDefault();
			final List containers = new ArrayList();
			final List list = factory.getDescriptions();
			for (final Iterator itr = list.iterator(); itr.hasNext();) {
				final ContainerTypeDescription ctd = (ContainerTypeDescription) itr.next();
				final String name = ctd.getName();
				if (!name.equals("ecf.discovery.*") //$NON-NLS-1$
						&& name.startsWith("ecf.discovery.")) { //$NON-NLS-1$
					final IContainer container = factory.createContainer(ctd.getName());
					containers.add(container);
				}
			}
			return new CompositeDiscoveryContainer(containers);
		} catch (final IDCreateException e) {
			final ContainerCreateException excep = new ContainerCreateException(Messages.CompositeDiscoveryContainerInstantiator);
			excep.setStackTrace(e.getStackTrace());
			throw excep;
		} catch (final ContainerCreateException e) {
			final ContainerCreateException excep = new ContainerCreateException(Messages.CompositeDiscoveryContainerInstantiator);
			excep.setStackTrace(e.getStackTrace());
			throw excep;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedAdapterTypes(final ContainerTypeDescription description) {
		return new String[] {IDiscoveryAdvertiser.class.getName(), IDiscoveryLocator.class.getName()};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public Class[][] getSupportedParameterTypes(final ContainerTypeDescription description) {
		return new Class[0][0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedIntents(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}

}
