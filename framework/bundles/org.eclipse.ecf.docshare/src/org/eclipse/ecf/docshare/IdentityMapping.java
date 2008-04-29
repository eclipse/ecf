package org.eclipse.ecf.docshare;

import org.eclipse.ecf.docshare.messages.UpdateMessage;

public class IdentityMapping implements SynchronizationStrategy {

	private static IdentityMapping instance;

	public static IdentityMapping getInstance() {
		if (instance == null) {
			instance = new IdentityMapping();
		}
		return instance;
	}

	public UpdateMessage registerOutgoingMessage(UpdateMessage localMsg) {
		return localMsg;
	}

	public UpdateMessage transformIncomingMessage(UpdateMessage remoteMsg) {
		return remoteMsg;
	}

}
