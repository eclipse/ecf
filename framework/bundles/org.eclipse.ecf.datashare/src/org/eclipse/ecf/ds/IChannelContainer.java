package org.eclipse.ecf.ds;

import java.util.Map;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

public interface IChannelContainer {
	public IChannel createChannel(ID channelID, IChannelListener listener, Map properties) throws ECFException;
	public IChannel getChannel(ID channelID);
	public boolean disposeChannel(ID channelID);
}
