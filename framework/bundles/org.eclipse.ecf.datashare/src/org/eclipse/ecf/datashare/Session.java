/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare;

/**
 * <p>
 * Represents a data sharing session. Allows a
 * {@link org.eclipse.ecf.datashare.Participant Participant} to broadcast
 * messages (typically, shared data updates) to other connected participants.
 * </p>
 * <p>
 * A Participant is
 * {@link Participant#initialize(Session, Object[]) initialized} with a valid
 * Session instance; this in turn becomes invalid when the Participant is
 * {@link Participant#dispose() disposed}.
 * </p>
 * <p>
 * This interface is intended for service providers and should not be
 * implemented by clients.
 * </p>
 * 
 * @see org.eclipse.ecf.datashare.Participant
 */
public interface Session {

	/**
	 * Broadcasts a message (typically, an update to the shared data) to other
	 * connected {@link Participant participants}.
	 * 
	 * @param data
	 *            the message to broadcast
	 * @throws SendException
	 *             if the message could not be successfully broadcast
	 */
	void send(Object data) throws SendException;

	/**
	 * Requests to leave the data sharing session. As a result, the
	 * {@link Participant participant} is {@link Participant#dispose() disposed}.
	 */
	void leave();
}
