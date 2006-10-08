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
package org.eclipse.ecf.presence.chat;

import org.eclipse.core.runtime.IAdaptable;

public interface IChatRoomManager extends IAdaptable {
	
	/**
	 * Get parent IChatRoomManager.  If this manager is the root, then this method
	 * returns null.
	 * @return IChatRoomManager instance if this manager has a parent.  Returns null if this manager is the
	 * root of the hierarchy 
	 */
	public IChatRoomManager getParent();
	/**
	 * Get any children managers of this IChatRoomManager.  If this chat room manager
	 * has children chat room managers, then the returned array will have more than zero elements.
	 * If this IChatRoomManager has no children, then a zero-length array will be returned.
	 * 
	 * @return IChatRoomManager[] of children for this chat room manager.  If no children,
	 * a zero-length array will be returned.  Null will not be returned.
	 */
	public IChatRoomManager[] getChildren();
	
	/**
	 * Get detailed room info for given room name
	 * @param roomname the name of the room to get detailed info for.  If null, the room info is assumed
	 * to be a room associated with the chat room manager instance itself.  For example, for IRC, the
	 * chat room manager is also a chat room where message can be sent/received
	 * @return IRoomInfo an instance that provides the given info.  Null if no chat room info associated
	 * with given name or null
	 */
	public IRoomInfo getChatRoomInfo(String roomname);
	/**
	 * Get detailed room info for all chat rooms associated with this manager
	 * @return IRoomInfo an array of instances that provide info for all chat rooms
	 */
	public IRoomInfo[] getChatRoomsInfo();
	
	// XXX these two methods should ultimately be added to IChatRoomManager
	//public void addInvitationListener(IInvitationListener listener);
	//public IChatRoomContainer createChatRoomContainer() throws ContainerInstantiationException;
}
