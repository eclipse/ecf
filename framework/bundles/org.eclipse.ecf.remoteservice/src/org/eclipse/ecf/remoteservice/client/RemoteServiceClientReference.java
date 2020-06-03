/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;

/**
 * Reference objects for {@link AbstractClientContainer}.
 * 
 * @since 4.0
 */
public class RemoteServiceClientReference implements IRemoteServiceReference {

	protected RemoteServiceClientRegistration registration;

	public RemoteServiceClientReference(RemoteServiceClientRegistration remoteServiceClientRegistration) {
		registration = remoteServiceClientRegistration;
	}

	public ID getContainerID() {
		return registration.getContainerID();
	}

	public IRemoteServiceID getID() {
		return registration.getID();
	}

	public Object getProperty(String key) {
		return registration.getProperty(key);
	}

	public String[] getPropertyKeys() {
		return registration.getPropertyKeys();
	}

	public boolean isActive() {
		return registration != null;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("RemoteServiceClientReference["); //$NON-NLS-1$
		buf.append("id=").append(getID()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

}
