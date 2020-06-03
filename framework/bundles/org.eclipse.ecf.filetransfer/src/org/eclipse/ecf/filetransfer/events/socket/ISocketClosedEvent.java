/****************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.filetransfer.events.socket;


/**
 * Event issued after a socket has been closed.
 * <p>
 * The associated source returned by {@link #getSource()} is
 * the same as for the prior {@link ISocketCreatedEvent} and 
 * {@link ISocketConnectedEvent} with the same 
 * {@link #getFactorySocket()}. It may not reflect the source
 * currently using or most recently using the socket.
 * </p>

 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the ECF team.
 * </p>
 * 
 * @since 3.0
 */
public interface ISocketClosedEvent extends ISocketEvent {
	// no additional info needed.
}
