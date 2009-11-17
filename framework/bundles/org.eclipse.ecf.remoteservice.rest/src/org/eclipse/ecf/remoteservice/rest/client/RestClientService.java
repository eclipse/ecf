/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.net.URI;
import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.core.security.*;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallStartEvent;
import org.eclipse.ecf.remoteservice.rest.*;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.rest.resource.IRestResourceProcessor;
import org.eclipse.equinox.concurrent.future.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.ServiceException;

/**
 * This class represents a REST service from the client side of view. So a
 * RESTful web service can be accessed via the methods provided by this class.
 * Mostly the methods are inherited from {@link IRemoteService}.
 */
public class RestClientService implements IRemoteService, InvocationHandler {

	private long nextID = 0;

	/**
	 * get the next call id.
	 * 
	 * @return the next call id.
	 */
	protected long getNextRequestID() {
		return nextID++;
	}

	protected RestClientServiceRegistration registration;

	public RestClientService(RestClientServiceRegistration registration) {
		Assert.isNotNull(registration);
		this.registration = registration;
	}

	public Object callSync(IRemoteCall call) throws ECFException {
		IRestCallable callable = registration.lookupCallable(call);
		if (callable == null)
			throw new ECFException("Restcallable not found"); //$NON-NLS-1$
		return callHttpMethod(call, callable);
	}

	public IFuture callAsync(final IRemoteCall call) {
		return callAsync(call, registration.lookupCallable(call));
	}

	public void callAsync(IRemoteCall call, IRemoteCallListener listener) {
		callAsync(call, registration.lookupCallable(call), listener);
	}

	public void fireAsync(IRemoteCall call) throws ECFException {
		IRestCallable restClientCallable = registration.lookupCallable(call);
		if (restClientCallable == null)
			throw new ECFException("Restcallable not found"); //$NON-NLS-1$
		callAsync(call, restClientCallable);
	}

	protected void callAsync(IRemoteCall call, IRestCallable restClientCallable, IRemoteCallListener listener) {
		final AbstractExecutor executor = new ThreadsExecutor();
		executor.execute(new AsyncResult(call, restClientCallable, listener), null);
	}

