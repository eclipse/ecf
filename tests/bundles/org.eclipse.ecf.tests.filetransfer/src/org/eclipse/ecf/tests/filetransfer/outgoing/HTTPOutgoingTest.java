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

package org.eclipse.ecf.tests.filetransfer.outgoing;

import java.io.File;
import java.net.URL;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

/**
 * 
 */
public class HTTPOutgoingTest extends ContainerAbstractTestCase {

	private static final String TESTSRCPATH = "test.src";
	private static final String TESTSRCFILE = TESTSRCPATH + "/test.txt";

	private static final String TESTTARGETURL = "http://localhost:8080/webdav/test.txt";

	protected IOutgoingFileTransferContainerAdapter adapter = null;
	protected IFileTransferListener senderTransferListener = null;

	protected IFileTransferListener getFileTransferListener(final String prefix) {
		return new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				System.out.println(prefix + ".handleTransferEvent(" + event + ")");
				if (event instanceof IIncomingFileTransferReceiveDoneEvent) {

				}
			}
		};
	}

	protected void setUp() throws Exception {
		super.setUp();
		final IContainer container = ContainerFactory.getDefault().createContainer();
		adapter = (IOutgoingFileTransferContainerAdapter) container.getAdapter(IOutgoingFileTransferContainerAdapter.class);
		senderTransferListener = getFileTransferListener("localhost");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		adapter = null;
		senderTransferListener = null;
	}

	public void testSend() throws Exception {
		// Setup two clients.  Client 0 is the receiver, client 1 is the sender
		final IFileID targetID = FileIDFactory.getDefault().createFileID(adapter.getOutgoingNamespace(), new URL(TESTTARGETURL));
		adapter.sendOutgoingRequest(targetID, new File(TESTSRCFILE), senderTransferListener, null);

		sleep(20000);
	}

}
