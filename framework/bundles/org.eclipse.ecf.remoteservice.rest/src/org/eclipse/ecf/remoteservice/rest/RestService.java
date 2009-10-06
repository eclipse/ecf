/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.Callback;
import org.eclipse.ecf.core.security.CallbackHandler;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.security.NameCallback;
import org.eclipse.ecf.core.security.ObjectCallback;
import org.eclipse.ecf.core.security.UnsupportedCallbackException;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.remoteservice.rest.Activator;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallStartEvent;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.equinox.concurrent.future.AbstractExecutor;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * This class represents a REST service from the client side of view. So a RESTful
 * web service can be accessed via the methods provided by this class. Mostly the 
 * methods are inherited from {@link IRemoteService}.
 */
public class RestService implements IRemoteService {

	/**
	 * inner class implementing the asynchronous result object. This
	 * implementation also provides the calling infrastructure.
	 */
	private class AsyncResult extends Thread {

		// the result of the call.
		Object result;

		// the exception, if any happened during the call.
		Throwable exception;

		// the remote call object.
		IRestCall call;

		// the callback listener, if provided.
		private IRemoteCallListener listener;

		// constructor
		AsyncResult(final IRestCall call, final IRemoteCallListener listener) {
			this.call = call;
			this.listener = listener;
		}

		// the call happens here.
		public void run() {
			Object r = null;
			Throwable e = null;

			final long reqID = getNextID();

			if (listener != null) {
				listener.handleEvent(new IRemoteCallStartEvent() {
					public IRemoteCall getCall() {
						// TODO: should return the remoteCall
						return null;
					}

					public IRemoteServiceReference getReference() {
						return reference;
					}

					public long getRequestId() {
						return reqID;
					}
				});
			}

			try {
				r = callSync(call);
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
		}
	}

	private int nextID;
	private IRemoteServiceReference reference;
	private Object proxy;

	public RestService(IRemoteServiceReference reference) {
		this.reference = reference;
	}

	public RestService(Object proxy) {
		this.proxy = proxy;
		reference = null;
	}

	public RestService() {
		reference = null;
	}

	/**
	 * get the next call id.
	 * 
	 * @return the next call id.
	 */
	synchronized long getNextID() {
		return nextID++;
	}

	public void callAsync(IRemoteCall call, IRemoteCallListener listener) {
		callAsync(lookupRestCall(call), listener);
	}

	public void callAsync(IRestCall restCall, IRemoteCallListener listener) {
		new AsyncResult(restCall, listener).start();
	}

	public IFuture callAsync(final IRemoteCall call) {
		return callAsync(lookupRestCall(call));
	}

