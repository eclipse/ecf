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

import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;

/**
 * Listener for remote call events.  The IRemoteService.callAsynch
 * method supports the specification of a listener to receive and handle
 * the results of a remote call asynchronously.  When non-null instance of a
 * class implementing this interface is provided to the IRemoteService.callAsynch
 * method, it will subsequently have it's {@link #handleEvent(IRemoteCallEvent)}
 * method called with<br>
 * <ol>
 * <li>An event that implements IRemoteCallStartEvent</li>
 * <li>An event that implements IRemoteCallCompleteEvent</li>
 * </ol>
 * 
 * @see IRemoteService
 */
public interface IRemoteCallListener {

	/**
	 * Handle remote call events.  The two remote call events
	 * are IRemoteCallStartEvent, and IRemoteCallCompleteEvent
	 * 
	 * @param event the event
	 */
	public void handleEvent(IRemoteCallEvent event);
}
