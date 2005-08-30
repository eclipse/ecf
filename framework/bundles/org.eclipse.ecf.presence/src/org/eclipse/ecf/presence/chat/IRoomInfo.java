package org.eclipse.ecf.presence.chat;

import org.eclipse.ecf.core.identity.ID;

public interface IRoomInfo {
	/**
	 * Get a description for this room
	 * @return String description
	 */
	public String getDescription();
	/**
	 * Get subject/topic for room
	 * @return String topic
	 */
	public String getSubject();
	/**
	 * Get the ID associated with this room
	 * @return ID for room
	 */
	public ID getRoomID();
	/**
	 * Get a count of the current number of room participants
	 * @return int number of participants
	 */
	public int getParticipantsCount();
	/**
	 * Get a long name for room
	 * @return String name
	 */
	public String getName();
	/**
	 *
	 * @return true if room is persistent, false otherwise
	 */
	public boolean isPersistent();
	/**
	 *
	 * @return true if room connect requires password, false otherwise
	 */
	public boolean requiresPassword();
	/**
	 *
	 * @return true if room is moderated, false otherwise
	 */
	public boolean isModerated();
	/**
	 * 
	 * @return ID of service we are currently connected to
	 */
	public ID getConnectedID();
}
