/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import java.util.Map;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;

/**
 * Rest call provides a way for clients to access/call a remote service. Instances
 * can be created via the {@link RestCallFactory} static method.  Created instances
 * typically will be passed to one of the call methods on {@link IRemoteService}.
 */
public interface IRestCall extends IRemoteCall {

	/**
	 * Default remote call timeout is set to the value of system property 'ecf.remotecall.rest.timeout'.  If system
	 * property not set, the default is set to 30000ms (30s).
	 */
	public static final long DEFAULT_TIMEOUT = new Long(System.getProperty("ecf.remotecall.rest.timeout", "30000")).longValue(); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Provides any call-specific request headers.
	 * 
	 * @return a {@link Map} object which contains and additional header parameters
	 *         (String->String). May be <code>null</code>.
	 */
	public Map getRequestHeaders();

}
