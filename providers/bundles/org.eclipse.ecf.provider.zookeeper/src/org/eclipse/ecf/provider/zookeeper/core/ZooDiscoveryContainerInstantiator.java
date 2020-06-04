/****************************************************************************
 * Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 *  Contributors:
 *    Wim Jongman - initial API and implementation 
 *    Ahmed Aadel - initial API and implementation     
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.zookeeper.core;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;

public class ZooDiscoveryContainerInstantiator implements
		IContainerInstantiator {

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
