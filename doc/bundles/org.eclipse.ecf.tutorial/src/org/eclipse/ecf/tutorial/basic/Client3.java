package org.eclipse.ecf.tutorial.basic;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.example.clients.IMessageReceiver;
import org.eclipse.ecf.example.clients.XMPPClient;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class Client3 extends XMPPClient {
	
	private static final String DEFAULT_PASSWORD = "eclipsecon";
	private static final String DEFAULT_USERNAME = "eclipsecon@ecf.eclipse.org";
	
	public Client3() {
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
	}
	public void createAndConnect() throws ECFException {
		super.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
	}
}
