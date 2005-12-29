package org.eclipse.ecf.ds;

import org.eclipse.ecf.core.identity.ID;

public interface IChannelListener {
	public void handleMessage(ID channelID, byte [] message);
	public void handleMemberJoined(ID channelID, ID joined);
	public void handleMemberDeparted(ID channelID, ID departed);
}
