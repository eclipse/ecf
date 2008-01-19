/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.discovery;

/**
 * Adapter for IServiceInfo instances.  
 */
public interface IServiceInfoAdapter {

	/**
	 * Get container name associated with this service info.
	 * @return the container factory name.  Will return <code>null</code> if no
	 * container factory name associated with this service info.
	 */
	public String getContainerFactoryName();

	/**
	 * Get the targetID for accessing the remote container.
	 * @return String targetID for use in connecting to the remote container.   Will return <code>null</code>
	 * if there is incomplete/absent information for the info.
	 */
	public String getTarget();

	/**
	 * Set the container properties.
	 * 
	 * @param containerFactoryName set the containerFactoryName for this info.  May not be <code>null</code>.
	 * @param connectProtocol set the connectProtocol for the target.  May not be <code>null</code>.
	 * @param connectPath set the connect path for the target.  May be <code>null</code>.
	 * @param requiresPassword set whether the target requires a password.  May be <code>null</code>.
	 */
	public void setContainerProperties(String containerFactoryName, String connectProtocol, String connectPath, Boolean requiresPassword);
}
