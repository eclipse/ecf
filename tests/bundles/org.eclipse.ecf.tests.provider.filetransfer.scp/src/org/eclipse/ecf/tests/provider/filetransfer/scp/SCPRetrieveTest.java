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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.events.*;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public class SCPRetrieveTest extends ContainerAbstractTestCase {

	String host = System.getProperty("host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
	String file = System.getProperty("file", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$

	// URL (example:  scp://slewis@ecf1.osuosl.org/test.txt 
	String username = System.getProperty("username", "nobody"); //$NON-NLS-1$ //$NON-NLS-2$
	String password = System.getProperty("password", "password"); //$NON-NLS-1$ //$NON-NLS-2$

	IRetrieveFileTransferContainerAdapter adapter = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		final IContainer container = ContainerFactory.getDefault().createContainer();
		adapter = (IRetrieveFileTransferContainerAdapter) container.getAdapter(IRetrieveFileTransferContainerAdapter.class);
		receiveStartEvents = new ArrayList();
		receiveDataEvents = new ArrayList();
		receiveDoneEvents = new ArrayList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		receiveStartEvents.clear();
		receiveDataEvents.clear();
		receiveDoneEvents.clear();
		adapter = null;
	}

	List receiveStartEvents;

	List receiveDataEvents;

	List receiveDoneEvents;

	public void testReceive() throws Exception {
		assertNotNull(adapter);
		final IFileTransferListener listener = new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				if (event instanceof IIncomingFileTransferReceiveStartEvent) {
					IIncomingFileTransferReceiveStartEvent rse = (IIncomingFileTransferReceiveStartEvent) event;
					receiveStartEvents.add(rse);
					assertNotNull(rse.getFileID());
					assertNotNull(rse.getFileID().getFilename());
					try {
						rse.receive(System.out);
					} catch (IOException e) {
						fail(e.getLocalizedMessage());
					}
				} else if (event instanceof IIncomingFileTransferReceiveDataEvent) {
					receiveDataEvents.add(event);
				} else if (event instanceof IIncomingFileTransferReceiveDoneEvent) {
					receiveDoneEvents.add(event);
					syncNotify();
				}
			}
		};

		String targetURL = "scp://" + host + (file.startsWith("/") ? "" : "/") + file; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		System.out.println("Retrieving from " + targetURL + " with username=" + username); //$NON-NLS-1$ //$NON-NLS-2$
		adapter.setConnectContextForAuthentication(ConnectContextFactory.createUsernamePasswordConnectContext(username, password));
		adapter.sendRetrieveRequest(FileIDFactory.getDefault().createFileID(adapter.getRetrieveNamespace(), targetURL), listener, null);

		syncWaitForNotify(60000);

		assertHasEvent(receiveStartEvents, IIncomingFileTransferReceiveStartEvent.class);
		assertHasMoreThanEventCount(receiveDataEvents, IIncomingFileTransferReceiveDataEvent.class, 0);
		assertHasEvent(receiveDoneEvents, IIncomingFileTransferReceiveDoneEvent.class);

	}

	public void syncNotify() {
		super.syncNotify();
	}
}
