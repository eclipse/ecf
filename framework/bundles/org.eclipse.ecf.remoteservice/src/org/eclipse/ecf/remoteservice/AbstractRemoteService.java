/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ecf.core.jobs.JobsExecutor;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.remoteservice.Activator;
import org.eclipse.ecf.remoteservice.asyncproxy.*;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;
import org.eclipse.equinox.concurrent.future.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Abstract remote service implementation.  Clients may subclass to avoid re-implementing 
 * methods from IRemoteService.
 * 
 * @since 4.1
 */
//@ProviderType
public abstract class AbstractRemoteService extends AbstractAsyncProxyRemoteService implements IRemoteService, InvocationHandler {

	protected static final Object[] EMPTY_ARGS = new Object[0];

	/**
	 * @since 8.2
	 */
	protected int futureExecutorServiceMaxThreads = Integer.parseInt(System.getProperty("ecf.remoteservice.futureExecutorServiceMaxThreads", "10")); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * @since 8.2
	 */
	protected ExecutorService futureExecutorService;

	/**
	 * @since 8.2
	 */
	protected ExecutorService getFutureExecutorService(IRemoteCall call) {
		synchronized (this) {
			if (futureExecutorService == null)
				futureExecutorService = Executors.newFixedThreadPool(futureExecutorServiceMaxThreads);
		}
		return futureExecutorService;
	}

	/**
	 * @since 8.2
	 */
	protected void setFutureExecutorService(ExecutorService executorService) {
		this.futureExecutorService = executorService;
	}

	/**
	 * @since 8.2
	 */
	protected IExecutor iFutureExecutor;

	/**
	 * @since 8.2
	 */
	protected IExecutor getIFutureExecutor(IRemoteCall call) {
		synchronized (this) {
			if (iFutureExecutor == null)
				iFutureExecutor = new JobsExecutor("RSJobs[rsID=" + getRemoteServiceID() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return iFutureExecutor;
	}

	/**
	 * @since 8.2
	 */
	protected void setIFutureExecutor(IExecutor executor) {
		this.iFutureExecutor = executor;
	}

	protected abstract String[] getInterfaceClassNames();

	protected abstract IRemoteServiceID getRemoteServiceID();

	protected abstract IRemoteServiceReference getRemoteServiceReference();

	protected Class loadInterfaceClass(String className) throws ClassNotFoundException {
		return loadInterfaceClass(this.getClass().getClassLoader(), className);
	}

	/**
	 * @since 6.0
	 */
	protected Class loadInterfaceClass(ClassLoader cl, String className) throws ClassNotFoundException {
		return Class.forName(className, true, cl);
	}

	protected IRemoteService getRemoteService() {
		return this;
	}

	protected long getDefaultTimeout() {
		return IRemoteCall.DEFAULT_TIMEOUT;
	}

	@Override
	protected IFuture callAsync(AbstractAsyncProxyRemoteCall call) {
		return callAsync((IRemoteCall) call);
	}

	public IFuture callAsync(final IRemoteCall call) {
		IExecutor executor = getIFutureExecutor(call);
		if (executor == null)
			throw new ServiceException("iFuture executor is null.  Cannot callAsync remote method=" + call.getMethod()); //$NON-NLS-1$
		return executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Exception {
				return callSync(call);
			}
		}, null);
	}

	@SuppressWarnings("unchecked")
	public Object getProxy() throws ECFException {
		List classes = new ArrayList();
		ClassLoader cl = this.getClass().getClassLoader();
		try {
			// Get clazz from reference
			final String[] clazzes = getInterfaceClassNames();
			for (int i = 0; i < clazzes.length; i++)
				classes.add(loadInterfaceClass(cl, clazzes[i]));
		} catch (final Exception e) {
			ECFException except = new ECFException("Failed to create proxy", e); //$NON-NLS-1$
			logWarning("Exception in remote service getProxy", except); //$NON-NLS-1$
			throw except;
		} catch (final NoClassDefFoundError e) {
			ECFException except = new ECFException("Failed to load proxy interface class", e); //$NON-NLS-1$
			logWarning("Could not load class for getProxy", except); //$NON-NLS-1$
			throw except;
		}
		return getProxy(cl, (Class[]) classes.toArray(new Class[classes.size()]));
	}

	/**
	 * @since 6.0
	 */
	protected void addRemoteServiceProxyToProxy(List classes) {
		IRemoteServiceReference rsReference = getRemoteServiceReference();
		// add IRemoteServiceProxy interface to set of interfaces supported by this proxy
		if (rsReference != null && rsReference.getProperty(Constants.SERVICE_PREVENT_RSPROXY) == null)
			classes.add(IRemoteServiceProxy.class);
	}

