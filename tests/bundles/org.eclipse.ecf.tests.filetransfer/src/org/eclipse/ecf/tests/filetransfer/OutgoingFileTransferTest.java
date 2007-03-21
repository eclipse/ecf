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

package org.eclipse.ecf.tests.filetransfer;

import java.io.File;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IFileTransferRequestEvent;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

/**
 * 
 */
public class OutgoingFileTransferTest extends ContainerAbstractTestCase {

	private static final String TESTSRCPATH = "test.src";
	private static final String TESTSRCFILE = TESTSRCPATH + "/test.txt";

	private static final String TESTTARGETPATH = "test.target";

	static final String XMPP_CONTAINER = "ecf.xmpp.smack";

	protected IOutgoingFileTransferContainerAdapter adapter0, adapter1 = null;

	protected String getClientContainerName() {
		return XMPP_CONTAINER;
	}

	protected IOutgoingFileTransferContainerAdapter getOutgoingFileTransfer(
			int client) {
		IContainer c = getClient(client);
		if (c != null)
			return (IOutgoingFileTransferContainerAdapter) c
					.getAdapter(IOutgoingFileTransferContainerAdapter.class);
		else
			return null;
	}

	protected IIncomingFileTransferRequestListener incomingListener = new IIncomingFileTransferRequestListener() {

		public void handleFileTransferRequest(IFileTransferRequestEvent event) {
			System.out.println("handleFileTransferRequest(" + event + ")");
			try {
				event.accept(new File(TESTTARGETPATH, event
						.getFileTransferInfo().getFile().getName()));
			} catch (IncomingFileTransferException e) {
				fail("exception calling accept for receive file transfer");
			}
		}

	};

	protected IFileTransferListener transferListener = new IFileTransferListener() {
		public void handleTransferEvent(IFileTransferEvent event) {
			System.out.println("handleTransferEvent(" + event + ")");
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.presence.PresenceAbstractTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		setClientCount(2);
		clients = createClients();
		adapter0 = getOutgoingFileTransfer(0);
		adapter0.addListener(incomingListener);
		adapter1 = getOutgoingFileTransfer(1);
		for (int i = 0; i < 2; i++) {
			connectClient(i);
		}
	}

	protected ID getServerConnectID(int client) {
		IContainer container = getClient(client);
		Namespace connectNamespace = container.getConnectNamespace();
		String username = getUsername(client);
		try {
			return IDFactory.getDefault().createID(connectNamespace, username);
		} catch (IDCreateException e) {
			fail("Could not create server connect ID");
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		disconnectClients();
	}

	public void testSendRequest() throws Exception {
		adapter1.sendOutgoingRequest(getServerConnectID(0), new File(
				TESTSRCFILE), transferListener, null);
		sleep(20000);
	}
}
