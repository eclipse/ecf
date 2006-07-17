package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;

public interface IRemoteCallListener {

	public void handleEvent(IRemoteCallEvent event);
}
