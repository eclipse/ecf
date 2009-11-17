/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.rest.client.IRestClientContainerAdapter;

/**
 * Definition of an individual rest method call.  Instances of this interface can be registered with
 * {@link IRestClientContainerAdapter#registerCallable(IRestCallable[], java.util.Dictionary)} in order
 * to setup an association between a method name, and a resourcePath...as well as optionally other 
 * information about a specific rest method (the http request type...i.e. "get", "put", "post", "delete"
 * see {@link RequestType}, the default timeout, rest parameter names/default values, default request headers).  
 * 
 * When a rest call is actually invoked (e.g. via {@link IRemoteService#callSync(org.eclipse.ecf.remoteservice.IRemoteCall)}),
 * the value returned from {@link IRestCall#getMethod()} will be used to lookup/find an IRestCallable instance
 * with a matching value returned from {@link #getMethod()}, and if a match is found then the rest call will be made
 * with the parameters from the call, using the values from the found IRestCallable to actually make the http
 * request (e.g. the requestPath, the requestType, the default timeout, etc).
 *
 */
public interface IRestCallable {

	/**
	 * Default rest timeout is set to the value of system property 'ecf.restcall.timeout'.  If system
	 * property not set, the default is set to 30000ms.
	 */
	public static final long DEFAULT_TIMEOUT = new Long(System.getProperty("ecf.restcall.timeout", "30000")).longValue(); //$NON-NLS-1$ //$NON-NLS-2$
	/**
	 * Default request type is set to the value of system property 'ecf.restcall.defaultresttype'.  If system
	 * property not set, the default rest type is set to {@link RequestType#POST}.
	 */
	public static final RequestType DEFAULT_REQUEST_TYPE = RequestType.fromString(System.getProperty("ecf.restcall.defaultresttype", //$NON-NLS-1$
			"POST")); //$NON-NLS-1$

	/**
	 * Get the callable method.  Must not be <code>null</code>.
	 * @return String method
	 */
	public abstract String getMethod();

	/**
	 * Get the callable resourcePath.  Must not be <code>null</code>.  Typically, this will be the
	 * resource path of the resource to access for the rest call...e.g. "/statuses/user_timeline.json"
	 * @return String resourcePath.
	 */
	public abstract String getResourcePath();

	/**
	 * Get the callable default parameter information.  May be <code>null</code>.
	 * @return IRestParameter[] default parameter information associated with this rest call.
	 */
	public abstract IRestParameter[] getParameters();

	/**
	 * Get the timeout (in ms) for this rest callable.
	 * @return timeout (in ms).  If 0, request will wait indefinitely.  Default is set to {@link #DEFAULT_TIMEOUT}.
	 */
	public abstract long getDefaultTimeout();

	/**
	 * Get the request type for this callable.  Will not be <code>null</code>.
	 * 
	 * @return RequestType for this callable.  See {@link RequestType}.
	 */
	public abstract RequestType getRequestType();

	/**
	 * Get any default request headers for this call.  May be <code>null</code>.
	 * The resulting Map should container String keys and String values, and these will
	 * be inserted into the http request headers for transmission.
	 * 
	 * @return Map of request headers.  May be <code>null</code>.  Map should be <String,String>.
	 */
	public abstract Map getDefaultRequestHeaders();

	/**
	 * Http request type (GET, POST, PUT, DELETE).
	 */
	public static class RequestType implements Serializable {

		private static final long serialVersionUID = 2532492034058667225L;

		private static final String GET_NAME = "GET"; //$NON-NLS-1$

		private static final String POST_NAME = "POST"; //$NON-NLS-1$

		private static final String PUT_NAME = "PUT"; //$NON-NLS-1$

		private static final String DELETE_NAME = "DELETE"; //$NON-NLS-1$

		private final transient String name;

		// Protected constructor so that only subclasses are allowed to create
		// instances
		protected RequestType(String name) {
			this.name = name;
		}

		public static RequestType fromString(String requestType) {
			if (requestType == null)
				return null;
			if (requestType.equals(GET)) {
				return GET;
			} else if (requestType.equals(POST_NAME)) {
				return POST;
			} else if (requestType.equals(PUT_NAME)) {
				return PUT;
			} else if (requestType.equals(DELETE_NAME)) {
				return DELETE;
			} else
				return null;
		}

		public static final RequestType GET = new RequestType(GET_NAME);

		public static final RequestType POST = new RequestType(POST_NAME);

		public static final RequestType PUT = new RequestType(PUT_NAME);

		public static final RequestType DELETE = new RequestType(DELETE_NAME);

		public String toString() {
			return name;
		}

		// This is to make sure that subclasses don't screw up these methods
		public final boolean equals(Object that) {
			return super.equals(that);
		}

		public final int hashCode() {
			return super.hashCode();
		}

		// For serialization
		private static int nextOrdinal = 0;

		private final int ordinal = nextOrdinal++;

		private static final RequestType[] VALUES = {GET, POST, PUT, DELETE};

		/**
		 * @return Object
		 * @throws ObjectStreamException
		 *             not thrown by this implementation.
		 */
		Object readResolve() throws ObjectStreamException {
			return VALUES[ordinal];
		}
	}

}