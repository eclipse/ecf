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

package org.eclipse.ecf.tests.provider.filetransfer.xmpp;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransfer;
import org.eclipse.ecf.filetransfer.ISendFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.UserCancelledException;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IFileTransferRequestEvent;
import org.eclipse.ecf.filetransfer.events.IOutgoingFileTransferResponseEvent;
import org.eclipse.ecf.filetransfer.identity.FileCreateException;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

/**
 * 
 */
public class XMPPSOutgoingTest extends ContainerAbstractTestCase {

	private static final String TESTSRCPATH = "test.src";
	private static final String TESTSRCFILE = TESTSRCPATH + "/test.txt";

	private static final String TESTTARGETPATH = "test.target";

	static final String XMPP_CONTAINER = "ecf.xmpps.smack";

	protected ISendFileTransferContainerAdapter adapter0, adapter1 = null;

	protected IOutgoingFileTransfer outgoing;

	protected IFileID targetID;

	protected Object lock = new Object();

	protected String getClientContainerName() {
		return XMPP_CONTAINER;
	}

	protected ISendFileTransferContainerAdapter getOutgoingFileTransfer(int client) {
		final IContainer c = getClient(client);
		if (c != null)
			return (ISendFileTransferContainerAdapter) c.getAdapter(ISendFileTransferContainerAdapter.class);
		else
			return null;
	}

	protected IFileTransferListener getSenderFileTransferListener(final String prefix) {
		return new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				System.out.println(prefix + ".handleTransferEvent(" + event + ")");
				if (event instanceof IOutgoingFileTransferResponseEvent) {
					final IOutgoingFileTransferResponseEvent revent = (IOutgoingFileTransferResponseEvent) event;
					outgoing = revent.getSource();
					synchronized (lock) {
						lock.notify();
					}
				}
			}
		};
	}

	protected IFileTransferListener getReceiverFileTransferListener(final String prefix) {
		return new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				System.out.println(prefix + ".handleTransferEvent(" + event + ")");
			}
		};
	}

	File incomingDirectory = null;
	File incomingFile = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.ContainerAbstractTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(2);
		clients = createClients();
		adapter0 = getOutgoingFileTransfer(0);
		adapter0.addListener(requestListener);
		adapter1 = getOutgoingFileTransfer(1);
		for (int i = 0; i < 2; i++) {
			connectClient(i);
		}
		targetID = createFileID(adapter1, getServerConnectID(0), TESTSRCFILE);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		disconnectClients();
		if (incomingFile != null)
			incomingFile.delete();
		incomingFile = null;
		if (incomingDirectory != null)
			incomingDirectory.delete();
		incomingDirectory = null;
		outgoing = null;
		targetID = null;
	}

	protected IIncomingFileTransferRequestListener requestListener = new IIncomingFileTransferRequestListener() {

		public void handleFileTransferRequest(IFileTransferRequestEvent event) {
			System.out.println("receiver.handleFileTransferRequest(" + event + ")");
			incomingDirectory = new File(TESTTARGETPATH);
			incomingDirectory.mkdirs();
			incomingFile = new File(incomingDirectory, event.getFileTransferInfo().getFile().getName());
			try {
				FileOutputStream fos = new FileOutputStream(incomingFile);
				event.accept(fos, receiverTransferListener);
				//event.accept(f);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				fail("exception calling accept for receive file transfer");
			}
		}

	};

	protected IFileTransferListener senderTransferListener = getSenderFileTransferListener("sender");
	protected IFileTransferListener receiverTransferListener = getReceiverFileTransferListener("receiver");

	protected ID getServerConnectID(int client) {
		final IContainer container = getClient(client);
		final Namespace connectNamespace = container.getConnectNamespace();
		final String username = getUsername(client);
		try {
			return IDFactory.getDefault().createID(connectNamespace, username);
		} catch (final IDCreateException e) {
			e.printStackTrace(System.err);
			fail("Could not create server connect ID");
			return null;
		}
	}

	/*
		public void testOneClientToSend() throws Exception {
			// Setup one client.  Client 0 is the sender
			setClientCount(2);
			clients = createClients();
			adapter0 = getOutgoingFileTransfer(0);
			for (int i = 0; i < 1; i++) {
				// Only connect client 0 (not client 1)
				connectClient(i);
			}

			adapter0.sendOutgoingRequest(getServerConnectID(1), new File(
					TESTSRCFILE), senderTransferListener, null);
			sleep(200000);
			
			disconnectClients();

		}
	*/

	protected IFileID createFileID(ISendFileTransferContainerAdapter adapter, ID clientID, String filename) throws FileCreateException {
		return FileIDFactory.getDefault().createFileID(adapter.getOutgoingNamespace(), new Object[] {clientID, filename});
	}

	public void testTwoClientsToSendAndReceive() throws Exception {
		// Setup two clients.  Client 0 is the receiver, client 1 is the sender
		adapter1.sendOutgoingRequest(targetID, new File(TESTSRCFILE), senderTransferListener, null);

		sleep(5000);
	}

	public void testSenderCancel() throws Exception {
		// Setup two clients.  Client 0 is the receiver, client 1 is the sender
		adapter1.sendOutgoingRequest(targetID, new File(TESTSRCFILE), senderTransferListener, null);

		synchronized (lock) {
			lock.wait();
		}
		assertTrue(outgoing != null);

		// Now cancel
		outgoing.cancel();
		// wait a short while
		sleep(500);

		final Exception e = outgoing.getException();
		assertTrue(e != null && e instanceof UserCancelledException);

	}

}
