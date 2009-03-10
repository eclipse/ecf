/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.core.IContainer;

/**
 * @since 3.0
 */
public interface IRemoteServiceContainer {

	/**
	 * Get the container instance for this remote service container.  Will
	 * not return <code>null</code>.
	 * @return IContainer for this remote service container.  Will not return <code>null</code>.
	 */
	public IContainer getContainer();

	/**
	 * Get the container adapter for this remote service container.
	 * Will not return <code>null</code>
	 * 
	 * @return IRemoteServiceContainerAdapter that is the adapter for the container
	 * returned from {@link #getContainer()}.
	 */
	public IRemoteServiceContainerAdapter getContainerAdapter();

}
