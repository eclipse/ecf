/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tutorial.basic;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.example.clients.IMessageReceiver;
import org.eclipse.ecf.example.clients.XMPPClient;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.roster.IRosterEntry;
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
			public void handlePresence(ID fromID, IPresence presence) {
				System.out.println("handlePresence("+fromID+","+presence+")");
			}
			public void handleRosterEntryAdd(IRosterEntry entry) {
				System.out.println("handleRosterEntryAdd("+entry+")");
			}
			public void handleRosterEntryRemove(IRosterEntry entry) {
				System.out.println("handleRosterEntryRemove("+entry+")");
			}
			public void handleRosterEntryUpdate(IRosterEntry entry) {
				System.out.println("handleRosterEntryUpdate("+entry+")");
			}});
	}
	public void createAndConnect() throws ECFException {
		super.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
	}
}
