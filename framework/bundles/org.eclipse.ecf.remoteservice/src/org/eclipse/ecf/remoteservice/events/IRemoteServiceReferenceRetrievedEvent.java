package org.eclipse.ecf.remoteservice.events;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.IAsyncResult;

public interface IRemoteServiceReferenceRetrievedEvent extends IRemoteServiceEvent {

	public ID[] getIDFilter();

	public String getFilter();

	public IAsyncResult getAsyncResult();
}
