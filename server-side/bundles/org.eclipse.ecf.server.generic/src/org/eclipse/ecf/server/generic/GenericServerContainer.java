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
/**
 * 
 */
package org.eclipse.ecf.server.generic;

import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.*;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.generic.TCPServerSOContainer;
import org.eclipse.ecf.provider.generic.TCPServerSOContainerGroup;

/**
 * 
 * @since 2.0
 *
 */
public class GenericServerContainer extends TCPServerSOContainer {

	final AbstractGenericServer abstractGenericServer;

	private IContainerListener departedListener = new IContainerListener() {
		public void handleEvent(IContainerEvent event) {
			if (event instanceof IContainerDisconnectedEvent) {
				IContainerDisconnectedEvent de = (IContainerDisconnectedEvent) event;
				GenericServerContainer.this.abstractGenericServer.handleDisconnect(de.getTargetID());
			} else if (event instanceof IContainerEjectedEvent) {
				IContainerEjectedEvent de = (IContainerEjectedEvent) event;
				GenericServerContainer.this.abstractGenericServer.handleEject(de.getTargetID());
			}
		}
	};

	public GenericServerContainer(AbstractGenericServer abstractGenericServer, ISharedObjectContainerConfig config, TCPServerSOContainerGroup listener, String path, int keepAlive) {
		super(config, listener, path, keepAlive);
		this.abstractGenericServer = abstractGenericServer;
		addListener(departedListener);
	}

	public void dispose() {
		removeListener(departedListener);
		super.dispose();
	}
}