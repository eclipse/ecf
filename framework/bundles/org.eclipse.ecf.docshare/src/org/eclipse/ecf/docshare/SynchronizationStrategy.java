package org.eclipse.ecf.docshare;

import org.eclipse.ecf.docshare.messages.UpdateMessage;

public interface SynchronizationStrategy {

	public UpdateMessage registerOutgoingMessage(UpdateMessage localMsg);

	public UpdateMessage transformIncomingMessage(UpdateMessage remoteMsg);

}
