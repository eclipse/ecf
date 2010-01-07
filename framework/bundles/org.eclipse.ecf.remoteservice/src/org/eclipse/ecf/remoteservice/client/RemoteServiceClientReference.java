/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;

/**
 * Objects of this class represents the reference for a {@link AbstractClientContainer}.
 * @since 3.3
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
