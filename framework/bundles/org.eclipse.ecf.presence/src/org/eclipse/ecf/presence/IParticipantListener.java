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
package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.identity.ID;

public interface IParticipantListener {

	/**
	 * Notification that a presence update has been received
	 * 
	 * @param fromID the ID of the sender of the presence update
	 * @param presence the presence information for the sender
	 */
	public void handlePresence(ID fromID, IPresence presence);
}
