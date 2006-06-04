package org.eclipse.ecf.example.clients;

import java.io.IOException;

import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.AbstractSharedObject;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsgEvent;
import org.eclipse.ecf.core.util.Event;

public class TrivialSharedObject extends AbstractSharedObject {

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

	protected Event handleSharedObjectMsgEvent(ISharedObjectMessageEvent event) {
		try {
			((SharedObjectMsgEvent) event.getData()).getSharedObjectMsg()
					.invoke(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}

	protected void handleMessage(String message) {
		// XXX this should call the view back to display the message/do other things, etc
		System.out.println("TrivialSharedObject.handleMessage(" + message + ")");
	}

}
