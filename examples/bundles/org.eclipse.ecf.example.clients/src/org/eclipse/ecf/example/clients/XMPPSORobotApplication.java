package org.eclipse.ecf.example.clients;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IRosterEntry;

public class XMPPSORobotApplication implements IPlatformRunnable, IMessageReceiver {

	private boolean running = false;
	private String userName;
	private XMPPClient client;
	private String targetIMUser;
	private TrivialSharedObject sharedObject = null;
	
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
					String targetName = (String) arguments[3];
					runRobot(hostName, password, targetName);
					return new Integer(0);
				}
			}
		}
		System.out
				.println("Usage: pass in four arguments (username, hostname, password, targetIMUser)");
		return new Integer(-1);
	}

	private IPresenceListener getPresenceListener() {
		return new IPresenceListener() {
			public void handleContainerDeparted(ID departedContainer) {
			}
			public void handleContainerJoined(ID joinedContainer) {
			}
			public void handleRosterEntry(IRosterEntry entry) {
			}
			public void handleSetRosterEntry(IRosterEntry entry) {
			}
			public void handlePresence(ID fromID, IPresence presence) {
			}};
	}
	private void runRobot(String hostName, String password, String targetIMUser)
			throws ECFException, Exception, InterruptedException {
		// Create client and connect to host
		client = new XMPPClient(this, getPresenceListener());
		client.setupContainer();
		client.setupPresence();
		// Get ISharedObjectContainer adapter and add TrivialSharedObject to container
		ISharedObjectContainer socontainer = (ISharedObjectContainer) client.getContainer().getAdapter(ISharedObjectContainer.class);
		createTrivialSharedObjectForContainer(socontainer);

		client.doConnect(userName + "@" + hostName, password);
		
		this.targetIMUser = targetIMUser;
		// Send initial message for room
		client.sendMessage(targetIMUser,"Hi, I'm an im robot. To get rid of me, just send an IM back");
		
		running = true;
		int count = 0;
		while (running && count++ < 10) {
			sendSOMessage();
			wait(10000);
		}
	}

    protected void sendSOMessage() {
    	if (sharedObject != null) {
    		sharedObject.sendMessageTo(client.getID(targetIMUser),"hello there");
    	}
    }
	protected void createTrivialSharedObjectForContainer(ISharedObjectContainer soContainer) throws ECFException {
		if (soContainer != null) {
			// Create a new GUID for new TrivialSharedObject instance
			ID newID = IDFactory.getDefault().createStringID(TrivialSharedObject.class.getName());
			// Create TrivialSharedObject
			sharedObject = new TrivialSharedObject();
			// Add shared object to container
			soContainer.getSharedObjectManager().addSharedObject(newID, sharedObject, null);
		}
	}
	
	public synchronized void handleMessage(String from, String msg) {
		// direct message
		//client.sendMessage(from,"gotta run");
		//running = false;
		notifyAll();
	}

}
