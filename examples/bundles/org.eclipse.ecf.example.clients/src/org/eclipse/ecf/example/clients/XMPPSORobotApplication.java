package org.eclipse.ecf.example.clients;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.core.util.ECFException;

public class XMPPSORobotApplication implements IPlatformRunnable,
		IMessageReceiver {

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

	private void runRobot(String hostName, String password, String targetIMUser)
			throws ECFException, Exception, InterruptedException {
		// Create client and connect to host
		client = new XMPPClient(this);
		client.setupContainer();
		client.setupPresence();

		// Get ISharedObjectContainer adapter
		ISharedObjectContainer socontainer = (ISharedObjectContainer) client
				.getContainer().getAdapter(ISharedObjectContainer.class);
		// Create TrivialSharedObject instance and add to container
		createTrivialSharedObjectForContainer(socontainer);

		// Then connect
		client.doConnect(userName + "@" + hostName, password);

		this.targetIMUser = targetIMUser;
		// Send initial message for room
		client.sendMessage(targetIMUser, "Hi, I'm an IM robot");

		running = true;
		int count = 0;
		// Loop ten times and send ten 'hello there' so messages to targetIMUser
		while (running && count++ < 10) {
			sendSOMessage(count + " hello there");
			wait(10000);
		}
	}

	protected void sendSOMessage(String msg) {
		if (sharedObject != null) {
			sharedObject.sendMessageTo(client.createID(targetIMUser), msg);
		}
	}

	protected void createTrivialSharedObjectForContainer(
			ISharedObjectContainer soContainer) throws ECFException {
		if (soContainer != null) {
			// Create a new GUID for new TrivialSharedObject instance
			ID newID = IDFactory.getDefault().createStringID(
					TrivialSharedObject.class.getName());
			// Create TrivialSharedObject
			sharedObject = new TrivialSharedObject();
			// Add shared object to container
			soContainer.getSharedObjectManager().addSharedObject(newID,
					sharedObject, null);
		}
	}

	public synchronized void handleMessage(String from, String msg) {
		// direct message
		// client.sendMessage(from,"gotta run");
		// running = false;
		notifyAll();
	}

}
