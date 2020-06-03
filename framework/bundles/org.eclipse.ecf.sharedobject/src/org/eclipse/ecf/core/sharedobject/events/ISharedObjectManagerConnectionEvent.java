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
package org.eclipse.ecf.core.sharedobject.events;

import org.eclipse.ecf.core.sharedobject.ISharedObjectConnector;
import org.eclipse.ecf.core.sharedobject.ISharedObjectManager;

/**
 * Shared object manager connection event. Instances implementing this interface
 * are sent to IContainerListeners when the
 * {@link ISharedObjectManager#connectSharedObjects(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.identity.ID[])}
 * or
 * {@link ISharedObjectManager#disconnectSharedObjects(ISharedObjectConnector)}
 * is called.
 * 
 */
public interface ISharedObjectManagerConnectionEvent extends
		ISharedObjectManagerEvent {

	/**
	 * Get the {@link ISharedObjectConnector} associated with this event
	 * 
	 * @return ISharedObjectConnector associated with this new connection
	 */
	public ISharedObjectConnector getConnector();
}
