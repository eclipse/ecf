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

	private static final String TESTTARGETURL = System.getProperty("url"); //$NON-NLS-1$

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		adapter = null;
	}

	List receiveStartEvents = new ArrayList();

	List receiveDataEvents = new ArrayList();

	List receiveDoneEvents = new ArrayList();

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
				}
			}
		};

		adapter.setConnectContextForAuthentication(ConnectContextFactory.createUsernamePasswordConnectContext(System.getProperty("username"), System.getProperty("password"))); //$NON-NLS-1$ //$NON-NLS-2$
		adapter.sendRetrieveRequest(FileIDFactory.getDefault().createFileID(adapter.getRetrieveNamespace(), TESTTARGETURL), listener, null);
		// Wait for 5 seconds
		sleep(5000, "Starting 5 second wait", "Ending 5 second wait"); //$NON-NLS-1$ //$NON-NLS-2$

		assertHasEvent(receiveStartEvents, IIncomingFileTransferReceiveStartEvent.class);
		assertHasMoreThanEventCount(receiveDataEvents, IIncomingFileTransferReceiveDataEvent.class, 0);
		assertHasEvent(receiveDoneEvents, IIncomingFileTransferReceiveDoneEvent.class);

	}
}
