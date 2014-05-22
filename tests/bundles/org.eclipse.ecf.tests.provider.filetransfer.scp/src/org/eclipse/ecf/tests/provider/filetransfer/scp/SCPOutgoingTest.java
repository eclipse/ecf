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
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.ISendFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

/**
 * 
 */
public class SCPOutgoingTest extends ContainerAbstractTestCase {

	String sendFile = System.getProperty("sendFile", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$

	String host = System.getProperty("host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
	String file = System.getProperty("file", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$

	// URL (example:  scp://slewis@ecf1.osuosl.org/test.txt 
	String username = System.getProperty("username", "nobody"); //$NON-NLS-1$ //$NON-NLS-2$
	String password = System.getProperty("password", "password"); //$NON-NLS-1$ //$NON-NLS-2$

	protected ISendFileTransferContainerAdapter adapter = null;
	protected IFileTransferListener senderTransferListener = null;

	protected IFileTransferListener getFileTransferListener(final String prefix) {
		return new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				System.out.println(prefix + ".handleTransferEvent(" + event + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
	}

	protected void setUp() throws Exception {
		super.setUp();
		final IContainer container = ContainerFactory.getDefault().createContainer();
		adapter = (ISendFileTransferContainerAdapter) container.getAdapter(ISendFileTransferContainerAdapter.class);
		senderTransferListener = getFileTransferListener("localhost"); //$NON-NLS-1$
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		adapter = null;
		senderTransferListener = null;
	}

	public void testSend() throws Exception {
		String targetURL = "scp://" + username + "@" + host + (file.startsWith("/") ? "" : "/") + file; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		System.out.println("Sending to " + targetURL); //$NON-NLS-1$
		final IFileID targetID = FileIDFactory.getDefault().createFileID(adapter.getOutgoingNamespace(), new URL(targetURL));
		adapter.setConnectContextForAuthentication(ConnectContextFactory.createPasswordConnectContext(password));
		adapter.sendOutgoingRequest(targetID, new File(sendFile), senderTransferListener, null);

		sleep(10000);
	}
}
