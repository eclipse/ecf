/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.Serializable;

import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.remoteservice.IRemoteCall;

public class RemoteCallImpl  extends SharedObjectMsg implements IRemoteCall, Serializable{
	
	private static final long serialVersionUID = 1L;

	private static final long DEFAULT_REMOTE_CALL_TIMEOUT = 30000;

	long timeout = DEFAULT_REMOTE_CALL_TIMEOUT;
	
	public static RemoteCallImpl createRemoteCall(String clazz, String method, Object [] parameters, long timeout) {
		return new RemoteCallImpl(clazz,method,parameters,timeout);
	}
	public static RemoteCallImpl createRemoteCall(String clazz, String method, Object [] parameters) {
		return RemoteCallImpl.createRemoteCall(clazz,method,parameters,DEFAULT_REMOTE_CALL_TIMEOUT);
	}

	public static RemoteCallImpl createRemoteCall(String clazz, String method) {
		return RemoteCallImpl.createRemoteCall(clazz,method,null,DEFAULT_REMOTE_CALL_TIMEOUT);
	}

	protected RemoteCallImpl(String clazz, String method, Object [] parameters, long timeout) {
		super(clazz,method,parameters);
		this.timeout = timeout;
	}
	private RemoteCallImpl() {
		super();
	}
	public long getTimeout() {
		return timeout;
	}

}
