/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.remoteservice;

/**
 * 
 */
public interface Constants {

	/**
	 * Service property (named &quot;remote.objectClass&quot;) identifying all
	 * of the class names under which a service was registered in the remote
	 * services API (of type <code>java.lang.String[]</code>).
	 * 
	 * <p>
	 * This property is set by the remote services API when a service is
	 * registered.
	 */
	public static final String OBJECTCLASS = "remote.objectClass";

	/**
	 * Service property (named &quot;remote.service.id&quot;) identifying a
	 * service's registration number (of type <code>java.lang.Long</code>).
	 * 
	 * <p>
	 * The value of this property is assigned by the remote services API when a
	 * service is registered. The remote services API assigns a unique value
	 * that is larger than all previously assigned values since the remote
	 * services API was started. These values are NOT persistent across restarts
	 * of the remote services API.
	 */
	public static final String SERVICE_ID = "remote.service.id";

	/**
	 * Service property (named &quot;remote.service.ranking&quot;) identifying a
	 * service's ranking number (of type <code>java.lang.Integer</code>).
	 * 
	 * <p>
	 * This property may be supplied in the <code>properties
	 * Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 * 
	 * <p>
	 * The service ranking is used by the remote services API to determine the
	 * <i>default </i> service to be returned from a call to the
	 * {@link IRemoteServiceContainerAdapter#getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID[], String, String)}
	 * method: If more than one service implements the specified class, the
	 * <code>RemoteServiceReference</code> object with the highest ranking is
	 * returned.
	 * 
	 * <p>
	 * The default ranking is zero (0). A service with a ranking of
	 * <code>Integer.MAX_VALUE</code> is very likely to be returned as the
	 * default service, whereas a service with a ranking of
	 * <code>Integer.MIN_VALUE</code> is very unlikely to be returned.
	 * 
	 * <p>
	 * If the supplied property value is not of type
	 * <code>java.lang.Integer</code>, it is deemed to have a ranking value
	 * of zero.
	 */
	public static final String SERVICE_RANKING = "remote.service.ranking";

	/**
	 * Service property (named &quot;remote.service.vendor&quot;) identifying a
	 * service's vendor.
	 * 
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 */
	public static final String SERVICE_VENDOR = "remote.service.vendor";

	/**
	 * Service property (named &quot;remoteservice.description&quot;)
	 * identifying a service's description.
	 * 
	 * <p>
	 * This property may be supplied in the properties <code>Dictionary</code>
	 * object passed to the
	 * <code>IRemoteServiceContainerAdapter.registerRemoteService</code>
	 * method.
	 */
	public static final String SERVICE_DESCRIPTION = "remote.service.description";

}
