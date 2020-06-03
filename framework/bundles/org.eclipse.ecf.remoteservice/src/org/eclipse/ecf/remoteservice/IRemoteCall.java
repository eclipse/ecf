/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.remoteservice;

/**
 * Instances of this interface are used to invoke a method call on a remote
 * service
 * 
 * @see IRemoteService
 */
public interface IRemoteCall {

	/**
	 * Default remote call timeout is set to the value of system property 'ecf.remotecall.timeout'.  If system
	 * property not set, the default is set to 30000ms (30s).
	 * @since 4.0
	 */
	public static final long DEFAULT_TIMEOUT = new Long(System.getProperty("ecf.remotecall.timeout", "30000")).longValue(); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Get the method name to call on the remote. Must return a non-null and
	 * non-empty string
	 * 
	 * @return String name of method to call on the remote
	 */
	public String getMethod();

	/**
	 * Get the method parameters of the method to call on the remote. Will
	 * return a non-<code>null</code> array of Object parameters. The given
	 * Objects in the array must be be Serializable so that they may be
	 * serialized to deliver to remote.
	 * 
	 * @return Object [] the parameters to be provided for this call. Will not
	 *         be <code>null</code>, but may be empty array.
	 */
	public Object[] getParameters();

	/**
	 * Get timeout (in ms) for the remote call.
	 * 
	 * @return long timeout in ms
	 */
	public long getTimeout();
}
