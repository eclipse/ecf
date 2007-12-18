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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;

public class MultiRetrieveTest extends AbstractRetrieveTestCase {

	private static final String TESTSRCPATH = "test.src";
	private static final String TESTTARGETPATH = "test.target";

	private static List srcFiles = new ArrayList();

	File targetDir = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		final Enumeration files = Activator.getDefault().getBundle().getEntryPaths(TESTSRCPATH);
		for (; files.hasMoreElements();) {
			final URL url = Activator.getDefault().getBundle().getEntry((String) files.nextElement());
			final String file = url.getFile();
			if (file != null && !file.equals("") && !file.endsWith("/")) {
				srcFiles.add(url.toExternalForm());
			}
		}
		// Make target directory if it's not there
		targetDir = new File(TESTTARGETPATH);
		targetDir.mkdirs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		targetDir.delete();
	}

	protected void printFileInfo(String prefix, IFileTransferEvent event, File targetFile) {
		System.out.println(prefix + ";" + event + ";length=" + targetFile.length() + ";file=" + targetFile.getAbsolutePath());
	}

	File srcFile = null;
	File targetFile = null;
	BufferedOutputStream bufferedStream = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractRetrieveTestCase#handleStartEvent(org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent)
	 */
	protected void handleStartEvent(IIncomingFileTransferReceiveStartEvent event) {
		super.handleStartEvent(event);
		targetFile = new File(TESTTARGETPATH, event.getFileID().getFilename());
		try {
			bufferedStream = new BufferedOutputStream(new FileOutputStream(targetFile));
			incomingFileTransfer = event.receive(bufferedStream);
		} catch (final IOException e) {
			fail(e.getLocalizedMessage());
		}
		printFileInfo("START", event, targetFile);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractRetrieveTestCase#handleDataEvent(org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent)
	 */
	protected void handleDataEvent(IIncomingFileTransferReceiveDataEvent event) {
		super.handleDataEvent(event);
		printFileInfo("DATA", event, targetFile);
	}

	protected void handleDoneEvent(IIncomingFileTransferReceiveDoneEvent event) {
		super.handleDoneEvent(event);
		try {
			bufferedStream.flush();
			printFileInfo("DONE", event, targetFile);
			assertTrue(srcFile.length() == targetFile.length());
		} catch (final IOException e) {
			fail(e.getLocalizedMessage());
		}
	}

	protected void testReceive(String url) throws Exception {
		new File(url);
		assertNotNull(retrieveAdapter);
		final IFileTransferListener listener = createFileTransferListener();
		final IFileID fileID = createFileID(new URL(url));
		retrieveAdapter.sendRetrieveRequest(fileID, listener, null);

		waitForDone(20000);
	}

	public void testReceives() throws Exception {
		for (final Iterator i = srcFiles.iterator(); i.hasNext();) {
			testReceive((String) i.next());
		}
	}

}
