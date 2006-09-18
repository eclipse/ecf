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

import org.eclipse.ecf.core.identity.ID;

/**
 * Remote service reference.  Instances implementing this
 * interface are returned from the IRemoteServiceContainer.getRemoteServiceReferences call.
 * Once retrieved, such references can be resolved to
 * an IRemoteService via calls to IRemoteServiceContainer.getRemoteService(reference)
 *
 * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainer
 */
public interface IRemoteServiceReference {
	/**
	 * Get container ID for remote service
	 * @return ID the containerID for this reference (where the service is located)
	 */
	public ID getContainerID();
	/**
	 * Get given property for remote service
	 * @param key the key for the propert to get
	 * @return Object the object or null if does not have named property
	 */
	public Object getProperty(String key);
	/**
	 * Get all property keys for remote service
	 * @return String [] of property keys
	 */
	public String [] getPropertyKeys();
	
	/**
	 * Return true if reference is active, false otherwise
	 * @return true if reference is currently active, false otherwise
	 */
	public boolean isActive();
	
}
