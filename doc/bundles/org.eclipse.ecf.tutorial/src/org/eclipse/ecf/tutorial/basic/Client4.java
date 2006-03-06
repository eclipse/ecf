package org.eclipse.ecf.tutorial.basic;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.example.clients.IMessageReceiver;
import org.eclipse.ecf.example.clients.XMPPClient;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IRosterEntry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class Client4 extends XMPPClient {
	
	private static final String DEFAULT_PASSWORD = "eclipsecon";
	private static final String DEFAULT_USERNAME = "eclipsecon@ecf.eclipse.org";
	
	public Client4() {
		super();
		setMessageReceiver(new IMessageReceiver() {
			public void handleMessage(final String from, final String msg) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(null, "XMPP message from "
								+ from, msg);
					}
				});
			}});
		setPresenceListener(new IPresenceListener(){
			public void handleContainerJoined(ID joinedContainer) {
				System.out.println("handleContainerJoined("+joinedContainer+")");
			}
			public void handleRosterEntry(IRosterEntry entry) {
				System.out.println("handleRosterEntry("+entry+")");
			}
			public void handleSetRosterEntry(IRosterEntry entry) {
				System.out.println("handleSetRosterEntry("+entry+")");
			}
			public void handleContainerDeparted(ID departedContainer) {
				System.out.println("handleContainerDeparted("+departedContainer+")");
			}
			public void handlePresence(ID fromID, IPresence presence) {
				System.out.println("handlePresence("+fromID+","+presence+")");
			}});
	}
	public void createAndConnect() throws ECFException {
		super.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
	}
}
