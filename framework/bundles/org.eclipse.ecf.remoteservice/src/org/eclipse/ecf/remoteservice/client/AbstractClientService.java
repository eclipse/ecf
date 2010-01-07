/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import java.io.NotSerializableException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallStartEvent;
import org.eclipse.equinox.concurrent.future.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.ServiceException;

/**
 * @since 3.3
 */
public abstract class AbstractClientService implements IRemoteService, InvocationHandler {

	private long nextID = 0;
	protected RemoteServiceClientRegistration registration;
	protected AbstractClientContainer container;

	public AbstractClientService(AbstractClientContainer container, RemoteServiceClientRegistration registration) {
		this.container = container;
		Assert.isNotNull(container);
		this.registration = registration;
		Assert.isNotNull(this.registration);
	}

	public Object callSync(IRemoteCall call) throws ECFException {
		IRemoteCallable callable = getRegistration().lookupCallable(call);
		if (callable == null)
			throw new ECFException("Callable not found for call=" + call); //$NON-NLS-1$
		return invokeRemoteCall(call, callable);
	}

	public IFuture callAsync(final IRemoteCall call) {
		return callAsync(call, getRegistration().lookupCallable(call));
	}

	public void callAsync(IRemoteCall call, IRemoteCallListener listener) {
		callAsync(call, getRegistration().lookupCallable(call), listener);
	}

	public void fireAsync(IRemoteCall call) throws ECFException {
		IRemoteCallable callable = getRegistration().lookupCallable(call);
		if (callable == null)
			throw new ECFException("Remote callable not found"); //$NON-NLS-1$
		callAsync(call, callable);
	}

