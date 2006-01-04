package org.eclipse.ecf.ds;

import org.eclipse.ecf.ds.events.IChannelEvent;


public interface IChannelListener {
	public void handleChannelEvent(IChannelEvent event);
}
