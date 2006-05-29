package org.eclipse.ecf.example.clients;

import java.io.IOException;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.chat.IChatMessageSender;
import org.eclipse.ecf.presence.chat.IChatRoomContainer;

public class SORobotApplication implements IPlatformRunnable, IMessageReceiver,
IMessageListener {

	private IChatMessageSender sender;
	private boolean running = false;
	private String userName;
	
	public synchronized Object run(Object args) throws Exception {
		if (args instanceof Object[]) {
			Object[] arguments = (Object[]) args;
			while (arguments.length > 0 && arguments[0] instanceof String
					&& ((String) arguments[0]).startsWith("-")) {
				System.arraycopy(arguments, 1,
						arguments = new Object[arguments.length - 1], 0,
						arguments.length);
			}
			if (arguments.length == 4) {
				if (arguments[0] instanceof String
						&& arguments[1] instanceof String
						&& arguments[2] instanceof String
						&& arguments[3] instanceof String) {
					userName = (String) arguments[0];
					String hostName = (String) arguments[1];
					String password = (String) arguments[2];
					String roomName = (String) arguments[3];
					runRobot(hostName, password, roomName);
					return new Integer(0);
				}
			}
		}
		System.out
				.println("Usage: pass in four arguments (username, hostname, password, roomname)");
		return new Integer(-1);
	}

	private void runRobot(String hostName, String password, String roomName)
			throws ECFException, Exception, InterruptedException {
		// Create client and connect to host
		XMPPChatClient client = new XMPPChatClient(this);
		client.connect(userName + "@" + hostName, password);
		// Create room of given name
		IChatRoomContainer room = client.createChatRoom(roomName);
		// Get ISharedObjectContainer adapter and add TrivialSharedObject to container
		addSharedObject((ISharedObjectContainer) room.getAdapter(ISharedObjectContainer.class));
		// Add this as message listener for room
		room.addMessageListener(this);
		// Connect to room with given room ID
		room.connect(client.getChatRoomInfo().getRoomID(),null);
	    // Get message sender interface for room
		sender = room.getChatMessageSender();
		running = true;
		// Send initial message for room
		sender
				.sendMessage("Hi, I'm a robot. To get rid of me, send me a direct message.");
		
		while (running) {
			wait();
		}
	}

	protected void addSharedObject(ISharedObjectContainer soContainer) throws ECFException {
		if (soContainer != null) {
			// Create a new GUID for new TrivialSharedObject instance
			ID newID = IDFactory.getDefault().createGUID();
			// Create TrivialSharedObject
			TrivialSharedObject tso = new TrivialSharedObject();
			// Add shared object to container
			soContainer.getSharedObjectManager().addSharedObject(newID, tso, null);
		}
	}
	
	public synchronized void handleMessage(String from, String msg) {
		// direct message
		try {
			sender.sendMessage("gotta run");
		} catch (IOException e) {
			e.printStackTrace();
		}
		running = false;
		notifyAll();
	}

	public void handleMessage(ID fromID, ID toID, Type type, String subject,
			String messageBody) {
		System.out.println("Got message from "+fromID+": "+messageBody);
	}


}