	public Object getProxy() throws ECFException {
		Object proxy;
		try {
			// Get clazz from reference
			final String[] clazzes = getRegistration().getClazzes();
			final Class[] cs = new Class[clazzes.length + 1];
			for (int i = 0; i < clazzes.length; i++)
				cs[i] = Class.forName(clazzes[i], true, this.getClass().getClassLoader());
			// add IRemoteServiceProxy interface to set of interfaces supported
			// by this proxy
			cs[clazzes.length] = IRemoteServiceProxy.class;
			proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), cs, this);
		} catch (final Exception e) {
			throw new ECFException(NLS.bind("Exception creating proxy rsid={0}", registration.getID()), e); //$NON-NLS-1$
		}
		return proxy;
	}

	protected Object[] getCallParametersForProxyInvoke(String callMethod, Method proxyMethod, Object[] args) {
		return args;
	}

	protected long getCallTimeoutForProxyInvoke(String callMethod, Method proxyMethod, Object[] args) {
		return IRemoteCall.DEFAULT_TIMEOUT;
	}

	protected String getCallMethodNameForProxyInvoke(Method method, Object[] args) {
		return RemoteServiceClientRegistration.getFQMethod(method.getDeclaringClass().getName(), method.getName());
	}

	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		// methods declared by Object
		RemoteServiceClientRegistration reg = getRegistration();
		Assert.isNotNull(reg);
		try {
			if (method.getName().equals("toString")) { //$NON-NLS-1$
				final String[] clazzes = reg.getClazzes();
				String proxyClass = (clazzes.length == 1) ? clazzes[0] : Arrays.asList(clazzes).toString();
				return proxyClass + ".proxy@" + reg.getID(); //$NON-NLS-1$
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
				// This handles the use of IRemoteServiceProxy.getRemoteService
				// method
			} else if (method.getName().equals("getRemoteService")) { //$NON-NLS-1$
				return this;
			} else if (method.getName().equals("getRemoteServiceReference")) { //$NON-NLS-1$
				return reg.getReference();
			}
			// So the method is a user-defined method for the proxy
			// 
			final String fqCallMethod = getCallMethodNameForProxyInvoke(method, args);
			final Object[] callParameters = getCallParametersForProxyInvoke(fqCallMethod, method, args);
			final long callTimeout = getCallTimeoutForProxyInvoke(fqCallMethod, method, args);
			final IRemoteCall remoteCall = new IRemoteCall() {

				public String getMethod() {
					return fqCallMethod;
				}

				public Object[] getParameters() {
					return callParameters;
				}

				public long getTimeout() {
					return callTimeout;
				}
			};
			// Now lookup callable
			IRemoteCallable callable = reg.lookupCallable(remoteCall);
			// If not found...we're finished
			if (callable == null)
				throw new ECFException("Callable not found for call=" + remoteCall); //$NON-NLS-1$
			return invokeRemoteCall(remoteCall, callable);
		} catch (Throwable t) {
			if (t instanceof ServiceException)
				throw (ServiceException) t;
			// else rethrow as service exception
			throw new ServiceException("Service exception on remote service proxy rsid=" + reg.getID(), ServiceException.REMOTE, t); //$NON-NLS-1$
		}
	}

	protected long getNextRequestID() {
		return nextID++;
	}

	protected void callAsync(IRemoteCall call, IRemoteCallable restClientCallable, IRemoteCallListener listener) {
		final AbstractExecutor executor = new ThreadsExecutor();
		executor.execute(new AsyncResult(call, restClientCallable, listener), null);
	}

	protected IFuture callAsync(final IRemoteCall call, final IRemoteCallable callable) {
		final AbstractExecutor executor = new ThreadsExecutor();
		return executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Exception {
				if (callable == null)
					throw new ECFException("Callable not found"); //$NON-NLS-1$
				return invokeRemoteCall(call, callable);
			}
		}, null);
	}

	/**
	 * inner class implementing the asynchronous result object. This
	 * implementation also provides the calling infrastructure.
	 */
	protected class AsyncResult implements IProgressRunnable {

		IRemoteCall call;
		// the remote call object.
		IRemoteCallable callable;
		// the callback listener, if provided.
		IRemoteCallListener listener;

		// the result of the call.
		Object result;
		// the exception, if any happened during the call.
		Throwable exception;

		// constructor
		public AsyncResult(final IRemoteCall call, final IRemoteCallable callable, final IRemoteCallListener listener) {
			this.call = call;
			this.callable = callable;
			this.listener = listener;
		}

		public Object run(IProgressMonitor monitor) throws Exception {
			Object r = null;
			Throwable e = null;

			final long reqID = getNextRequestID();

			if (listener != null) {
				listener.handleEvent(new IRemoteCallStartEvent() {
					public IRemoteCall getCall() {
						return call;
					}

					public IRemoteServiceReference getReference() {
						return getRegistration().getReference();
					}

					public long getRequestId() {
						return reqID;
					}
				});
			}

			try {
				if (callable == null)
					throw new ECFException(NLS.bind("Restcall not found for method={0}", call.getMethod())); //$NON-NLS-1$
				r = invokeRemoteCall(call, callable);
			} catch (Throwable t) {
				e = t;
			}

			synchronized (AsyncResult.this) {
				result = r;
				exception = e;
				AsyncResult.this.notify();
			}

			if (listener != null) {
				listener.handleEvent(new IRemoteCallCompleteEvent() {

					public Throwable getException() {
						return exception;
					}

					public Object getResponse() {
						return result;
					}

					public boolean hadException() {
						return exception != null;
					}

					public long getRequestId() {
						return reqID;
					}
				});
			}
			return null;
		}
	}

	protected AbstractClientContainer getClientContainer() {
		return container;
	}

	protected RemoteServiceClientRegistration getRegistration() {
		return registration;
	}

	protected String prepareURIForRequest(IRemoteCall call, IRemoteCallable callable) {
		return getClientContainer().prepareURIForRequest(call, callable);
	}

	protected IRemoteCallParameter[] prepareParametersForRequest(String uri, IRemoteCall call, IRemoteCallable callable) throws NotSerializableException {
		return getClientContainer().prepareParametersForRequest(uri, call, callable);
	}

	protected Object processResponse(String uri, IRemoteCall call, IRemoteCallable callable, Map responseHeaders, String responseBody) throws NotSerializableException {
		return getClientContainer().processResponse(uri, call, callable, responseHeaders, responseBody);
	}

	protected abstract Object invokeRemoteCall(final IRemoteCall call, final IRemoteCallable callable) throws ECFException;

}
