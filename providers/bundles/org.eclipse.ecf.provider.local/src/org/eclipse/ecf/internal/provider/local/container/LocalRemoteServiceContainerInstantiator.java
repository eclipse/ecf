/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.local.container;

import java.util.Dictionary;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.provider.local.identity.LocalID;
import org.eclipse.ecf.provider.local.identity.LocalNamespace;

public class LocalRemoteServiceContainerInstantiator extends
		BaseContainerInstantiator implements
		IRemoteServiceContainerInstantiator {

	public IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException {
		try {
			LocalID localID = null;
			if (parameters != null && parameters[0] instanceof LocalID)
				localID = (LocalID) parameters[0];
			else
				localID = (LocalID) IDFactory.getDefault().createID(LocalNamespace.NAME, parameters);
			return new LocalRemoteServiceContainer(localID);
		} catch (Exception e) {
			throw new ContainerCreateException("Could not create LocalRemoteServiceContainer", e); //$NON-NLS-1$
		}
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] { description.getName() };
	}

	public String[] getImportedConfigs(ContainerTypeDescription description,
			String[] exporterSupportedConfigs) {
		return new String[] { description.getName() };
	}

	public Dictionary getPropertiesForImportedConfigs(
			ContainerTypeDescription description, String[] importedConfigs,
			Dictionary exportedProperties) {
		return null;
	}

}
