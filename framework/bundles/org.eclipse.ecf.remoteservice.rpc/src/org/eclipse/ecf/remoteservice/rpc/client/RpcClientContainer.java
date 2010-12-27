/*******************************************************************************
 * Copyright (c) 2010 Naumen. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Pavel Samolisov - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservice.rpc.client;

import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
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
		// TODO
		return null;
	}

	protected String prepareEndpointAddress(IRemoteCall call, IRemoteCallable callable) {
		// TODO
		return callable.getResourcePath();
	}

	public boolean setRemoteServiceCallPolicy(IRemoteServiceCallPolicy policy) {
		// By default, rpc client's cannot set the call policy, so
		// return false
		return false;
	}

}
