package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.identity.ID;

public interface IParticipantListener {
	
	/**
	 * Notification that a presence update has been received
	 * @param fromID
	 * @param presence
	 */
    public void handlePresence(ID fromID, IPresence presence);
}
