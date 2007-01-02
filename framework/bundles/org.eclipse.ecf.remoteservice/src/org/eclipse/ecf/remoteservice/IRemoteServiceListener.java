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

package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;

/**
 * Listener for remote service changes (register and unregister).
 * 
 */
public interface IRemoteServiceListener {
	/**
	 * Handle remote service events. The remote service events are
	 * IRemoteServiceRegisterEvent and IRemoteServiceUnregisterEvent.
	 * 
	 * @param event
	 *            the event. Will not be <code>null</code> .
	 */
	public void handleServiceEvent(IRemoteServiceEvent event);
}
