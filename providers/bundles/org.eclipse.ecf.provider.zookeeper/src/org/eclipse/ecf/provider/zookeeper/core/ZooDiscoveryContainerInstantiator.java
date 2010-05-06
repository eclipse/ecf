/*******************************************************************************
 *  Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Ahmed Aadel - initial API and implementation     
 *******************************************************************************/
package org.eclipse.ecf.provider.zookeeper.core;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
public class ZooDiscoveryContainerInstantiator implements IContainerInstantiator {

	public static final String NAME = "ecf.discovery.zoodiscovery"; //$NON-NLS-1$

	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) {
		return ZooDiscoveryContainer.getSingleton();

	}

	public String[] getSupportedAdapterTypes(
			ContainerTypeDescription description) {
		if (description.getName().equals(NAME))
			return new String[] { IContainer.class.getName(),
					IDiscoveryAdvertiser.class.getName(),
					IDiscoveryLocator.class.getName() };
		return null;
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}

	public Class[][] getSupportedParameterTypes(
			ContainerTypeDescription description) {
		return new Class[][] { { String.class } };

	}
}
