/*******************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.internal.provider.r_osgi;

import java.util.Arrays;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

/**
 * The container instantiator creates new container instances of type
 * <i>ecf.r_osgi.peer</i> through the
 * <code>org.eclipse.ecf.ContainerFactory</code> extension point.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public final class R_OSGiContainerInstantiator implements IContainerInstantiator {

	public static final String[] r_OSGiIntents = {"passByValue", "exactlyOnce", "ordered",}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * creates a new container instance.
	 * 
	 * @param description
	 *            the container type description.
	 * @param parameters
	 *            the parameter passed to the container constructor.
	 * @return the factored container instance.
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription,
	 *      java.lang.Object[])
	 */
	public IContainer createInstance(final ContainerTypeDescription description, final Object[] parameters) throws ContainerCreateException {
		try {
			if (parameters.length == 1 && parameters[0] instanceof ID) {
				return new R_OSGiRemoteServiceContainer(Activator.getDefault().getRemoteOSGiService(), (ID) parameters[0]);
			}
			throw new ContainerCreateException("Unsupported arguments " //$NON-NLS-1$
					+ Arrays.asList(parameters));
		} catch (IDCreateException e) {
			throw new ContainerCreateException(e);
		}
	}

	/**
	 * get the adapter types that are supported by this container instantiator.
	 * 
	 * @param description
	 *            the container type description.
	 * @return a string array of the supported classes to which the factored
	 *         containers can provide adapters.
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedAdapterTypes(final ContainerTypeDescription description) {
		return new String[] {IRemoteServiceContainerAdapter.class.getName(), IContainer.class.getName()};
	}

	/**
	 * get the supported parameter types which the constructor of the container
	 * takes.
	 * 
	 * @param description
	 *            the container type description.
	 * @return an array of class arrays. Each array entry describes one sequence
	 *         of supported parameter.
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public Class[][] getSupportedParameterTypes(final ContainerTypeDescription description) {
		return new Class[][] {new Class[] {}, new Class[] {ID.class}};
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return r_OSGiIntents;
	}

}