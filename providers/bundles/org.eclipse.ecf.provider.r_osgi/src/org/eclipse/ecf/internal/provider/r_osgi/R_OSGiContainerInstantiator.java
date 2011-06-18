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

import ch.ethz.iks.r_osgi.RemoteOSGiService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.provider.r_osgi.identity.R_OSGiID;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

/**
 * The container instantiator creates new container instances of type
 * <i>ecf.r_osgi.peer</i> through the
 * <code>org.eclipse.ecf.ContainerFactory</code> extension point.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public final class R_OSGiContainerInstantiator implements IContainerInstantiator, IRemoteServiceContainerInstantiator {

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
			final RemoteOSGiService remoteOSGiService = Activator.getDefault().getRemoteOSGiService();
			if (parameters == null) {
				//TODO factor localHost and protocol out?
				final String localHost = InetAddress.getLocalHost().getCanonicalHostName();
				final String protocol = "r-osgi"; //$NON-NLS-1$

				final int port = remoteOSGiService.getListeningPort(protocol);
				final ID containerID = new R_OSGiID(protocol + "://" + localHost + ":" + port); //$NON-NLS-1$ //$NON-NLS-2$
				return new R_OSGiRemoteServiceContainer(remoteOSGiService, containerID);
			} else if (parameters.length > 0) {
				if (parameters[0] instanceof ID) {
					return new R_OSGiRemoteServiceContainer(remoteOSGiService, (ID) parameters[0]);
				} else if (parameters[0] instanceof String) {
					return new R_OSGiRemoteServiceContainer(remoteOSGiService, new R_OSGiID((String) parameters[0]));
				}
			}
			throw new ContainerCreateException("Unsupported arguments " //$NON-NLS-1$
					+ Arrays.asList(parameters));
		} catch (IDCreateException e) {
			throw new ContainerCreateException(e);
		} catch (UnknownHostException e) {
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

	private static final String ROSGI_CONFIG = "ecf.r_osgi.peer"; //$NON-NLS-1$

	private static final String[] ROSGI_CONFIGS = new String[] {ROSGI_CONFIG};

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return ROSGI_CONFIGS;
	}

	public String[] getImportedConfigs(ContainerTypeDescription description, String[] exporterSupportedConfigs) {
		if (exporterSupportedConfigs == null)
			return null;
		List supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		if (supportedConfigs.contains(ROSGI_CONFIG))
			return ROSGI_CONFIGS;
		return null;
	}

	public Dictionary getPropertiesForImportedConfigs(ContainerTypeDescription description, String[] importedConfigs, Dictionary exportedProperties) {
		return null;
	}

}
