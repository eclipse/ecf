package org.eclipse.ecf.ds;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.identity.ID;

public interface IChannelHandler {
	public void handleMessage(ID channelID, byte [] message);
	public void handleConnect(ID channelID, ID groupID);
	public void handleDisconnect(ID channelID, IStatus status);
	public void handleMemberJoined(ID channelID, ID joined);
	public void handleMemberDeparted(ID channelID, ID departed);
}
