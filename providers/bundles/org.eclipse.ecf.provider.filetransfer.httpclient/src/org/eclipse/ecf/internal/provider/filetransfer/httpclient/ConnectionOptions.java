/*******************************************************************************
* Copyright (c) 2011 IBM, and others. 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   IBM Corporation - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient;

/**
 * Options to enable reusing socket connections.
 * <p>
 * System property {@link #PROP_REUSE_CONNECTIONS} determines whether this file transfer 
 * provider reuses connections. 
 * </p> <p>
 * Without connection reuse each transfer or browse operations uses its 
 * own connection independent connection. As a result no connections are reused.
 * </p>
 * <p> With connection reuse enable a single connection pool is used. Details
 * of its behavior can be further customized with the following system properties:
 * <ul>
 * <li>{@link #PROP_MAX_TOTAL_CONNECTIONS}</li>
 * <li>{@link #PROP_MAX_CONNECTIONS_PER_HOST}</li>
 * <li>{@link #PROP_POOL_CONNECTION_TIMEOUT}</li>
 * <li>{@link #PROP_POOL_CLOSE_IDLE_PERIOD}</li>
 * </ul>
 * Changing this and any of the other system properties does not affect connections
 * already made.  
 * </p>
 * @since 4.0.1
 */
public interface ConnectionOptions {
	/**
	 * System property name to enable connection reuse for this provider.
	 * <p>
	 * The boolean value of this system property determines connection reuse. 
	 * The default value of this property is {@value #REUSE_CONNECTIONS_DEFAULT} as 
	 * defined by {@link #REUSE_CONNECTIONS_DEFAULT}. </p>
	 */
	public String PROP_REUSE_CONNECTIONS = "org.eclipse.ecf.provider.filetransfer.httpclient.reuseConnections.enabled"; //$NON-NLS-1$
	public boolean REUSE_CONNECTIONS_DEFAULT = true;

	/**
	 * System property name to specify maximum number of total connections in connection reuse mode.
	 * <p>
	 * This property only applies when connection reuse is enabled by {@link #PROP_REUSE_CONNECTIONS}.
	 * </p><p>
	 * The default value of this property is {@value #MAX_TOTAL_CONNECTIONS_DEFAULT} as 
	 * defined by {@link #MAX_TOTAL_CONNECTIONS_DEFAULT}. </p>
	 * <p>
	 * When the maximum number of connections are being used simultaneously another connection request 
	 * waits until a connection becomes available to the connection pool. The maximum wait time can 
	 * be adjusted using {@link #PROP_POOL_CONNECTION_TIMEOUT}. 
	 * </p><p>
	 * </p>     
	 */
	public String PROP_MAX_TOTAL_CONNECTIONS = "org.eclipse.ecf.provider.filetransfer.httpclient.maxConnectionsTotal"; //$NON-NLS-1$
	public int MAX_TOTAL_CONNECTIONS_DEFAULT = 200; // HttpClient default is 20.

	/**
	 * System property name to specify maximum number of connections per host in connection reuse mode.
	 * <p>
	 * This property only applies when connection reuse is enabled by {@link #PROP_REUSE_CONNECTIONS}.
	 * </p><p>
	 * The default value of this property is {@value #MAX_CONNECTIONS_PER_HOST_DEFAULT} as 
	 * defined by {@link #MAX_CONNECTIONS_PER_HOST_DEFAULT}. </p>
	 * <p>
	 * When the maximum number of connections are being used simultaneously another connection request 
	 * waits until a connection becomes available to the connection pool. The maximum wait time can 
	 * be adjusted using {@link #PROP_POOL_CONNECTION_TIMEOUT}. 
	 * </p><p>
	 * </p>     
	 */
	public String PROP_MAX_CONNECTIONS_PER_HOST = "org.eclipse.ecf.provider.filetransfer.httpclient.maxConnectionsPerHost"; //$NON-NLS-1$
	public int MAX_CONNECTIONS_PER_HOST_DEFAULT = 4; // HttpClient default is 2. 

	/**
	 * Property for connection pool timeout.
	 * <p>
	 * This property only applies when connection reuse is enabled by {@link #PROP_REUSE_CONNECTIONS}.
	 * </p><p>
	 * This is the name for a system property to change the timeout value for a 
	 * caller waits until a connection becomes available in the connection pool.
	 * </p> 
	 * <p>
	 * The value is a long value and its unit is milliseconds.
	 * With the value 0 no timeouts are used so that the caller waits until a connection becomes available.
	 * </p><p>
	 * The default value of this property is {@value #POOL_CONNECTION_TIMEOUT_DEFAULT} as 
	 * defined by {@link #POOL_CONNECTION_TIMEOUT_DEFAULT}. </p>
	 * </p>
	 */
	public String PROP_POOL_CONNECTION_TIMEOUT = "org.eclipse.ecf.provider.filetransfer.httpclient.poolConnectionTimeout"; //$NON-NLS-1$

	public long POOL_CONNECTION_TIMEOUT_DEFAULT = 0;

	/**
	 * Property to set period after which idle connections are closed. 
	 * <p>
	 * This setting only applies when reusing connection is enabled (see {@link #PROP_REUSE_CONNECTIONS}.
	 * <p></p> 
	 * This is the name for a system property to change the time period after
	 * which an idle connection can be closed by the ECF HttpClient based provider. 
	 * Currently idle connections are only closed when another transfer is made.
	 * </p> 
	 * <p>
	 * The value is a long value and its unit is milliseconds.
	 * When the value is 0 (or negative) idle connections are never closed except on shutdown.
	 * </p><p>
	 * The default is {@value #POOL_CLOSE_IDLE_PERIOD_DEFAULT} as 
	 * defined by {@link #POOL_CLOSE_IDLE_PERIOD_DEFAULT}.
	 * </p>
	 */
	public String PROP_POOL_CLOSE_IDLE_PERIOD = "org.eclipse.ecf.provider.filetransfer.httpclient.poolCloseIdle"; //$NON-NLS-1$

	/**
	 * Default period before idle connections are closed.
	 * <p>
	 * The default period after which idle connections can be closed is 3 minutes.  
	 * </p> 
	 */
	public long POOL_CLOSE_IDLE_PERIOD_DEFAULT = 3 * 60 * 1000;

}
