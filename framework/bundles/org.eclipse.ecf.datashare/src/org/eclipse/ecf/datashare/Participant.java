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
 * Interface for clients that wish to participate in data sharing sessions. The
 * client must provide an implementation when requesting to
 * {@link DataShareService#joinSession(ID, Participant, Object) join} a session. When
 * successful, the service will
 * {@link #initialize(Session, Object[]) initialize} the participant with a
 * valid {@link Session session} and any initial data. The participant will then
 * {@link #receive(Object) receive} zero or more messages (typically, shared
 * data updates}, and will be {@link #dispose() disposed} when it can no longer
 * participate in the session (e.g., as a result of
 * {@link Session#leave() leaving} the session, or because the session
 * terminated for some other reason).
 * </p>
 * <p>
 * The participant is {@link #joined(Object) notified} when a new particpant
 * joins the session, at which point it may return some data that would be used
 * to {@link #initialize(Session, Object[]) initialize} the new participant.
 * </p>
 * 
 * @see org.eclipse.ecf.datashare.DataShareService
 * @see org.eclipse.ecf.datashare.Session
 */
public interface Participant {

	/**
	 * Initializes this participant with a valid session. The session instance
	 * becomes invalid when the participant is {@link #dispose() disposed}.
	 * 
	 * @param session
	 *            session instance that can be used to broadcast messages to
	 *            other connected participants
	 * @param data
	 *            data obtained from other participants
	 * @see #initialize(Session, Object[])
	 */
	void initialize(Session session, Object[] data);

	/**
	 * Delivers a message broadcast by another session participant.
	 * 
	 * @param data
	 *            message (e.g., a shared data update) from another participant
	 */
	void receive(Object data);

	/**
	 * Notifies this participant that another participant is joining the
	 * session.
	 * 
	 * @param data
	 *            data provided by the joining participant
	 * @return any data to pass to the joining participant in
	 *         {@link #initialize(Session, Object[])}
	 * @throws JoinException
	 *             if the participant should not be allowed to join
	 */
	Object joined(Object data) throws JoinException;

	/**
	 * Disposes this participant when it can no longer participate in the
	 * session (e.g., as a result of leaving, or involuntary disconnect).
	 */
	void dispose();
}