	public IFuture callAsync(final IRestCall call) {
		final AbstractExecutor executor = new ThreadsExecutor();
		return executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Exception {
				return callSync(call);
			}
		}, null);
	}

	public Object callSync(IRemoteCall call) throws ECFException {
		return callSync(lookupRestCall(call));
	}

	public Object callSync(IRestCall call) throws ECFException {
		if(call == null)
			throw new ECFException("no IRestCall found for IRemoteCall");
		return callHttpMethod(call);
	}

	private IRestCall lookupRestCall(IRemoteCall call) {
		if(reference instanceof RestServiceReference) {
			RestServiceReference ref = (RestServiceReference) reference;
			RestContainer container = ref.getContainer();
			return container.lookupRestCall(call);
		}
		return null;
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
	public Object callHttpMethod(final IRestCall restCall) throws ECFException {
		// call the method
		HttpClient httpClient = new HttpClient();
		String url = handleURI(restCall.getURI().toString());
		HttpMethod httpMethod = createHttpMethod(restCall, url);
		// add additional request headers
		handleRequestHeaders(httpMethod, restCall.getRequestHeaders());
		// handle authentication
		handleAuthentication(httpClient, httpMethod);
		Object response = null;
		// needed because a resource can link to another resource
		httpClient.getParams().setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, new Boolean(true));
		// execute method
		try {
			int responseCode = httpClient.executeMethod(httpMethod);
			if(responseCode == HttpStatus.SC_OK) {
				response = getResourceRepresentation(restCall, httpMethod);
				if (proxy != null && proxy instanceof IRestResponseProcessor)
					((IRestResponseProcessor) proxy).processResource(response);
			} else 
				throw new ECFException("Service returned status code: " + responseCode);
		} catch (HttpException e) {
			throw new ECFException(e);
		} catch (IOException e) {
			throw new ECFException(e);
		} catch (ParseException e) {
			throw new ECFException(e);
		}
		return response;
	}

	/**
	 * Gets the resource representation for a given rest call and http method.
	 * The resource representation is queried from the
	 * IRestResourceRepresentationFactory service
	 * 
	 * @param restCall
	 *            the rest call
	 * @param httpMethod
	 *            the http method
	 * @param response
	 *            the response
	 * 
	 * @return the resource representation
	 * 
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * 
	 * @see IRestCall#getEstimatedResourceIdentifier()
	 * @see IRestCall#getMethod()
	 * @see IRestResourceRepresentationFactory
	 */
	private Object getResourceRepresentation(final IRestCall restCall, HttpMethod httpMethod) throws ParseException,
			IOException {
		Object response = null;
		BundleContext context = Activator.getDefault().getContext();
		ServiceReference serviceReference = context.getServiceReference(IRestResourceRepresentationFactory.class
				.getName());
		if (serviceReference != null) {
			IRestResourceRepresentationFactory factory = (IRestResourceRepresentationFactory) context
					.getService(serviceReference);
			response = factory.createResourceRepresentation(httpMethod, restCall);
		}
		return response;
	}

	protected void handleRequestHeaders(HttpMethod httpMethod,
			Map requestHeaders) {
		if(requestHeaders != null) {
			Set keySet = requestHeaders.keySet();
			Object[] headers = keySet.toArray();
			for(int i = 0; i < headers.length; i++) {
				String key = (String) headers[i];
				String value = (String) requestHeaders.get(key);
				httpMethod.addRequestHeader(key, value);
			}
		}
	}

	protected HttpMethod createHttpMethod(IRestCall restCall, String url) throws ECFException {
		HttpMethod httpMethod = null;
		String method = restCall.getMethod();
		if(method.equals(IRestCall.HTTP_GET)) {
			httpMethod = new GetMethod(url);
			addGetParams(httpMethod, restCall.getParameters());
		} else if(method.equals(IRestCall.HTTP_POST)) {
			httpMethod = new PostMethod(url);
			addPostParams(httpMethod, restCall.getParameters(), restCall
					.getRequestEntity());
		} else if(method.equals(IRestCall.HTTP_PUT)) {
			httpMethod = new PutMethod(url);
			addPutRequestBody(restCall, httpMethod);
		} else if(method.equals(IRestCall.HTTP_DELETE)) {
			httpMethod = new DeleteMethod(url);
		} else {
			throw new ECFException("HTTP method not supported");
		}
		return httpMethod;
	}

	protected void addPutRequestBody(IRestCall restCall, HttpMethod httpMethod) throws ECFException {
		PutMethod putMethod = (PutMethod) httpMethod;
		if(restCall.getParameters()[0] instanceof String) {
			String body = (String) restCall.getParameters()[0];
			RequestEntity entity;
			try {
				entity = new StringRequestEntity(body, null, null);
			} catch (UnsupportedEncodingException e) {
				throw new ECFException(
						"An error occured while creating the request entity", e);
			}
			putMethod.setRequestEntity(entity);
		} else {
			throw new ECFException(
					"For put the first Parameter must be a String");
		}
	}

	protected void addPostParams(HttpMethod httpMethod, Object[] restParams, RequestEntity requestEntity) {
		PostMethod postMethod = (PostMethod) httpMethod;
		// query parameters exclude a request entity
		if(restParams != null && restParams.length > 0) {
			postMethod.addParameters(toNameValuePairs(restParams));
		} else if(requestEntity != null) {
			postMethod.setRequestEntity(requestEntity);
		}
	}

	protected void addGetParams(HttpMethod httpMethod, Object[] restParams) {
		if(restParams != null) {
			httpMethod.setQueryString(toNameValuePairs(restParams));
		}
	}

	private NameValuePair[] toNameValuePairs(Object[] restParams) {
		List nameValueList = new ArrayList(restParams.length);
		for(int i = 0; i < restParams.length; i++) {
			if(restParams[i] instanceof String) {
				String param = (String) restParams[i];
				int indexOfEquals = param.indexOf('=');
				String key = param.substring(0, indexOfEquals);
				String value = param.substring(indexOfEquals + 1, param
						.length());
				nameValueList.add(new NameValuePair(key, value));
			}
		}
		return (NameValuePair[]) nameValueList.toArray(new NameValuePair[nameValueList.size()]);
	}

	protected void handleAuthentication(HttpClient httpClient, HttpMethod method) {
		if(reference instanceof RestServiceReference) {
			RestServiceReference ref = (RestServiceReference) reference;
			RestContainer container = ref.getContainer();
			IConnectContext connectContext = container.getConnectContext();
			if(connectContext != null) {
				NameCallback nameCallback = new NameCallback("");
				ObjectCallback passwordCallback = new ObjectCallback();
				Callback[] callbacks = new Callback[] { nameCallback,
						passwordCallback };
				CallbackHandler callbackHandler = connectContext
						.getCallbackHandler();
				if(callbackHandler == null)
					return;
				try {
					callbackHandler.handle(callbacks);
					String username = nameCallback.getName();
					String password = (String) passwordCallback.getObject();
					AuthScope authscope = new AuthScope(null, -1);
					Credentials credentials = new UsernamePasswordCredentials(
							username, password);
					httpClient.getState()
							.setCredentials(authscope, credentials);
					method.setDoAuthentication(true);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnsupportedCallbackException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private String handleURI(String uri) {
		if(uri.indexOf("http://") > -1)
			return uri;
		ID containerID = reference.getContainerID();

		if(containerID instanceof RestID) {
			RestID id = (RestID) containerID;
			URL baseURL = id.getBaseURL();
			String baseUrlString = baseURL.toExternalForm();
			int length = baseUrlString.length();
			char[] lastChar = new char[1];
			baseUrlString.getChars(length - 1, length, lastChar, 0);
			char[] firstMethodChar = new char[1];
			uri.getChars(0, 1, firstMethodChar, 0);
			if((lastChar[0] == '/' && firstMethodChar[0] != '/')
					|| (lastChar[0] != '/' && firstMethodChar[0] == '/'))
				return baseUrlString + uri;
			else if(lastChar[0] == '/' && firstMethodChar[0] == '/') {
				String tempurl = baseUrlString.substring(0, length - 1);
				return tempurl + uri;
			} else if(lastChar[0] != '/' && firstMethodChar[0] != '/')
				return baseUrlString + "/" + uri;
		}
		return null;
	}

	public Object getProxy() throws ECFException {
		return proxy;
	}

	void setReference(IRemoteServiceReference reference) {
		this.reference = reference;
	}

	public void fireAsync(IRemoteCall call) throws ECFException {
		callAsync(call);
	}

}
