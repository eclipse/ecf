/****************************************************************************
 * Copyright (c) 2013 Composent, Inc. and others.
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
import org.eclipse.ecf.provider.generic.SSLServerSOContainer;
import org.eclipse.ecf.provider.generic.SSLServerSOContainerGroup;

/**
 * 
 * @since 6.0
 *
 */
public class SSLGenericServerContainer extends SSLServerSOContainer {

	final SSLAbstractGenericServer abstractGenericServer;

	private IContainerListener departedListener = new IContainerListener() {
		public void handleEvent(IContainerEvent event) {
			if (event instanceof IContainerDisconnectedEvent) {
				IContainerDisconnectedEvent de = (IContainerDisconnectedEvent) event;
				SSLGenericServerContainer.this.abstractGenericServer.handleDisconnect(de.getTargetID());
			} else if (event instanceof IContainerEjectedEvent) {
				IContainerEjectedEvent de = (IContainerEjectedEvent) event;
				SSLGenericServerContainer.this.abstractGenericServer.handleEject(de.getTargetID());
			}
		}
	};

	public SSLGenericServerContainer(SSLAbstractGenericServer abstractGenericServer, ISharedObjectContainerConfig config, SSLServerSOContainerGroup listener, String path, int keepAlive) {
		super(config, listener, path, keepAlive);
		this.abstractGenericServer = abstractGenericServer;
		addListener(departedListener);
	}

	public void dispose() {
		removeListener(departedListener);
		super.dispose();
	}
}