/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;

public class RemoteServiceImpl implements IRemoteService, InvocationHandler {

	protected static final long DEFAULT_TIMEOUT = 30000;

	protected RemoteServiceRegistrationImpl registration = null;

	protected RegistrySharedObject sharedObject = null;

	public RemoteServiceImpl(RegistrySharedObject sharedObject,
			RemoteServiceRegistrationImpl registration) {
		this.sharedObject = sharedObject;
		this.registration = registration;
	}

	public void callAsynch(IRemoteCall call, IRemoteCallListener listener) {
		sharedObject.sendCallRequestWithListener(registration, call, listener);
	}

	public Object callSynch(IRemoteCall call) throws ECFException {
		return sharedObject.callSynch(registration, call);
	}

	public void fireAsynch(IRemoteCall call) throws ECFException {
		sharedObject.sendFireRequest(registration, call);
	}

	public Object getProxy() throws ECFException {
		Object proxy;
		try {
			// Get clazz from reference
			final RemoteServiceReferenceImpl reference = (RemoteServiceReferenceImpl) registration
					.getReference();
			final String clazz = reference.getRemoteClass();
			final Class loadedClass = Class.forName(clazz);
			proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(),
					new Class[] { loadedClass }, this);
		} catch (final Exception e) {
			throw new ECFException(
					"Exception creating proxy for remote service", e);
		}
		return proxy;
	}

	public Object invoke(Object proxy, final Method method, final Object[] args)
			throws Throwable {
		return this.callSynch(new IRemoteCall() {

			public String getMethod() {
				return method.getName();
			}

			public Object[] getParameters() {
				return args;
			}

			public long getTimeout() {
				return DEFAULT_TIMEOUT;
			}
		});
	}

}
