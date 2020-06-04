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
package org.eclipse.ecf.internal.provider.r_osgi;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceReference;

public class LocalRemoteServiceReferenceImpl implements IRemoteServiceReference {

	private final IRemoteServiceID remoteServiceID;
	private ServiceReference reference;

	public LocalRemoteServiceReferenceImpl(IRemoteServiceID remoteServiceID, ServiceReference ref) {
		this.remoteServiceID = remoteServiceID;
		this.reference = ref;
	}

	public ID getContainerID() {
		return remoteServiceID.getContainerID();
	}

	public IRemoteServiceID getID() {
		return remoteServiceID;
	}

	public Object getProperty(String key) {
		return reference.getProperty(key);
	}

	public String[] getPropertyKeys() {
		return reference.getPropertyKeys();
	}

	public boolean isActive() {
		return true;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("LocalRemoteServiceReferenceImpl["); //$NON-NLS-1$
		buf.append("remoteServiceID=").append(remoteServiceID); //$NON-NLS-1$
		buf.append(";reference=").append(reference).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
