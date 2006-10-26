/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.comm.provider;

import org.eclipse.ecf.core.comm.ConnectionTypeDescription;
import org.eclipse.ecf.core.comm.ConnectionCreateException;
import org.eclipse.ecf.core.comm.ISynchAsynchConnection;
import org.eclipse.ecf.core.comm.ISynchAsynchEventHandler;

/**
 * Provider interface for the org.eclipse.ecf.core.connectionFactory extension
 * point. Extensions implementing the org.eclipse.ecf.core.connectionFactory
 * extension point must implement this interface.
 * 
 */
public interface ISynchAsynchConnectionInstantiator {
	/**
	 * Create a new instance implementing ISynchAsynchConnection
	 * 
	 * @param description
	 *            the ConnectionTypeDescription use to create the instance
	 * @param handler
	 *            the event handler to be associated with the new connection
	 *            instance
	 * @param clazzes
	 *            the classes for the args array
	 * @param args
	 *            the arguments passed to the connection factory
	 * @return ISynchAsynchConnection created. Must not be null.
	 * @throws ConnectionCreateException
	 *             thrown if new instance of given ConnectionTypeDescription
	 *             cannot be created for whatever reason
	 */
	public ISynchAsynchConnection createInstance(
			ConnectionTypeDescription description,
			ISynchAsynchEventHandler handler, Class[] clazzes, Object[] args)
			throws ConnectionCreateException;
}