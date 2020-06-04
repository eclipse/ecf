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
import org.eclipse.ecf.core.sharedobject.*;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

public class RemoteServiceContainerAdapterFactory extends AbstractSharedObjectContainerAdapterFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.sharedobject.AbstractSharedObjectContainerAdapterFactory#createAdapter(org.eclipse.ecf.core.sharedobject.ISharedObjectContainer,
	 *      java.lang.Class, org.eclipse.ecf.core.identity.ID)
	 */
	protected ISharedObject createAdapter(ISharedObjectContainer container, Class adapterType, ID adapterID) {
		if (adapterType.equals(IRemoteServiceContainerAdapter.class)) {
			return new RegistrySharedObject();
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] {IRemoteServiceContainerAdapter.class};
	}

}
