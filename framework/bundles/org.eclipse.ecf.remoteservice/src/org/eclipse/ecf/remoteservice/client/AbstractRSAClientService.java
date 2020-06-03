/****************************************************************************
 * Copyright (c) 2016 Composent, Inc. and others.
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
package org.eclipse.ecf.remoteservice.client;

import java.lang.reflect.Method;
import java.util.concurrent.*;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.osgi.framework.ServiceException;

/**
 * Abstract client remote service instance.   This class should be overridden to implement the abstract
 * invokeAsync, and invokeSync methods, which will be called when the proxy created is called by clients.
 * 
 * @since 8.9
 */
public abstract class AbstractRSAClientService extends AbstractClientService {

	public static class RSARemoteCall extends RemoteCall {

		private final Object proxy;
		private final Method reflectMethod;

		public RSARemoteCall(Object proxy, Method method, String methodName, Object[] parameters, long timeout) {
			super(methodName, parameters, timeout);
			this.reflectMethod = method;
			this.proxy = proxy;
		}

		public Method getReflectMethod() {
			return reflectMethod;
		}

		public Object getProxy() {
			return proxy;
		}
	}

	/**
	 * @param call the remote call to invoke
	 * @param callable the remote callable to invoke
	 * @return Object result of remote call
	 * @throws ECFException if invoke fails
	 */
	@Override
	protected Object invokeRemoteCall(IRemoteCall call, IRemoteCallable callable) throws ECFException {
		return null;
	}

	public AbstractRSAClientService(AbstractClientContainer container, RemoteServiceClientRegistration registration) {
		super(container, registration);
	}

	/**
	 * Invoke a remote call asynchronously.  This method should not block and should return either a {@link org.eclipse.equinox.concurrent.future.IFuture}, {@link java.util.concurrent.Future}, or {@link java.util.concurrent.CompletableFuture}
	 * or a
	 * CompletableFuture based upon the return type defined in the asynchronous service interface.
	 * 
	 * @param remoteCall the RSARemoteCall to use to make the asynchronous remote call.  Will not be <code>null</code>.
	 * @return Object.   Should return a non-null instance of {@link org.eclipse.equinox.concurrent.future.IFuture}, {@link java.util.concurrent.Future}, or {@link java.util.concurrent.CompletableFuture}
	 * @throws ECFException if async cannot be invoked
	 */
	protected Object invokeAsync(RSARemoteCall remoteCall) throws ECFException {
		return callFuture(remoteCall, remoteCall.getReflectMethod().getReturnType());
	}

	/**
	 * Invoke a remote call synchronously.  This method should block until a value may be returned, or the remote
	 * call has failed or timed out.
	 * 
	 * @param remoteCall the RSARemoteCall to synchronously invoke.  Will not be <code>null</code>.
	 * @return the result (of appropriate type)
	 * @throws ECFException if some exception occurred during invocation
	 */
	protected Object invokeSync(RSARemoteCall remoteCall) throws ECFException {
		if (remoteCall.getClass().isAssignableFrom(RSARemoteCall.class)) {
			Callable<Object> c = getSyncCallable(remoteCall);
			if (c == null)
				throw new ECFException("invokeSync failed on method=" + remoteCall.getMethod(), new NullPointerException("createSyncCallable() must not return null.  It's necessary for distribution provider to override createSyncCallable.")); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				return callSync(remoteCall, c);
			} catch (InterruptedException e) {
				throw new ECFException("invokeSync interrupted on method=" + remoteCall.getMethod(), e); //$NON-NLS-1$
			} catch (ExecutionException e) {
				throw new ECFException("invokeSync exception on method=" + remoteCall.getMethod(), e.getCause()); //$NON-NLS-1$
			} catch (TimeoutException e) {
				throw new ECFException("invokeSync timeout on method=" + remoteCall.getMethod(), e); //$NON-NLS-1$
			}
		}
		return super.invokeSync(remoteCall);
	}

	protected RSARemoteCall createRemoteCall(Object proxy, Method method, String methodName, Object[] parameters, long timeout) {
		return new RSARemoteCall(proxy, method, methodName, parameters, timeout);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			Object resultObject = invokeObject(proxy, method, args);
			if (resultObject != null)
				return resultObject;
			try {
				// If return is async type (Future, IFuture, CompletableFuture, CompletionStage)
				if (isReturnAsync(proxy, method, args)) {
					if (isInterfaceAsync(method.getDeclaringClass()) && isMethodAsync(method.getName()))
						return invokeAsync(createRemoteCall(proxy, method, getAsyncInvokeMethodName(method), args, getDefaultTimeout()));
					// If OSGI Async then invoke method directly
					if (isOSGIAsync())
						return invokeAsync(createRemoteCall(proxy, method, method.getName(), args, getDefaultTimeout()));
				}
			} catch (Throwable t) {
				handleProxyException("Exception invoking async method on remote service proxy=" + getRemoteServiceID(), t); //$NON-NLS-1$
			}

			final String callMethod = getCallMethodNameForProxyInvoke(method, args);
			final Object[] callParameters = getCallParametersForProxyInvoke(callMethod, method, args);
			final long callTimeout = getCallTimeoutForProxyInvoke(callMethod, method, args);
			return invokeSync(createRemoteCall(proxy, method, callMethod, callParameters, callTimeout));
		} catch (Throwable t) {
			if (t instanceof ServiceException)
				throw t;
			// rethrow as service exception
			throw new ServiceException("Service exception on remote service proxy rsid=" + getRemoteServiceID(), ServiceException.REMOTE, t); //$NON-NLS-1$
		}
	}

	@Override
	protected ExecutorService getFutureExecutorService(IRemoteCall call) {
		return super.getFutureExecutorService(call);
	}

	@Override
	public void callAsync(IRemoteCall call, IRemoteCallListener listener) {
		if (call instanceof RSARemoteCall)
			callAsyncWithTimeout(call, getAsyncCallable((RSARemoteCall) call), listener);
		else
			super.callAsync(call, listener);
	}

	@Override
	public Object callSync(IRemoteCall call) throws ECFException {
		if (call instanceof RSARemoteCall)
			try {
				return getSyncCallable((RSARemoteCall) call).call();
			} catch (Exception e) {
				throw new ECFException("Exception calling callable for method=" + call.getMethod(), e); //$NON-NLS-1$
			}
		return super.callSync(call);
	}

	/**
	 * @since 8.13
	 */
	protected ExecutorService getExecutorService() {
		return getFutureExecutorService(null);
	}

	/**
	 * @since 8.13
	 */
	protected Callable<IRemoteCallCompleteEvent> getAsyncCallable(final RSARemoteCall call) {
		throw new UnsupportedOperationException("distribution provider must override createAsyncCallable for service method=" + call.getMethod() + " class=" + call.getReflectMethod().getDeclaringClass()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @since 8.13
	 */
	protected Callable<Object> getSyncCallable(final RSARemoteCall call) {
		throw new UnsupportedOperationException("distribution provider must override createAsyncCallable for service method=" + call.getMethod() + " class=" + call.getReflectMethod().getDeclaringClass()); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
