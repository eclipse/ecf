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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
import org.osgi.framework.Bundle;

public class URLRetrievePauseResumeTest extends ContainerAbstractTestCase {

	// We need something sufficiently large to take a few seconds to download. 
	// Currently, around 15-20 MB seem fairly safe.
	private static final String HTTP_RETRIEVE = "http://mirror.csclub.uwaterloo.ca/eclipse/rt/ecf/3.14.4/org.eclipse.ecf.sdk_3.14.4.v20181013-2146.zip";
	private static final String HTTP_RETRIEVE_MD5 = "a6a8a64fd5784165f7e7ba0d6c7ffa1b";

	private static final String FILENAME = "foo.zip";

	private static final int PAUSE_TIME = 10000;
	private static final double RESUMED_DOWNLOAD_AMOUNT_THRESHOLD = 1.5;

	private IRetrieveFileTransfer transferInstance;

	private File incomingFile = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		transferInstance = Activator.getDefault().getRetrieveFileTransferFactory().newInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (session != null) {
			session.cancel();
		}
		session = null;
		pausable = null;
		if (incomingFile != null)
			incomingFile.delete();
		incomingFile = null;
	}

	IIncomingFileTransfer session = null;

	IFileTransferPausable pausable = null;

	FileOutputStream outs = null;

	Object notify = new Object();

	private void closeOutputStream() {
		if (outs != null) {
			try {
				outs.close();
			} catch (final IOException e) {
				fail("output stream close exception");
			} finally {
				outs = null;
			}
		}
	}

	protected void testReceiveHttp(String url) throws Exception {
		assertNotNull(transferInstance);
		final AtomicBoolean wasPaused = new AtomicBoolean(false);
		final AtomicBoolean wasResumed = new AtomicBoolean(false);
		final AtomicLong downloaded = new AtomicLong(0);
		final IFileTransferListener listener = new IFileTransferListener() {
			public void handleTransferEvent(IFileTransferEvent event) {
				if (event instanceof IIncomingFileTransferReceiveResumedEvent) {
					wasResumed.set(true);
					try {
						IIncomingFileTransferReceiveResumedEvent rse = (IIncomingFileTransferReceiveResumedEvent) event;
						session = rse.receive(outs);
					} catch (Exception e) {
						fail(e.getLocalizedMessage());
					}
				} else if (event instanceof IIncomingFileTransferReceiveStartEvent) {
					IIncomingFileTransferReceiveStartEvent rse = (IIncomingFileTransferReceiveStartEvent) event;
					try {
						incomingFile = new File(FILENAME);
						outs = new FileOutputStream(incomingFile);
						session = rse.receive(outs);
						pausable = (IFileTransferPausable) session.getAdapter(IFileTransferPausable.class);
						assertNotNull("pausable is null", pausable);
					} catch (IOException e) {
						fail(e.getLocalizedMessage());
					}
				} else if (event instanceof IIncomingFileTransferReceiveDataEvent) {
					System.out.println("data=" + event);
				} else if (event instanceof IIncomingFileTransferReceivePausedEvent) {
					System.out.println("paused=" + event);
					wasPaused.set(true);
				} else if (event instanceof IIncomingFileTransferReceiveDoneEvent) {
					closeOutputStream();
					System.out.println("done=" + event);
					synchronized (notify) {
						notify.notify();
					}
				}
				if (session != null) {
					long bytesReceived = session.getBytesReceived();
					downloaded.set(bytesReceived);
				}
			}
		};

		transferInstance.sendRetrieveRequest(FileIDFactory.getDefault().createFileID(transferInstance.getRetrieveNamespace(), url), listener, null);

		// Now if we can do pausing, then pause, wait a while and resume
		Thread.sleep(500);
		assertNotNull("pausable is null", pausable);
		System.out.println("pausable.pause()");
		boolean paused = pausable.pause();
		assertTrue("Pause failed", paused);
		Thread.sleep(500);
		assertTrue(wasPaused.get());
		long downloadedUntilPause = downloaded.get();
		System.out.println("Pausing " + PAUSE_TIME / 1000 + " seconds");
		Thread.sleep(PAUSE_TIME - 500);
		long downloadedBeforeResume = session.getBytesReceived();
		assertEquals("Download continued before resume", downloadedUntilPause, downloadedBeforeResume);
		final boolean success = pausable.resume();
		System.out.println("pausable.resume()=" + success);
		if (!success) {
			System.out.println("session=" + session);
			final Exception e = session.getException();
			System.out.println("  exception=" + e);
			if (e != null)
				e.printStackTrace();
			System.out.println("  isDone=" + session.isDone());
			fail("Resume failed");
		}
		Thread.sleep(500);
		assertTrue("Resume event was not received", wasResumed.get());
		long downloadedAfterResume = downloaded.get();
		assertTrue("Download continued before resume", downloadedAfterResume < (1+RESUMED_DOWNLOAD_AMOUNT_THRESHOLD) * downloadedUntilPause);  
		System.out.println();

		synchronized (notify) {
			notify.wait();
		}

		final Exception e = session.getException();
		if (e != null)
			throw e;

		assertTrue("Nothing received after resume", downloaded.get() > downloadedUntilPause);
		assertEquals("File corrupted", HTTP_RETRIEVE_MD5, downloadChecksum());
		Thread.sleep(500);//WIP
	}

	private String downloadChecksum() throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		DigestInputStream dis = null;
		try 
		{
			dis = new DigestInputStream(new FileInputStream(incomingFile), md);
			byte[] buffer = new byte[1024];
		    while(dis.read(buffer) != -1) {}
		} finally {
			if (dis != null) {
				dis.close();
			}
		}
		byte[] digest = md.digest();
		StringBuilder sb = new StringBuilder();
	    for (byte b : digest) {
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}

	public void testReceiveFile() throws Exception {
		testReceiveHttp(HTTP_RETRIEVE);
	}
}
