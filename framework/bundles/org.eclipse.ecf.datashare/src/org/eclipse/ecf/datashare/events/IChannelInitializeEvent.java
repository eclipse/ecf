package org.eclipse.ecf.datashare.events;

import org.eclipse.ecf.core.identity.ID;

public interface IChannelInitializeEvent extends IChannelEvent {
	public ID[] getGroupMembers();
}
