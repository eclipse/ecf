package org.eclipse.ecf.ds;

import org.eclipse.ecf.core.identity.ID;

public interface IChannelChangeEvent extends IChannelEvent {
	public boolean hasJoined();
	public ID getTargetID();
}
