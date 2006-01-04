package org.eclipse.ecf.ds.events;

import org.eclipse.ecf.core.identity.ID;

public interface IChannelGroupDepartEvent extends IChannelEvent {
	public ID getTargetID();
}
