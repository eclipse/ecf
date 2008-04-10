/*******************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.internal.provider.r_osgi;

import ch.ethz.iks.r_osgi.RemoteServiceReference;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;

/**
 * The R-OSGi adapter implementation of the IRemoteServiceReference.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
final class RemoteServiceReferenceImpl implements IRemoteServiceReference {

	// the container ID from where the remote service reference was retrieved.
	private ID containerID;

	// the R-OSGi remote service reference.
	private RemoteServiceReference ref;

	/**
	 * constructor.
	 * 
	 * @param containerID
	 *            the container ID.
	 * @param rref
	 *            the R-OSGi remote service reference.
	 */
	public RemoteServiceReferenceImpl(final ID containerID, final RemoteServiceReference rref) {
		this.containerID = containerID;
		this.ref = rref;
	}

	/**
	 * get the container ID.
	 * 
	 * @return the container ID.
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceReference#getContainerID()
	 */
	public ID getContainerID() {
		return containerID;
	}

	/**
	 * get the internal R-OSGi remote service reference.
	 * 
	 * @return the internal R-OSGi remote service reference.
	 */
	RemoteServiceReference getR_OSGiServiceReference() {
		return ref;
	}

	/**
	 * get a property of the remote service.
	 * 
	 * @return the property value.
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceReference#getProperty(java.lang.String)
	 */
	public Object getProperty(final String key) {
		return ref.getProperty(key);
	}

	/**
	 * get the property keys of the remote service.
	 * 
	 * @return a string array of the property keys.
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceReference#getPropertyKeys()
	 */
	public String[] getPropertyKeys() {
		return ref.getPropertyKeys();
	}

	/**
	 * check, if the reference is still active.
	 * 
	 * @return <code>true</code>, if active.
	 * 
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceReference#isActive()
	 */
	public boolean isActive() {
		return ref.isActive();
	}

	/**
	 * check for equality.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object o) {
		if (o instanceof RemoteServiceReferenceImpl) {
			return ref.equals(((RemoteServiceReferenceImpl) o).ref);
		}
		return false;
	}

	/**
	 * get the hash code.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return ref.hashCode();
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return ref.toString();
	}
}
