/****************************************************************************
 * Copyright (c) 2004 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tutorial.basic;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.example.clients.IMessageReceiver;
import org.eclipse.ecf.example.clients.XMPPChatClient;
import org.eclipse.ecf.presence.im.IChatMessage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class Client3 extends XMPPChatClient {
	
	private static final String DEFAULT_PASSWORD = "eclipsecon";
	private static final String DEFAULT_USERNAME = "eclipsecon@ecf.eclipse.org";
	
	public Client3() {
		super(new IMessageReceiver() {
			public void handleMessage(final IChatMessage chatMessage) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(null, "XMPP message from "
								+ chatMessage.getFromID().getName(), chatMessage.getBody());
					}
				});
			}});
	}
	public void createAndConnect() throws ECFException {
		super.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
	}
}
