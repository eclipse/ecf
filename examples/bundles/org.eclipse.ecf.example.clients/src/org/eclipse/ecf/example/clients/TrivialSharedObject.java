package org.eclipse.ecf.example.clients;

import java.io.IOException;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.BaseSharedObject;
import org.eclipse.ecf.core.sharedobject.SharedObjectInitException;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;

public class TrivialSharedObject extends BaseSharedObject {

	public TrivialSharedObject() {
		super();
	}

	protected void initialize() throws SharedObjectInitException {
		super.initialize();
	}

	public void sendMessageTo(ID targetID, String message) {
		try {
			super.sendSharedObjectMsgTo(targetID, SharedObjectMsg.createMsg(
					null, "handleMessage", message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean handleSharedObjectMsg(SharedObjectMsg msg) {
		try {
			msg.invoke(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected void handleMessage(String message) {
		// XXX this should call the view back to display the message/do other
		// things, etc
		System.out
				.println("TrivialSharedObject.handleMessage(" + message + ")");
	}

}
