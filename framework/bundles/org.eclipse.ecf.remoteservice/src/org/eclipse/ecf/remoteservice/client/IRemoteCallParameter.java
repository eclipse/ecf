/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.client;

/**
 * A remote call parameter, with a String name and Object value.
 * 
 * @since 4.0
 */
public interface IRemoteCallParameter {

	/**
	 * Get the name of the remote call parameter.  Should not be <code>null</code>.
	 * @return String name for the parameter.  Should not be <code>null</code>.
	 */
	public String getName();

	/**
	 * Get the value associated with this remote call parameter.  May be <code>null</code>.
	 * @return Object value associated with the name given above.
	 */
	public Object getValue();

}
