/*******************************************************************************
  * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.lang.reflect.*;
import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.core.jobs.JobsExecutor;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.provider.remoteservice.Messages;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.ServiceException;

public class RemoteServiceImpl implements IRemoteService, InvocationHandler {

	protected static final long DEFAULT_TIMEOUT = new Long(System.getProperty("ecf.remotecall.timeout", "30000")).longValue(); //$NON-NLS-1$ //$NON-NLS-2$

	protected RemoteServiceRegistrationImpl registration = null;

	protected RegistrySharedObject sharedObject = null;

	static final Object[] EMPTY_PARAMETERS = new Object[0];

	public RemoteServiceImpl(RegistrySharedObject sharedObject, RemoteServiceRegistrationImpl registration) {
		this.sharedObject = sharedObject;
		this.registration = registration;
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.remoteservice.IRemoteService#callAsync(org.eclipse.ecf.remoteservice.IRemoteCall, org.eclipse.ecf.remoteservice.IRemoteCallListener)
	 */
	public void callAsync(IRemoteCall call, IRemoteCallListener listener) {
		sharedObject.sendCallRequestWithListener(registration, call, listener);
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.remoteservice.IRemoteService#callAsync(org.eclipse.ecf.remoteservice.IRemoteCall)
	 */
	public IFuture callAsync(final IRemoteCall call) {
		JobsExecutor executor = new JobsExecutor(NLS.bind("callAsynch({0}", call.getMethod())); //$NON-NLS-1$
		return executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Exception {
				return callSync(call);
			}
		}, null);
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.remoteservice.IRemoteService#callSync(org.eclipse.ecf.remoteservice.IRemoteCall)
	 */
	public Object callSync(IRemoteCall call) throws ECFException {
		return sharedObject.callSynch(registration, call);
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.remoteservice.IRemoteService#fireAsync(org.eclipse.ecf.remoteservice.IRemoteCall)
	 */
	public void fireAsync(IRemoteCall call) throws ECFException {
		sharedObject.sendFireRequest(registration, call);
	}

	public Object getProxy() throws ECFException {
		Object proxy;
		try {
			// Get clazz from reference
			final String[] clazzes = registration.getClasses();
			List classes = new ArrayList();
			for (int i = 0; i < clazzes.length; i++) {
				Class c = Class.forName(clazzes[i]);
				classes.add(c);
				// check to see if async remote service proxy interface is defined
				Class asyncRemoteServiceProxyClass = findAsyncRemoteServiceProxyClass(c);
				if (asyncRemoteServiceProxyClass != null && asyncRemoteServiceProxyClass.isInterface())
					classes.add(asyncRemoteServiceProxyClass);
			}
			// add IRemoteServiceProxy interface to set of interfaces supported by this proxy
			classes.add(IRemoteServiceProxy.class);
			proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), (Class[]) classes.toArray(new Class[] {}), this);
		} catch (final Exception e) {
			throw new ECFException(Messages.RemoteServiceImpl_EXCEPTION_CREATING_PROXY, e);
		}
		return proxy;
	}

	private Class findAsyncRemoteServiceProxyClass(Class c) {
		String sourceName = c.getName();
		String asyncRemoteServiceProxyClassname = sourceName + IAsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX;
		try {
			return Class.forName(asyncRemoteServiceProxyClassname);
		} catch (Exception t) {
			return null;
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}

	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		// methods declared by Object
		try {
			if (method.getName().equals("toString")) { //$NON-NLS-1$
				final String[] clazzes = registration.getClasses();
				String proxyClass = (clazzes.length == 1) ? clazzes[0] : Arrays.asList(clazzes).toString();
				return proxyClass + ".proxy@" + registration.getID(); //$NON-NLS-1$
			} else if (method.getName().equals("hashCode")) { //$NON-NLS-1$
				return new Integer(hashCode());
			} else if (method.getName().equals("equals")) { //$NON-NLS-1$
				if (args == null || args.length == 0)
					return Boolean.FALSE;
				try {
					return new Boolean(Proxy.getInvocationHandler(args[0]).equals(this));
				} catch (IllegalArgumentException e) {
					return Boolean.FALSE;
				}
				// This handles the use of IRemoteServiceProxy.getRemoteService method
			} else if (method.getName().equals("getRemoteService")) { //$NON-NLS-1$
				return this;
			} else if (method.getName().equals("getRemoteServiceReference")) { //$NON-NLS-1$
				return registration.getReference();
			}
			// If the method's class is a subclass of IAsyncRemoteServiceProxy, then we assume
			// that the methods are intended to be invoked asynchronously
			if (Arrays.asList(method.getDeclaringClass().getInterfaces()).contains(IAsyncRemoteServiceProxy.class))
				return checkAndCallAsync(method, args);
			// else call synchronously/block and return result
			return this.callSync(new IRemoteCall() {

				public String getMethod() {
					return method.getName();
				}

				public Object[] getParameters() {
					return (args == null) ? EMPTY_PARAMETERS : args;
				}

				public long getTimeout() {
					return DEFAULT_TIMEOUT;
				}
			});
		} catch (Throwable t) {
			// rethrow as service exception
			throw new ServiceException("Service exception on remote service proxy rsid=" + registration.getID(), ServiceException.REMOTE, t); //$NON-NLS-1$
		}
	}

	private Object checkAndCallAsync(final Method method, final Object[] args) throws Throwable {
		IRemoteCallListener listener = verifyMethodAndArgs(method, args);
		String methodName = method.getName();
		final String invokeMethodName = methodName.endsWith(IAsyncRemoteServiceProxy.ASYNC_METHOD_SUFFIX) ? methodName.substring(0, methodName.length() - IAsyncRemoteServiceProxy.ASYNC_METHOD_SUFFIX.length()) : methodName;
		IRemoteCall call = new IRemoteCall() {
			public String getMethod() {
				return invokeMethodName;
			}

			public Object[] getParameters() {
				return args;
			}

			public long getTimeout() {
				return DEFAULT_TIMEOUT;
			}
		};
		if (listener == null)
			return callAsync(call);
		callAsync(call, listener);
		return null;
	}

	private IRemoteCallListener verifyMethodAndArgs(Method method, Object[] args) {
		Class returnType = method.getReturnType();
		// If the return type is declared to be *anything* except an IFuture, then 
		// we are expecting the last argument to be an IRemoteCallListener
		if (!returnType.equals(IFuture.class)) {
			// If the provided args do *not* include an IRemoteCallListener then we have a problem
			if (args == null || args.length == 0)
				throw new IllegalArgumentException("Async calls must include a IRemoteCallListener instance as the last argument"); //$NON-NLS-1$
			Object lastArg = args[args.length - 1];
			// Again if the last are is not an instance of IRemoteCallListener then
			// we have a problem
			if (!(lastArg instanceof IRemoteCallListener))
				throw new IllegalArgumentException("Last argument must be an instance of IRemoteCallListener"); //$NON-NLS-1$
			return (IRemoteCallListener) lastArg;
		}
		return null;
	}

}
