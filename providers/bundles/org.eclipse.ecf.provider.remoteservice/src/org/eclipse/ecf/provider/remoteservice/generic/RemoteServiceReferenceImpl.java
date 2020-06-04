/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;

public class RemoteServiceReferenceImpl implements IRemoteServiceReference {

	protected RemoteServiceRegistrationImpl registration;

	protected String clazz = null;

	public RemoteServiceReferenceImpl(RemoteServiceRegistrationImpl registration) {
		this.registration = registration;
	}

	public Object getProperty(String key) {
		return registration.getProperty(key);
	}

	public String[] getPropertyKeys() {
		return registration.getPropertyKeys();
	}

	public ID getContainerID() {
		return registration.getContainerID();
	}

	public boolean isActive() {
		return (registration != null);
	}

	protected synchronized void setInactive() {
		registration = null;
		clazz = null;
	}

	protected RemoteServiceRegistrationImpl getRegistration() {
		return registration;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("RemoteServiceReferenceImpl["); //$NON-NLS-1$
		buf.append("registration=").append(getRegistration()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

	/**
	 * @since 3.0
	 */
	public IRemoteServiceID getID() {
		return registration.getID();
	}
}
