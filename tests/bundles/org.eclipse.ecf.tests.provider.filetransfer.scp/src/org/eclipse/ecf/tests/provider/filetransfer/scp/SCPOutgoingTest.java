/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.tests.provider.filetransfer.scp;

import java.io.File;
import java.net.URL;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.ISendFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IOutgoingFileTransferSendDoneEvent;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;

/**
 * 
 */
public class SCPOutgoingTest extends AbstractSCPTest {

	private String localSendFile = System.getProperty("localSendFile", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$
	private String targetSendFile = System.getProperty("targetSendFile", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$

	ISendFileTransferContainerAdapter adapter = null;
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
		final IContainer container = ContainerFactory.getDefault().createContainer();
		adapter = (ISendFileTransferContainerAdapter) container.getAdapter(ISendFileTransferContainerAdapter.class);
		senderTransferListener = getFileTransferListener("localhost"); //$NON-NLS-1$
		String targetURL = "scp://" + username + "@" + host + (targetSendFile.startsWith("/") ? "" : "/") + targetSendFile; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		targetID = FileIDFactory.getDefault().createFileID(adapter.getOutgoingNamespace(), new URL(targetURL));
		adapter.setConnectContextForAuthentication(ConnectContextFactory.createPasswordConnectContext(password));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		adapter = null;
		senderTransferListener = null;
	}

	public void testSend() throws Exception {
		System.out.println("sending to targetID=" + targetID); //$NON-NLS-1$
		adapter.sendOutgoingRequest(targetID, new File(localSendFile), senderTransferListener, null);

		syncWaitForNotify(20000);
	}
}
