/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.remoteservice.events;

import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;

/**
 * Event received when remote call started.
 * 
 */
public interface IRemoteCallStartEvent extends IRemoteCallEvent {

	/**
	 * Get remote service reference used for call.
	 * 
	 * @return IRemoteServiceReference used to make call. Will not be
	 *         <code>null</code>.
	 */
	public IRemoteServiceReference getReference();

	/**
	 * Get the remote call itself.
	 * 
	 * @return IRemoteCall actually started. Will not be <code>null</code>
	 */
	public IRemoteCall getCall();

}
