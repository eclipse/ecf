/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice;

/**
 * Remote callables define 
 * @since 3.3
 */
public interface IRemoteCallable {

	/**
	 * Default remote call timeout is set to the value of system property 'ecf.remotecall.timeout'.  If system
	 * property not set, the default is set to 30000ms (30s).
	 */
	public static final long DEFAULT_TIMEOUT = new Long(System.getProperty("ecf.remotecallable.timeout", "30000")).longValue(); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Get the method name associated with this remote callable.
	 * @return String method name.  Must not be <code>null</code>.
	 */
	public String getMethod();

	/**
	 * Get the resource path associated with this remote callable.
	 * @return String the resource path for this remote callable.  Must not be <code>null</code>.
	 */
	public String getResourcePath();

	/**
	 * Get request type associated with this remote callable. 
	 * @return IRemoteCallableRequestType for this callable.  May be <code>null</code>.
	 */
	public IRemoteCallableRequestType getRequestType();

	/**
	 * Get default remote call parameters for this remote callable.
	 * @return IRemoteCallParameter[] array of default parameters for this remote callable. 
	 * May be <code>null</code>.
	 */
	public IRemoteCallParameter[] getDefaultParameters();

	/**
	 * Get default timeout for this remote callable.
	 * @return long default timeout value.  
	 */
	public long getDefaultTimeout();

}
