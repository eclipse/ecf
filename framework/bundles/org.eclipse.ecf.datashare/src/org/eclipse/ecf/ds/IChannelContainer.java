package org.eclipse.ecf.ds;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

public interface IChannelContainer extends IContainer {
	public IChannel createChannel(ID channelID, IChannelHandler handler) throws ECFException;
}
