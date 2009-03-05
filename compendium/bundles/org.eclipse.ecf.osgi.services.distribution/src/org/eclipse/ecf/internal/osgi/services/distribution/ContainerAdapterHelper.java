/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
/**
 * 
 */
package org.eclipse.ecf.internal.osgi.services.distribution;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

class ContainerAdapterHelper {
	private IContainer container;
	private IRemoteServiceContainerAdapter containerAdapter;

	public ContainerAdapterHelper(IContainer c, IRemoteServiceContainerAdapter rsca) {
		this.container = c;
		this.containerAdapter = rsca;
	}

	public IContainer getContainer() {
		return container;
	}

	public IRemoteServiceContainerAdapter getRSCA() {
		return containerAdapter;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ContainerAdapterHelper[");
		buf.append("containerID=").append(getContainer().getID());
		buf.append(";rsca=").append(getRSCA()).append("]");
		return buf.toString();
	}
}