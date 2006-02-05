package org.eclipse.ecf.presence.chat;

import org.eclipse.ecf.core.IIdentifiable;
import org.eclipse.ecf.core.identity.ID;

public interface IChatRoomCategory extends IIdentifiable {
	
	public IChatRoomCategory getParent();
	public IChatRoomCategory [] getChildren();
	/**
	 * Get IDs for chat rooms available via this chat room manager
	 * @return null if no access provided to chat room identities
	 */
	public ID[] getChatRooms();
	/**
	 * Get detailed room info for given room id
	 * @param roomID the id of the room to get detailed info for
	 * @return IRoomInfo an instance that provides the given info
	 */
	public IRoomInfo getChatRoomInfo(ID roomID);
	/**
	 * Get detailed room info for all chat rooms associated with this manager
	 * @return IRoomInfo an array of instances that provide info for all chat rooms
	 */
	public IRoomInfo[] getChatRoomsInfo();

}
