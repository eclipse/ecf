package org.eclipse.ecf.provider.irc.container;

import org.eclipse.ecf.core.identity.ID;

public interface IRCMessageChannel {

	public void fireMessageListeners(ID sender, String msg);

}
