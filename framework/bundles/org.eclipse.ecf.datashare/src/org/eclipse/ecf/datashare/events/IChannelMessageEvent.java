package org.eclipse.ecf.datashare.events;

import org.eclipse.ecf.core.identity.ID;


public interface IChannelMessageEvent extends IChannelEvent {
	public ID getFromContainerID();
	public byte [] getData();
}
