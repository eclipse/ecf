/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis and Jeff McAffer - initial API and
 * implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server;

/**
 * Service Client Manager
 * @since 2.0
 */
public interface IServiceClientManager {

	/**
	 * Get an IServiceClient by it's String id.
	 * @param id the String id to use to lookup the IServiceClient.  If <code>null</code>, then <code>null</code> 
	 * will be returned.
	 * @return IServiceClient corresponding to given id.  If not found then <code>null</code> will be returned.
	 */
	public IServiceClient lookupClient(String id);
}
