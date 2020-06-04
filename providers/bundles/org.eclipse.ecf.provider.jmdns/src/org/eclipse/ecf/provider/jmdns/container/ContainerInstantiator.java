/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.jmdns.container;

import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;

public class ContainerInstantiator implements IContainerInstantiator {

	/**
	 * @since 4.3
	 */
	public static final String JMDNS_CONTAINER_NAME = "ecf.container.jmdns";
	/**
	 * @since 4.3
	 */
	public static final String JMDNS_LOCATOR_NAME = "ecf.container.jmdns.locator";
	/**
	 * @since 4.3
	 */
	public static final String JMDNS_ADVERTISER_NAME = "ecf.container.jmdns.advertiser";

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
	 */
	public IContainer createInstance(final ContainerTypeDescription description, final Object[] args) throws ContainerCreateException {
		try {
			return new JMDNSDiscoveryContainer();
		} catch (final IDCreateException e) {
			final ContainerCreateException excep = new ContainerCreateException("Jmdns container create failed", e); //$NON-NLS-1$
			excep.setStackTrace(e.getStackTrace());
			throw excep;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedAdapterTypes(final ContainerTypeDescription description) {
		return new String[] {IDiscoveryContainerAdapter.class.getName()};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public Class[][] getSupportedParameterTypes(final ContainerTypeDescription description) {
		return new Class[0][0];
	}

	/** 
	 * @since 3.0
	 */
	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}

}
