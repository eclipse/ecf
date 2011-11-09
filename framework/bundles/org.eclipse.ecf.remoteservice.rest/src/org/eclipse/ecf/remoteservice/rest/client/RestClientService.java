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
import java.io.NotSerializableException;
import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.security.*;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.remoteservice.rest.Activator;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.client.*;
import org.eclipse.ecf.remoteservice.rest.IRestCall;
import org.eclipse.ecf.remoteservice.rest.RestException;

/**
 * This class represents a REST service from the client side of view. So a
 * RESTful web service can be accessed via the methods provided by this class.
 * Mostly the methods are inherited from {@link IRemoteService}.
 */
public class RestClientService extends AbstractClientService {

	protected final static int DEFAULT_RESPONSE_BUFFER_SIZE = 1024;

	protected final static String DEFAULT_HTTP_CONTENT_CHARSET = "UTF-8"; //$NON-NLS-1$

	protected HttpClient httpClient;
	protected int responseBufferSize = DEFAULT_RESPONSE_BUFFER_SIZE;

	public RestClientService(RestClientContainer container, RemoteServiceClientRegistration registration) {
		super(container, registration);
		this.httpClient = new HttpClient();
	}

	private boolean isResponseOk(int httpResponseCode) {
		int isOkCode = httpResponseCode - 200;
		return (isOkCode >= 0 && isOkCode < 100);
	}

