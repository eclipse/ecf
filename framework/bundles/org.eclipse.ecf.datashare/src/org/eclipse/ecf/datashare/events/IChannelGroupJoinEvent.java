package org.eclipse.ecf.datashare.events;

import org.eclipse.ecf.core.identity.ID;

public interface IChannelGroupJoinEvent extends IChannelEvent {
	public ID getTargetID();
}
