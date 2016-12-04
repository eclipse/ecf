/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.provider.filetransfer.scp;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IOutgoingFileTransferSendDoneEvent;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.ISendFileTransfer;
import org.eclipse.ecf.internal.provider.filetransfer.scp.ScpSendFileTransferFactory;

/**
 * 
 */
public class SCPOutgoingTest2 extends AbstractSCPTest {

	private String localSendFile = System.getProperty("localSendFile", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$
	private String targetSendFile = System.getProperty("targetSendFile", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$

	ISendFileTransfer sender = null;
	IFileTransferListener senderTransferListener = null;
	IFileID targetID;

	protected void syncNotify() {
		super.syncNotify();
	}

	private IFileTransferListener getFileTransferListener(final String prefix) {
		return new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				System.out.println(prefix + ".handleTransferEvent(" + event + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				if (event instanceof IOutgoingFileTransferSendDoneEvent) {
					System.out.println(prefix + " DONE"); //$NON-NLS-1$
					syncNotify();
				}
			}
		};
	}

	protected void setUp() throws Exception {
		super.setUp();
		sender = new ScpSendFileTransferFactory().newInstance();
		senderTransferListener = getFileTransferListener("localhost"); //$NON-NLS-1$
		String targetURL = "scp://" + username + "@" + host + (targetSendFile.startsWith("/") ? "" : "/") + targetSendFile; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		targetID = FileIDFactory.getDefault().createFileID(sender.getOutgoingNamespace(), new URL(targetURL));
		sender.setConnectContextForAuthentication(ConnectContextFactory.createPasswordConnectContext(password));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		sender = null;
		senderTransferListener = null;
	}

	public void testSend() throws Exception {
		System.out.println("sending to targetID=" + targetID); //$NON-NLS-1$
		Hashtable ht = new Hashtable();
		ht.put("test", "testa");
		sender.sendOutgoingRequest(targetID, new File(localSendFile), senderTransferListener, ht);

		syncWaitForNotify(20000);
	}
}
