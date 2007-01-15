package org.eclipse.ecf.provider.irc.bot.handler;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;

public interface ICommandHandler {
	
	public void execute(String command, IChatRoomMessageSender sender) throws ECFException;

}
