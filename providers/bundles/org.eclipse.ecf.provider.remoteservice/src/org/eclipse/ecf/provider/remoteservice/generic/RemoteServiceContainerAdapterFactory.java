/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import org.eclipse.ecf.core.AbstractSharedObjectContainerAdapterFactory;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

public class RemoteServiceContainerAdapterFactory extends AbstractSharedObjectContainerAdapterFactory {

	protected ISharedObject createAdapter(ISharedObjectContainer container, Class adapterType) {
		if (adapterType.equals(IRemoteServiceContainerAdapter.class)) return new RegistrySharedObject();
		else return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IRemoteServiceContainerAdapter.class };
	}
	
}
