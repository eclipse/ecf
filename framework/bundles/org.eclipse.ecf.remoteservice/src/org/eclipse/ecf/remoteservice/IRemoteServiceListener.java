package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;

public interface IRemoteServiceListener {
	
	public void handleServiceEvent(IRemoteServiceEvent event);
}
