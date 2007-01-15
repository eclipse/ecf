package org.eclipse.ecf.provider.irc.internal.bot;

import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;
import org.eclipse.ecf.provider.irc.bot.handler.ICommandHandler;

public interface ICommandEntry {
	
	public String getExpression();
	
	public ICommandHandler getHandler();
	
	public void execute(String message, IChatRoomMessageSender sender);

}
