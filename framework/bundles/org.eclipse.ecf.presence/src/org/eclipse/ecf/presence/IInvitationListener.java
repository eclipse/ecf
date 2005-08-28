package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.identity.ID;

public interface IInvitationListener {
	
	/**
	 * Handle notification of a received invitation to join room
	 * @param roomID the room id associated with the invitation
	 * @param from the id of the sender
	 * @param to the id of the intended receiver
	 * @param subject a subject for the invitation
	 * @param body a message body for the invitation
	 */
	public void handleInvitationReceived(ID roomID, ID from, ID to, String subject, String body);
}