	/**
	 * Calls the Rest service with given URL of IRestCall. The returned value is
	 * the response body as an InputStream.
	 * 
	 * @param call
	 *            The remote call to make.  Must not be <code>null</code>.
	 * @param callable
	 *            The callable with default parameters to use to make the call.
	 * @return The InputStream of the response body or <code>null</code> if an
	 *         error occurs.
	 */
	protected Object invokeRemoteCall(final IRemoteCall call, final IRemoteCallable callable) throws ECFException {
		String uri = prepareEndpointAddress(call, callable);
		HttpMethod httpMethod = createAndPrepareHttpMethod(uri, call, callable);
		// execute method
		String responseBody = null;
		int responseCode = -1;
		try {
			responseCode = httpClient.executeMethod(httpMethod);
			if (isResponseOk(responseCode)) {
				// Get responseBody as String
				responseBody = getResponseAsString(httpMethod);
			} else {
				// If this method returns true, we should retrieve the response body
				if (retrieveErrorResponseBody(responseCode)) {
					responseBody = getResponseAsString(httpMethod);
				}
				// Now pass to the exception handler
				handleException("Http response not OK.  URL=" + uri + " responseCode=" + new Integer(responseCode), null, responseCode, responseBody); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (HttpException e) {
			handleException("Transport HttpException", e, responseCode); //$NON-NLS-1$
		} catch (IOException e) {
			handleException("Transport IOException", e, responseCode); //$NON-NLS-1$
		}
		Object result = null;
		try {
			result = processResponse(uri, call, callable, convertResponseHeaders(httpMethod.getResponseHeaders()), responseBody);
		} catch (NotSerializableException e) {
			handleException("Exception deserializing response.  URL=" + uri + " responseCode=" + new Integer(responseCode), e, responseCode); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}

	protected boolean retrieveErrorResponseBody(int responseCode) {
		// XXX this needs to be defined differently for 
		return false;
	}

	protected String getResponseAsString(HttpMethod httpMethod) throws IOException {
		// Get response bytes
		byte[] responseBytes = httpMethod.getResponseBody();
		String responseCharSet = null;
		if (httpMethod instanceof HttpMethodBase) {
			HttpMethodBase methodBase = (HttpMethodBase) httpMethod;
			responseCharSet = methodBase.getRequestCharSet();
		}
		return getResponseAsString(responseBytes, responseCharSet);
	}

	protected String getResponseAsString(byte[] bytes, String responseCharSet) {
		if (bytes == null)
			return null;
		return EncodingUtil.getString(bytes, responseCharSet);
	}

	protected void handleException(String message, Throwable e, int responseCode, String responseBody) throws RestException {
		logException(message, e);
		throw new RestException(message, e, responseCode, responseBody);
	}

	protected void handleException(String message, Throwable e, int responseCode) throws RestException {
		handleException(message, e, responseCode, null);
	}

	protected void setupTimeouts(HttpClient httpClient, IRemoteCall call, IRemoteCallable callable) {
		long callTimeout = call.getTimeout();
		if (callTimeout == IRemoteCall.DEFAULT_TIMEOUT)
			callTimeout = callable.getDefaultTimeout();

		int timeout = (int) callTimeout;
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(timeout);
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
		httpClient.getParams().setConnectionManagerTimeout(timeout);
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

	protected void addRequestHeaders(HttpMethod httpMethod, IRemoteCall call, IRemoteCallable callable) {
		// Add request headers from the callable
		Map requestHeaders = (callable.getRequestType() instanceof AbstractRequestType) ? ((AbstractRequestType) callable.getRequestType()).getDefaultRequestHeaders() : new HashMap();
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

	protected HttpMethod createAndPrepareHttpMethod(String uri, IRemoteCall call, IRemoteCallable callable) throws RestException {
		HttpMethod httpMethod = null;

		IRemoteCallableRequestType requestType = callable.getRequestType();
		if (requestType == null)
			throw new RestException("Request type for call cannot be null"); //$NON-NLS-1$
		try {
			if (requestType instanceof HttpGetRequestType) {
				httpMethod = prepareGetMethod(uri, call, callable);
			} else if (requestType instanceof HttpPostRequestType) {
				httpMethod = preparePostMethod(uri, call, callable);
			} else if (requestType instanceof HttpPutRequestType) {
				httpMethod = preparePutMethod(uri, call, callable);
			} else if (requestType instanceof HttpDeleteRequestType) {
				httpMethod = prepareDeleteMethod(uri, call, callable);
			} else {
				throw new RestException("HTTP method " + requestType + " not supported"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (NotSerializableException e) {
			String message = "Could not serialize parameters for uri=" + uri + " call=" + call + " callable=" + callable; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			logException(message, e);
			throw new RestException(message);
		}
		// add additional request headers
		addRequestHeaders(httpMethod, call, callable);
		// handle authentication
		setupAuthenticaton(httpClient, httpMethod);
		// needed because a resource can link to another resource
		httpClient.getParams().setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, new Boolean(true));
		httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, DEFAULT_HTTP_CONTENT_CHARSET);
		setupTimeouts(httpClient, call, callable);
		return httpMethod;
	}

	/**
	 * @throws RestException  
	 */
	protected HttpMethod prepareDeleteMethod(String uri, IRemoteCall call, IRemoteCallable callable) throws RestException {
		return new DeleteMethod(uri);
	}

	protected HttpMethod preparePutMethod(String uri, IRemoteCall call, IRemoteCallable callable) throws NotSerializableException {
		PutMethod result = new PutMethod(uri);
		HttpPutRequestType putRequestType = (HttpPutRequestType) callable.getRequestType();

		IRemoteCallParameter[] defaultParameters = callable.getDefaultParameters();
		Object[] parameters = call.getParameters();

		if (putRequestType.useRequestEntity()) {
			if (defaultParameters != null && defaultParameters.length > 0 && parameters != null && parameters.length > 0) {
				RequestEntity requestEntity = putRequestType.generateRequestEntity(uri, call, callable, defaultParameters[0], parameters[0]);
				result.setRequestEntity(requestEntity);
			}
		} else {
			NameValuePair[] params = toNameValuePairs(uri, call, callable);
			if (params != null)
				result.setQueryString(params);
		}
		return result;
	}

	/**
	 * @throws ECFException  
	 */
	protected HttpMethod preparePostMethod(String uri, IRemoteCall call, IRemoteCallable callable) throws NotSerializableException {
		PostMethod result = new PostMethod(uri);
		HttpPostRequestType postRequestType = (HttpPostRequestType) callable.getRequestType();

		IRemoteCallParameter[] defaultParameters = callable.getDefaultParameters();
		Object[] parameters = call.getParameters();
		if (postRequestType.useRequestEntity()) {
			if (defaultParameters != null && defaultParameters.length > 0 && parameters != null && parameters.length > 0) {
				RequestEntity requestEntity = postRequestType.generateRequestEntity(uri, call, callable, defaultParameters[0], parameters[0]);
				result.setRequestEntity(requestEntity);
			}
		} else {
			NameValuePair[] params = toNameValuePairs(uri, call, callable);
			if (params != null)
				result.setQueryString(params);
		}
		return result;
	}

	/**
	 * @throws ECFException  
	 */
	protected HttpMethod prepareGetMethod(String uri, IRemoteCall call, IRemoteCallable callable) throws NotSerializableException {
		HttpMethod result = new GetMethod(uri);
		NameValuePair[] params = toNameValuePairs(uri, call, callable);
		if (params != null)
			result.setQueryString(params);
		return result;
	}

	protected NameValuePair[] toNameValuePairs(String uri, IRemoteCall call, IRemoteCallable callable) throws NotSerializableException {
		IRemoteCallParameter[] restParameters = prepareParameters(uri, call, callable);
		List nameValueList = new ArrayList();
		if (restParameters != null) {
			for (int i = 0; i < restParameters.length; i++) {
				String parameterValue = null;
				Object o = restParameters[i].getValue();
				if (o instanceof String) {
					parameterValue = (String) o;
				} else if (o != null) {
					parameterValue = o.toString();
				}
				if (parameterValue != null) {
					nameValueList.add(new NameValuePair(restParameters[i].getName(), parameterValue));
				}
			}
		}
		return (NameValuePair[]) nameValueList.toArray(new NameValuePair[nameValueList.size()]);
	}

	protected void setupAuthenticaton(HttpClient httpClient, HttpMethod method) {
		IConnectContext connectContext = container.getConnectContextForAuthentication();
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
				logException("IOException setting credentials for rest httpclient", e); //$NON-NLS-1$
			} catch (UnsupportedCallbackException e) {
				logException("UnsupportedCallbackException setting credentials for rest httpclient", e); //$NON-NLS-1$
			}

		}
	}

	protected void logException(String string, Throwable e) {
		Activator a = Activator.getDefault();
		if (a != null)
			a.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, string, e));
	}

	protected void logWarning(String string, Throwable e) {
		Activator a = Activator.getDefault();
		if (a != null)
			a.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, string));
	}

}
