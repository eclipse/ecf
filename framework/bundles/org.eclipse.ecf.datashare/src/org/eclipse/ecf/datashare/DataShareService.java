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

import org.eclipse.ecf.core.identity.ID;

/**
 * Allows the client to join a data sharing session. This interface is
 * implemented by service providers; clients typically acquire an instance in a
 * provider-specific way.
 * 
 * @see org.eclipse.ecf.datashare.Participant
 * @see org.eclipse.ecf.datashare.Session
 */
public interface DataShareService {

	/**
	 * Connects the given participant instance to the identified data sharing
	 * session.
	 * 
	 * @param sessionID
	 *            unique identifier of a data sharing session to join
	 * @param participant
	 *            client's "agent" that allows the client to participate in data
	 *            sharing
	 * @param data
	 *            arbitrary data to pass to existing participants via
	 *            {@link Participant#joined(Object)}
	 * @throws JoinException
	 *             if unable to join the specified session
	 */
	void joinSession(ID sessionID, Participant participant, Object data)
			throws JoinException;
}
