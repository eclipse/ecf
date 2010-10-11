/*******************************************************************************
 * Copyright (c) 2010 Naumen. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Pavel Samolisov - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.remoteservice.rpc;

import java.util.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.remoteservice.rpc.client.RPCClientContainer;
import org.eclipse.ecf.remoteservice.rpc.identity.RPCID;
import org.eclipse.ecf.remoteservice.rpc.identity.RPCNamespace;

public class RPCClientContainerInstantiator extends BaseContainerInstantiator implements
		IRemoteServiceContainerInstantiator {

	private static final String RPC_CONTAINER_TYPE = "ecf.xmlrpc.client"; //$NON-NLS-1$

	public IContainer createInstance(ContainerTypeDescription description, Object[] parameters)
			throws ContainerCreateException {
		try {
			RPCID ID = null;
			if (parameters != null && parameters[0] instanceof RPCID)
				ID = (RPCID) parameters[0];
			else
				ID = (RPCID) IDFactory.getDefault().createID(RPCNamespace.NAME, parameters);
			return new RPCClientContainer(ID);
		} catch (Exception e) {
			throw new ContainerCreateException(Messages.PRC_COULD_NOT_CREATE_CONTAINER, e);
		}
	}

	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		return getInterfacesAndAdaptersForClass(RPCClientContainer.class);
	}

	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		RPCNamespace namespace = (RPCNamespace) IDFactory.getDefault().getNamespaceByName(RPCNamespace.NAME);
		return namespace.getSupportedParameterTypes();
	}

	public String[] getImportedConfigs(ContainerTypeDescription description, String[] exporterSupportedConfigs) {
		if (RPC_CONTAINER_TYPE.equals(description.getName())) {
			List supportedConfigs = Arrays.asList(exporterSupportedConfigs);
			if (supportedConfigs.contains(RPC_CONTAINER_TYPE))
				return new String[] {RPC_CONTAINER_TYPE};
		}
		return null;
	}

	public Dictionary getPropertiesForImportedConfigs(ContainerTypeDescription description, String[] importedConfigs,
			Dictionary exportedProperties) {
		return null;
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] {RPC_CONTAINER_TYPE};
	}
}