	private boolean nameAlreadyPresent(String className, List classes) {
		for (Iterator i = classes.iterator(); i.hasNext();) {
			Class c = (Class) i.next();
			if (className.equals(c.getName()))
				return true;
		}
		return false;
	}

	/**
	 * @since 8.3
	 */
	protected List addAsyncProxyClasses(ClassLoader cl, Class[] interfaces) {
		List intfs = Arrays.asList(interfaces);
		List results = new ArrayList();
		if (getRemoteServiceReference().getProperty(Constants.SERVICE_PREVENT_ASYNCPROXY) == null) {
			for (Iterator i = intfs.iterator(); i.hasNext();) {
				Class intf = (Class) i.next();
				String intfName = convertInterfaceNameToAsyncInterfaceName(intf.getName());
				if (intfName != null && !nameAlreadyPresent(intfName, intfs)) {
					Class asyncClass = findAsyncRemoteServiceProxyClass(cl, intf);
					// Only add if async
					if (asyncClass != null && !intfs.contains(asyncClass))
						results.add(asyncClass);
				}
			}
		}
		results.addAll(intfs);
		return results;
	}

	/**
	 * @since 6.0
	 */
	@SuppressWarnings("unchecked")
	public Object getProxy(ClassLoader cl, Class[] interfaces) throws ECFException {
		// Now add any async p
		List classes = addAsyncProxyClasses(cl, interfaces);

		addRemoteServiceProxyToProxy(classes);
		// create and return proxy
		try {
			return createProxy(cl, (Class[]) classes.toArray(new Class[classes.size()]));
		} catch (final Exception e) {
			ECFException except = new ECFException("Failed to create proxy", e); //$NON-NLS-1$
			logWarning("Exception in remote service getProxy", except); //$NON-NLS-1$
			throw except;
		} catch (final NoClassDefFoundError e) {
			ECFException except = new ECFException("Failed to load proxy interface class", e); //$NON-NLS-1$
			logWarning("Could not load class for getProxy", except); //$NON-NLS-1$
			throw except;
		}
	}

	/**
	 * @since 8.0
	 */
	public class ProxyClassLoader extends ClassLoader {

		private ClassLoader cl;

		public ProxyClassLoader(ClassLoader cl) {
			this.cl = cl;
		}

		public Class loadClass(String name) throws ClassNotFoundException {
			try {
				return cl.loadClass(name);
			} catch (ClassNotFoundException e) {
				// If the classloader passed in upon construction cannot 
				// find the class, then use this bundle's classloader to
				// try to load the class
				Activator a = Activator.getDefault();
				if (a == null)
					throw e;
				BundleContext context = a.getContext();
				if (context == null)
					throw e;
				return context.getBundle().loadClass(name);
			}
		}
	}

	/**
	 * @since 8.0
	 */
	protected IRemoteServiceProxyCreator getRemoteServiceProxyCreator() {
		ServiceTracker st = new ServiceTracker(Activator.getDefault().getContext(), IRemoteServiceProxyCreator.class, null);
		st.open();
		IRemoteServiceProxyCreator result = (IRemoteServiceProxyCreator) st.getService();
		st.close();
		return result;
	}

	/**
	 * @since 6.0
	 */
	protected Object createProxy(ClassLoader cl, Class[] classes) {
		IRemoteServiceProxyCreator proxyCreator = getRemoteServiceProxyCreator();
		if (proxyCreator != null)
			return proxyCreator.createProxy(new ProxyClassLoader(cl), classes, this);
		return Proxy.newProxyInstance(new ProxyClassLoader(cl), classes, this);
	}

	protected Object createProxy(Class[] classes) {
		return createProxy(this.getClass().getClassLoader(), classes);
	}

