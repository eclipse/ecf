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

package org.eclipse.ecf.internal.provider.xmpp;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ISharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.provider.remoteservice.generic.RegistrySharedObject;
import org.eclipse.ecf.provider.remoteservice.generic.RemoteServiceContainerAdapterFactory;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

public class XMPPRemoteServiceAdapterFactory extends
		RemoteServiceContainerAdapterFactory {

	class XMPPRegistrySharedObject extends RegistrySharedObject {
		protected ID getLocalContainerID() {
			// For XMPP, the local container ID is its connected ID.
			return getContext().getConnectedID();
		}

	}

	public XMPPRemoteServiceAdapterFactory() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.remoteservice.generic.
	 * RemoteServiceContainerAdapterFactory
	 * #createAdapter(org.eclipse.ecf.core.sharedobject.ISharedObjectContainer,
	 * java.lang.Class, org.eclipse.ecf.core.identity.ID)
	 */
	protected ISharedObject createAdapter(ISharedObjectContainer container,
			Class adapterType, ID adapterID) {
		if (adapterType.equals(IRemoteServiceContainerAdapter.class)) {
			return new XMPPRegistrySharedObject();
		} else {
			return null;
		}
	}

}
