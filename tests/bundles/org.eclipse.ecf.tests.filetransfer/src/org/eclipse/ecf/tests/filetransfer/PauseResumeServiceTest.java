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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IFileTransferPausable;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceivePausedEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveResumedEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public class PauseResumeServiceTest extends ContainerAbstractTestCase {

	private static final String HTTP_RETRIEVE = "http://www.eclipse.org/ecf/ip_log.html";
	private static final String HTTPS_RETRIEVE = "https://bugs.eclipse.org/bugs";

	//private static final String EFS_RETRIEVE = "efs:file://c:/foo.txt";

	File tmpFile = null;

	private IRetrieveFileTransfer transferInstance;

	protected IRetrieveFileTransfer getTransferInstance() {
		return Activator.getDefault().getRetrieveFileTransferFactory().newInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		transferInstance = getTransferInstance();
		tmpFile = File.createTempFile("ECFTest", "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		tmpFile = null;
		session = null;
		pausable = null;
	}

	List receiveStartEvents = new ArrayList();

	List receiveDataEvents = new ArrayList();

	List receiveDoneEvents = new ArrayList();

	IIncomingFileTransfer session = null;

	IFileTransferPausable pausable = null;

	Object synch = new Object();

	protected void startPauseThread() {
		final Thread t = new Thread(new Runnable() {
			public void run() {
				if (session == null || pausable == null)
					return;
				try {
					Thread.sleep(1000);
					System.out.println("pausable.pause returns=" + pausable.pause());
					Thread.sleep(2000);
					System.out.println("pausable.resume returns=" + pausable.resume());
					Thread.sleep(3000);
					System.out.println("pause thread exiting");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					synchronized (synch) {
						synch.notify();
					}
				}
			}
		});
		t.start();
	}

	protected void testReceiveHttp(String url) throws Exception {
		assertNotNull(transferInstance);
		final IFileTransferListener listener = new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				if (event instanceof IIncomingFileTransferReceiveStartEvent) {
					IIncomingFileTransferReceiveStartEvent rse = (IIncomingFileTransferReceiveStartEvent) event;
					receiveStartEvents.add(rse);
					assertNotNull(rse.getFileID());
					assertNotNull(rse.getFileID().getFilename());
					try {
						session = rse.receive(System.out);
						pausable = (IFileTransferPausable) session.getAdapter(IFileTransferPausable.class);
						//rse.receive(tmpFile);
					} catch (IOException e) {
						fail(e.getLocalizedMessage());
					}
				} else if (event instanceof IIncomingFileTransferReceiveDataEvent) {
					receiveDataEvents.add(event);
					if (session.getPercentComplete() > 0.5) {
						// start other thread to do pause/resume
						if (pausable != null) {
							pausable.pause();
						} else {
							System.out.println("pausable not supported");
						}
					}
				} else if (event instanceof IIncomingFileTransferReceiveDoneEvent) {
					receiveDoneEvents.add(event);
				} else if (event instanceof IIncomingFileTransferReceivePausedEvent) {
					System.out.println("Transfer paused event=" + event);
				} else if (event instanceof IIncomingFileTransferReceiveResumedEvent) {
					try {
						IIncomingFileTransferReceiveStartEvent rse = (IIncomingFileTransferReceiveStartEvent) event;
						session = rse.receive(System.out);
					} catch (Exception e) {
						fail(e.getLocalizedMessage());
					}
				}
			}
		};

		transferInstance.sendRetrieveRequest(FileIDFactory.getDefault().createFileID(transferInstance.getRetrieveNamespace(), url), listener, null);
		// Wait for 5 seconds

		try {
			synchronized (synch) {
				synch.wait();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		sleep(5000, "Starting 5 second wait", "Ending 5 second wait");

		//assertHasEvent(receiveStartEvents, IIncomingFileTransferReceiveStartEvent.class);
		//assertHasMoreThanEventCount(receiveDataEvents, IIncomingFileTransferReceiveDataEvent.class, 0);
		//assertHasEvent(receiveDoneEvents, IIncomingFileTransferReceiveDoneEvent.class);

		//assertTrue(tmpFile.exists());
		//assertTrue(tmpFile.length() > 0);
	}

	public void testReceiveFile() throws Exception {
		testReceiveHttp(HTTP_RETRIEVE);
	}

	public void testHttpsReceiveFile() throws Exception {
		testReceiveHttp(HTTPS_RETRIEVE);
	}
	/*
	public void testEFSReceiveFile() throws Exception {
		testReceiveHttp(EFS_RETRIEVE);
	}
	*/
}
