/*******************************************************************************
 * Copyright (c) 2010 Naumen. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Pavel Samolisov - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservice.rpc.client;

import java.io.NotSerializableException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.client.*;
import org.eclipse.ecf.remoteservice.rpc.identity.RpcId;
import org.eclipse.ecf.remoteservice.rpc.identity.RpcNamespace;

/**
 * A container for XML-RPC services. 
 */
public class RpcClientContainer extends AbstractClientContainer implements IRemoteServiceClientContainerAdapter {

	public RpcClientContainer(RpcId id) {
		super(id);
	}

	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(RpcNamespace.NAME);
	}

	protected IRemoteService createRemoteService(RemoteServiceClientRegistration registration) {
		IRemoteService service = null;
		try {
			service = new RpcClientService(this, registration);
		} catch (ECFException e) {
			logException(e.getMessage(), e);
		}

		return service;
	}

	protected String prepareEndpointAddress(IRemoteCall call, IRemoteCallable callable) {
		// For XML-RPC, endpoint == resource.path
		return callable.getResourcePath();
	}

	/**
	 * All parameters will be serialized in the Apache XML-RPC library. We shouldn't serialize any parameters. 
	 * 
	 * @return the parameter
	 * @see IRemoteCallParameterSerializer#serializeParameter(String, IRemoteCall, IRemoteCallable, IRemoteCallParameter, Object)
	 * @since 4.1
	 */
	protected IRemoteCallParameter serializeParameter(String uri, IRemoteCall call, IRemoteCallable callable,
			final IRemoteCallParameter defaultParameter, final Object parameterValue) throws NotSerializableException {
		// Just return a parameter		
		return new RemoteCallParameter(defaultParameter.getName(), parameterValue == null ? defaultParameter.getValue()
				: parameterValue);
	}

	public boolean setRemoteServiceCallPolicy(IRemoteServiceCallPolicy policy) {
		// By default, XML-RPC client cannot set the call policy, so
		// return false
		return false;
	}
}
