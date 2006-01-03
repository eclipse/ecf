package org.eclipse.ecf.ds;

import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;

public interface IChannelEvent extends IContainerEvent {
	public ID getChannelID();
}
