package org.eclipse.ecf.ds;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

public interface IChannelContainer extends IContainer {
	public IChannel createChannel(ID channelID, IChannelListener handler) throws ECFException;
	public IChannel getChannel(ID channelID);
	public void removeChannel(ID channelID);
}
