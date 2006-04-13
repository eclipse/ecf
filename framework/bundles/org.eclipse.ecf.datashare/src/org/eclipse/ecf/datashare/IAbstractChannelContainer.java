package org.eclipse.ecf.datashare;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;

public interface IAbstractChannelContainer {
	/**
	 * Get expected Namespace for channel ID creation
	 * @return Namespace that can be used to create channel ID instances
	 * for this channel container
	 */
	public Namespace getChannelNamespace();
	
	/**
	 * Get IChannel with given channelID.  
	 * @param channelID the ID of the channel to get
	 * @return IChannel of channel within container with given ID.  Returns null if channel not found.
	 */
	public IChannel getChannel(ID channelID);
	/**
	 * Remove channel with given ID
	 * @param channelID the ID of the channel to Remove within this container
	 * @return true if channel found and Removed.  False if channel not found within container.
	 */
	public boolean removeChannel(ID channelID);
}