	protected IFuture callAsync(final IRemoteCall call, final IRestCallable callable) {
		final AbstractExecutor executor = new ThreadsExecutor();
		return executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Exception {
				if (callable == null)
					throw new ECFException("Restcallable not found"); //$NON-NLS-1$
				return callHttpMethod(call, callable);
			}
		}, null);
	}

	/**
	 * Calls the Rest service with given URL of IRestCall. The returned value is
	 * the response body as an InputStream.
	 * 
	 * @param restCall
	 *            The Rest Service to call represented by an IRestCall object
	 * @return The InputStream of the response body or <code>null</code> if an
	 *         error occurs.
	 */
	protected Object callHttpMethod(final IRemoteCall call, final IRestCallable callable) throws ECFException {
		String uri = prepareRequestURI(call, callable);
		HttpMethod httpMethod = createHttpMethod(uri, call, callable);
		// add additional request headers
		addRequestHeaders(httpMethod, call, callable);
		HttpClient httpClient = new HttpClient();
		// handle authentication
		setupAuthenticaton(httpClient, httpMethod);
		// needed because a resource can link to another resource
		httpClient.getParams().setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, new Boolean(true));
		setupTimeouts(httpClient, call, callable);
		// execute method
		String responseBody = null;
		try {
			int responseCode = executeHttpMethod(httpClient, httpMethod);
			if (responseCode == HttpStatus.SC_OK) {
				// Get responseBody as String
				responseBody = getResponseBodyAsString(httpMethod);
				if (responseBody == null)
					throw new ECFException("Invalid server response"); //$NON-NLS-1$
			} else
				throw new ECFException(NLS.bind("Http response not OK.  URL={0}, responseCode={1}", uri, new Integer(responseCode))); //$NON-NLS-1$
		} catch (HttpException e) {
			handleTransportException("Transport exception", e); //$NON-NLS-1$
		} catch (IOException e) {
			handleTransportException("Transport exception", e); //$NON-NLS-1$
		}
		return processResponse(call, callable, convertResponseHeaders(httpMethod.getResponseHeaders()), responseBody);
	}

	protected void handleTransportException(String message, Throwable e) throws ECFException {
		throw new ECFException(message, e);
	}

	protected int executeHttpMethod(HttpClient client, HttpMethod method) throws HttpException, IOException {
		return client.executeMethod(method);
	}

	protected String getResponseBodyAsString(HttpMethod method) throws IOException {
		return method.getResponseBodyAsString();
	}

	protected void setupTimeouts(HttpClient httpClient, IRemoteCall call, IRestCallable callable) {
		long callTimeout = call.getTimeout();
		if (callTimeout == IRestCallable.DEFAULT_TIMEOUT)
			callTimeout = callable.getDefaultTimeout();

		int timeout = (int) callTimeout;
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(timeout);
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
		httpClient.getParams().setConnectionManagerTimeout(timeout);
	}

	protected String prepareRequestURI(IRemoteCall call, IRestCallable callable) throws ECFException {
		String resourcePath = callable.getResourcePath();
		if (resourcePath == null || "".equals(resourcePath)) //$NON-NLS-1$
			throw new ECFException("resourcePath cannot be null or empty"); //$NON-NLS-1$
		// if resourcePath startswith http then we use it unmodified
		if (resourcePath.startsWith("http://")) //$NON-NLS-1$
			return resourcePath;

		RestID targetContainerID = registration.getRestClientContainer().getTargetRestID();
		URI baseURI = targetContainerID.toURI();
		String baseUriString = baseURI.toString();
		int length = baseUriString.length();
		char[] lastChar = new char[1];
		baseUriString.getChars(length - 1, length, lastChar, 0);
		char[] firstMethodChar = new char[1];
		resourcePath.getChars(0, 1, firstMethodChar, 0);
		if ((lastChar[0] == '/' && firstMethodChar[0] != '/') || (lastChar[0] != '/' && firstMethodChar[0] == '/'))
			return baseUriString + resourcePath;
		else if (lastChar[0] == '/' && firstMethodChar[0] == '/') {
			String tempurl = baseUriString.substring(0, length - 1);
			return tempurl + resourcePath;
		} else if (lastChar[0] != '/' && firstMethodChar[0] != '/')
			return baseUriString + "/" + resourcePath; //$NON-NLS-1$
		return null;
	}

	protected Object processResponse(IRemoteCall call, IRestCallable callable, Map responseHeaders, String responseBody) throws ECFException {
		IRestResourceProcessor restResourceProcessor = registration.getRestClientContainer().getRestResourceForCall(call, callable, responseHeaders);
		if (restResourceProcessor == null)
			return null;
		return restResourceProcessor.createResponseRepresentation(call, callable, responseHeaders, responseBody);
	}

	private Map convertResponseHeaders(Header[] headers) {
		Map result = new HashMap();
		if (headers == null)
			return result;
		for (int i = 0; i < headers.length; i++) {
			String name = headers[i].getName();
			String value = headers[i].getValue();
			result.put(name, value);
		}
		return result;
	}

	protected void addRequestHeaders(HttpMethod httpMethod, IRemoteCall call, IRestCallable callable) {
		// Add request headers from the callable
		Map requestHeaders = callable.getDefaultRequestHeaders();
		if (requestHeaders == null)
			requestHeaders = new HashMap();

		if (call instanceof IRestCall) {
			Map callHeaders = ((IRestCall) call).getRequestHeaders();
			if (callHeaders != null)
				requestHeaders.putAll(requestHeaders);
		}

		Set keySet = requestHeaders.keySet();
		Object[] headers = keySet.toArray();
		for (int i = 0; i < headers.length; i++) {
			String key = (String) headers[i];
			String value = (String) requestHeaders.get(key);
			httpMethod.addRequestHeader(key, value);
		}
	}

	protected HttpMethod createHttpMethod(String url, IRemoteCall call, IRestCallable callable) throws ECFException {
		HttpMethod httpMethod = null;

		IRestCallable.RequestType requestType = callable.getRequestType();
		if (requestType == null)
			throw new ECFException("Request type for call cannot be null"); //$NON-NLS-1$
		if (requestType.equals(IRestCallable.RequestType.GET)) {
			httpMethod = prepareGetMethod(url, call, callable);
		} else if (requestType.equals(IRestCallable.RequestType.POST)) {
			httpMethod = preparePostMethod(url, call, callable);
		} else if (requestType.equals(IRestCallable.RequestType.PUT)) {
			httpMethod = preparePutMethod(url, call, callable);
		} else if (requestType.equals(IRestCallable.RequestType.DELETE)) {
			httpMethod = prepareDeleteMethod(url, call, callable);
		} else {
			throw new ECFException(NLS.bind("HTTP method {0} not supported", requestType)); //$NON-NLS-1$
		}
		return httpMethod;
	}

	/**
	 * @throws ECFException  
	 */
	protected HttpMethod prepareDeleteMethod(String url, IRemoteCall call, IRestCallable callable) throws ECFException {
		return new DeleteMethod(url);
	}

	protected HttpMethod preparePutMethod(String url, IRemoteCall call, IRestCallable callable) throws ECFException {
		PutMethod putMethod = new PutMethod(url);
		if (call.getParameters()[0] instanceof String) {

			String body = (String) call.getParameters()[0];
			RequestEntity entity;
			try {
				entity = new StringRequestEntity(body, null, null);
			} catch (UnsupportedEncodingException e) {
				throw new ECFException("An error occured while creating the request entity", e); //$NON-NLS-1$
			}
			putMethod.setRequestEntity(entity);

		} else {
			throw new ECFException("For PutMethod the first parameter must be a String"); //$NON-NLS-1$
		}
		return putMethod;
	}

	/**
	 * @throws ECFException  
	 */
	protected HttpMethod preparePostMethod(String url, IRemoteCall call, IRestCallable callable) throws ECFException {
		PostMethod result = new PostMethod(url);
		NameValuePair[] params = toNameValuePairs(call, callable);
		if (params != null)
			result.addParameters(params);
		return result;
	}

	/**
	 * @throws ECFException  
	 */
	protected HttpMethod prepareGetMethod(String url, IRemoteCall call, IRestCallable callable) throws ECFException {
		HttpMethod result = new GetMethod(url);
		NameValuePair[] params = toNameValuePairs(call, callable);
		if (params != null)
			result.setQueryString(params);
		return result;
	}

	protected NameValuePair[] toNameValuePairs(IRemoteCall call, IRestCallable callable) {
		IRestParameter[] restParameters = toRestParameters(call.getParameters(), callable.getParameters());
		List nameValueList = new ArrayList();
		if (restParameters != null) {
			for (int i = 0; i < restParameters.length; i++) {
				nameValueList.add(new NameValuePair(restParameters[i].getName(), restParameters[i].getValue()));
			}
		}
		return (NameValuePair[]) nameValueList.toArray(new NameValuePair[nameValueList.size()]);
	}

	protected IRestParameter[] toRestParameters(Object[] callParameters, IRestParameter[] callableParameters) {
		List results = new ArrayList();
		if (callParameters == null)
			return callableParameters;
		for (int i = 0; i < callParameters.length; i++) {
			Object p = callParameters[i];
			// If the parameter is already a rest parameter just add
			if (p instanceof IRestParameter) {
				results.add(p);
				continue;
			}
			String name = null;
			if (callableParameters != null && i < callableParameters.length) {
				name = callableParameters[i].getName();
			}
			if (name != null) {
				String val = null;
				if (p instanceof String) {
					val = (String) p;
					results.add(new RestParameter(name, val));
				}
			}
		}
		return (IRestParameter[]) results.toArray(new IRestParameter[] {});
	}

	protected void setupAuthenticaton(HttpClient httpClient, HttpMethod method) {
		RestClientContainer container = registration.getRestClientContainer();
		IConnectContext connectContext = container.getRestConnectContext();
		if (connectContext != null) {
			NameCallback nameCallback = new NameCallback(""); //$NON-NLS-1$
			ObjectCallback passwordCallback = new ObjectCallback();
			Callback[] callbacks = new Callback[] {nameCallback, passwordCallback};
			CallbackHandler callbackHandler = connectContext.getCallbackHandler();
			if (callbackHandler == null)
				return;
			try {
				callbackHandler.handle(callbacks);
				String username = nameCallback.getName();
				String password = (String) passwordCallback.getObject();
				AuthScope authscope = new AuthScope(null, -1);
				Credentials credentials = new UsernamePasswordCredentials(username, password);
				httpClient.getState().setCredentials(authscope, credentials);
				method.setDoAuthentication(true);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnsupportedCallbackException e) {
				e.printStackTrace();
			}

		}
	}

	public Object getProxy() throws ECFException {
		Object proxy;
		try {
			// Get clazz from reference
			final String[] clazzes = registration.getClazzes();
			final Class[] cs = new Class[clazzes.length + 1];
			for (int i = 0; i < clazzes.length; i++)
				cs[i] = Class.forName(clazzes[i]);
			// add IRemoteServiceProxy interface to set of interfaces supported
			// by this proxy
			cs[clazzes.length] = IRemoteServiceProxy.class;
			proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), cs, this);
		} catch (final Exception e) {
			throw new ECFException(NLS.bind("Exception creating proxy rsid={0}", registration.getID()), e); //$NON-NLS-1$
		}
		return proxy;
	}

	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		// methods declared by Object
		try {
			if (method.getName().equals("toString")) { //$NON-NLS-1$
				final String[] clazzes = registration.getClazzes();
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
				// This handles the use of IRemoteServiceProxy.getRemoteService
				// method
			} else if (method.getName().equals("getRemoteService")) { //$NON-NLS-1$
				return this;
			} else if (method.getName().equals("getRemoteServiceReference")) { //$NON-NLS-1$
				return registration.getReference();
			}
			return callSync(new IRemoteCall() {

				public String getMethod() {
					return RestClientServiceRegistration.getFQMethod(method.getDeclaringClass().getName(), method.getName());
				}

				public Object[] getParameters() {
					return args;
				}

				public long getTimeout() {
					return IRestCallable.DEFAULT_TIMEOUT;
				}
			});
		} catch (Throwable t) {
			if (t instanceof ServiceException)
				throw (ServiceException) t;
			// else rethrow as service exception
			throw new ServiceException("Service exception on remote service proxy rsid=" + registration.getID(), ServiceException.REMOTE, t); //$NON-NLS-1$
		}
	}

	/**
	 * inner class implementing the asynchronous result object. This
	 * implementation also provides the calling infrastructure.
	 */
	private class AsyncResult implements IProgressRunnable {

		IRemoteCall call;
		// the remote call object.
		IRestCallable callable;
		// the callback listener, if provided.
		IRemoteCallListener listener;

		// the result of the call.
		Object result;
		// the exception, if any happened during the call.
		Throwable exception;

		// constructor
		AsyncResult(final IRemoteCall call, final IRestCallable callable, final IRemoteCallListener listener) {
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
						return registration.getReference();
					}

					public long getRequestId() {
						return reqID;
					}
				});
			}

			try {
				if (callable == null)
					throw new ECFException(NLS.bind("Restcall not found for method={0}", call.getMethod())); //$NON-NLS-1$
				r = callHttpMethod(call, callable);
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

}