	/**
	 * @since 3.3
	 */
	protected Class findAsyncRemoteServiceProxyClass(Class c) {
		String proxyClassName = convertInterfaceNameToAsyncInterfaceName(c.getName());
		try {
			return Class.forName(proxyClassName);
		} catch (ClassNotFoundException e) {
			logInfo("No async remote service interface found with name=" + proxyClassName + " for proxy service class=" + c.getName(), e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		} catch (NoClassDefFoundError e) {
			logWarning("Async remote service interface with name=" + proxyClassName + " could not be loaded for proxy service class=" + c.getName(), e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	/**
	 * @since 6.0
	 */
	protected Class findAsyncRemoteServiceProxyClass(ClassLoader cl, Class c) {
		String proxyClassName = convertInterfaceNameToAsyncInterfaceName(c.getName());
		if (proxyClassName == null)
			return null;
		try {
			return Class.forName(proxyClassName, true, cl);
		} catch (ClassNotFoundException e) {
			//logInfo("No async remote service interface found with name=" + proxyClassName + " for remote service class=" + c.getName(), e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		} catch (NoClassDefFoundError e) {
			logWarning("Async remote service interface with name=" + proxyClassName + " could not be loaded for proxy service class=" + c.getName(), e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	protected String convertInterfaceNameToAsyncInterfaceName(String interfaceName) {
		if (interfaceName == null)
			return null;
		String asyncProxyName = (String) getRemoteServiceReference().getProperty(Constants.SERVICE_ASYNC_RSPROXY_CLASS_ + interfaceName);
		if (asyncProxyName != null)
			return asyncProxyName;
		if (interfaceName.endsWith(IAsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX))
			return interfaceName;
		return interfaceName + IAsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX;
	}

	protected Object[] getCallParametersForProxyInvoke(String callMethod, Method proxyMethod, Object[] args) {
		return args == null ? EMPTY_ARGS : args;
	}

	protected long getCallTimeoutForProxyInvoke(String callMethod, Method proxyMethod, Object[] args) {
		return IRemoteCall.DEFAULT_TIMEOUT;
	}

	protected String getCallMethodNameForProxyInvoke(Method method, Object[] args) {
		return method.getName();
	}

	protected Object invokeObject(Object proxy, final Method method, final Object[] args) throws Throwable {
		String methodName = method.getName();
		if (methodName.equals("toString")) { //$NON-NLS-1$
			final String[] clazzes = getInterfaceClassNames();
			String proxyClass = (clazzes.length == 1) ? clazzes[0] : Arrays.asList(clazzes).toString();
			return proxyClass + ".proxy@" + getRemoteServiceID(); //$NON-NLS-1$
		} else if (methodName.equals("hashCode")) { //$NON-NLS-1$
			return new Integer(hashCode());
		} else if (methodName.equals("equals")) { //$NON-NLS-1$
			if (args == null || args.length == 0)
				return Boolean.FALSE;
			try {
				return new Boolean(Proxy.getInvocationHandler(args[0]).equals(this));
			} catch (IllegalArgumentException e) {
				return Boolean.FALSE;
			}
			// This handles the use of IRemoteServiceProxy.getRemoteService method
		} else if (methodName.equals("getRemoteService")) { //$NON-NLS-1$
			return getRemoteService();
		} else if (methodName.equals("getRemoteServiceReference")) { //$NON-NLS-1$
			return getRemoteServiceReference();
		}
		return null;
	}

	protected Object invokeSync(IRemoteCall call) throws ECFException {
		return callSync(call);
	}

	/**
	 * @since 8.3
	 */
	protected boolean isAsync(Object proxy, Method method, Object[] args) {
		return (Arrays.asList(method.getDeclaringClass().getInterfaces()).contains(IAsyncRemoteServiceProxy.class) || method.getName().endsWith(IAsyncRemoteServiceProxy.ASYNC_METHOD_SUFFIX));
	}

	/**
	 * @since 8.3
	 */
	protected IRemoteCall createRemoteCall(final String callMethod, final Object[] callParameters, final long callTimeout) {
		return new IRemoteCall() {
			public String getMethod() {
				return callMethod;
			}

			public Object[] getParameters() {
				return callParameters;
			}

			public long getTimeout() {
				return callTimeout;
			}
		};
	}

	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		// methods declared by Object
		try {
			// If the method is from Class Object, or from IRemoteServiceProxy
			// then return result
			Object resultObject = invokeObject(proxy, method, args);
			if (resultObject != null)
				return resultObject;
			if (isAsync(proxy, method, args))
				return invokeAsync(method, args);
			// else call synchronously/block and return result
			final String callMethod = getCallMethodNameForProxyInvoke(method, args);
			final Object[] callParameters = getCallParametersForProxyInvoke(callMethod, method, args);
			final long callTimeout = getCallTimeoutForProxyInvoke(callMethod, method, args);
			final IRemoteCall remoteCall = createRemoteCall(callMethod, callParameters, callTimeout);
			return invokeSync(remoteCall);
		} catch (Throwable t) {
			if (t instanceof ServiceException)
				throw t;
			// rethrow as service exception
			throw new ServiceException("Service exception on remote service proxy rsid=" + getRemoteServiceID(), ServiceException.REMOTE, t); //$NON-NLS-1$
		}
	}

	/**
	 * @since 8.4
	 */
	public class AsyncArgs {
		private IRemoteCallListener listener;
		private Object[] args;
		private Class returnType;

		public AsyncArgs(Object[] originalArgs, Class returnType) {
			this.listener = null;
			this.args = originalArgs;
			this.returnType = returnType;
		}

		public AsyncArgs(IRemoteCallListener listener, Object[] originalArgs) {
			this.listener = listener;
			if (this.listener != null) {
				int asynchArgsLength = originalArgs.length - 1;
				this.args = new Object[asynchArgsLength];
				System.arraycopy(originalArgs, 0, args, 0, asynchArgsLength);
			} else
				this.args = originalArgs;
		}

		public IRemoteCallListener getListener() {
			return listener;
		}

		public Object[] getArgs() {
			return args;
		}

		public Class getReturnType() {
			return returnType;
		}
	}

	/**
	 * @since 8.4
	 */
	protected RemoteCall getAsyncRemoteCall(String invokeMethodName, Object[] asyncArgs) {
		return new RemoteCall(invokeMethodName, asyncArgs, IRemoteCall.DEFAULT_TIMEOUT);
	}

	/**
	 * @since 3.3
	 */
	protected Object invokeAsync(final Method method, final Object[] args) throws Throwable {
		final String invokeMethodName = getAsyncInvokeMethodName(method);
		final AsyncArgs asyncArgs = getAsyncArgs(method, args);
		RemoteCall remoteCall = getAsyncRemoteCall(invokeMethodName, asyncArgs.getArgs());
		IRemoteCallListener listener = asyncArgs.getListener();
		return (listener != null) ? callAsyncWithResult(remoteCall, listener) : callFuture(remoteCall, asyncArgs.getReturnType());
	}

	/**
	 * @since 8.2
	 */
	protected Object callAsyncWithResult(IRemoteCall call, IRemoteCallListener listener) {
		callAsync(call, listener);
		return null;
	}

	/**
	 * @since 8.4
	 */
	@Override
	protected void callCompletableAsync(AbstractAsyncProxyRemoteCall call, final IAsyncProxyCompletable completable) {
		callAsync((IRemoteCall) call, new IRemoteCallListener() {
			public void handleEvent(IRemoteCallEvent event) {
				if (event instanceof IRemoteCallCompleteEvent) {
					IRemoteCallCompleteEvent cce = (IRemoteCallCompleteEvent) event;
					completable.handleComplete(cce.getResponse(), cce.hadException(), cce.getException());
				}
			}
		});
	}

	/**
	 * @since 8.4
	 */
	@Override
	protected Future callFutureAsync(AbstractAsyncProxyRemoteCall call) {
		return callFutureAsync((IRemoteCall) call);
	}

	/**
	 * @since 8.2
	 */
	protected Future callFutureAsync(final IRemoteCall call) {
		ExecutorService executorService = getFutureExecutorService(call);
		if (executorService == null)
			throw new ServiceException("future executor service is null.  .  Cannot callAsync remote method=" + call.getMethod()); //$NON-NLS-1$
		return executorService.submit(new Callable() {
			public Object call() throws Exception {
				return callSync(call);
			}
		});
	}

	/**
	 * @since 3.3
	 */
	protected AsyncArgs getAsyncArgs(Method method, Object[] args) {
		IRemoteCallListener listener = null;
		Class returnType = method.getReturnType();
		// If the return type is of type java.util.concurrent.Future, then we return
		if (Future.class.isAssignableFrom(returnType) || IFuture.class.isAssignableFrom(returnType))
			return new AsyncArgs(args, returnType);
		// If the provided args do *not* include an IRemoteCallListener then we have a problem
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("Async calls must include a IRemoteCallListener instance as the last argument"); //$NON-NLS-1$
		// Get the last arg
		Object lastArg = args[args.length - 1];
		// If it's an IRemoteCallListener implementer directly, then just cast and return
		if (lastArg instanceof IRemoteCallListener) {
			listener = (IRemoteCallListener) lastArg;
		}
		// If it's an implementation of IAsyncCallback, then create a new listener based upon 
		// callback and return
		if (lastArg instanceof IAsyncCallback) {
			listener = new CallbackRemoteCallListener((IAsyncCallback) lastArg);
		}
		// If the last are is not an instance of IRemoteCallListener then there is a problem
		if (listener == null)
			throw new IllegalArgumentException("Last argument must be an instance of IRemoteCallListener"); //$NON-NLS-1$
		return new AsyncArgs(listener, args);
	}

	/**
	 * @since 3.3
	 */
	protected String getAsyncInvokeMethodName(Method method) {
		String methodName = method.getName();
		return methodName.endsWith(IAsyncRemoteServiceProxy.ASYNC_METHOD_SUFFIX) ? methodName.substring(0, methodName.length() - IAsyncRemoteServiceProxy.ASYNC_METHOD_SUFFIX.length()) : methodName;
	}

	private void logInfo(String message, Throwable e) {
		Activator a = Activator.getDefault();
		if (a != null)
			a.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, message, e));
	}

	protected void logWarning(String string, Throwable e) {
		Activator a = Activator.getDefault();
		if (a != null)
			a.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, string, e));
	}

	/**
	 * @since 8.2
	 */
	public void dispose() {
		synchronized (this) {
			if (futureExecutorService != null) {
				futureExecutorService.shutdownNow();
				futureExecutorService = null;
			}
			iFutureExecutor = null;
		}
	}
}
