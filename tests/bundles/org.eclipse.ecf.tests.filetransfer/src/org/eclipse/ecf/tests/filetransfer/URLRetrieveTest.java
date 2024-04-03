/****************************************************************************
 * Copyright (c) 2004 Composent, Inc., and others.
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

package org.eclipse.ecf.tests.filetransfer;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;

import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.events.IFileTransferConnectStartEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;

public class URLRetrieveTest extends AbstractRetrieveTestCase {

	public static final String HTTP_RETRIEVE = "http://www.eclipse.org/ecf/ip_log.html";
	public static final String HTTP_RETRIEVE1 = "http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops/R-3.4.2-200902111700/jarprocessor.jar&url=http://ftp.osuosl.org/pub/eclipse/eclipse/downloads/drops/R-3.6.1-201009090800/jarprocessor.jar&mirror_id=272";
	public static final String HTTP_RETRIEVE_PORT = "https://www.eclipse.org:443/ecf/ip_log.html";
	private static final String HTTP_RETRIEVE_HOST_ONLY = "http://www.google.com";

	public static final String HTTPS_RETRIEVE = "https://www.eclipse.org";
	public static final String HTTP_404_FAIL_RETRIEVE = "http://www.google.com/googleliciousafdasdfasdfasdf";
	public static final String HTTP_BAD_URL = "http:ddasdf12121212";
	public static final String HTTP_MALFORMED_URL = "http://malformed:-1";
	public static final String HTTP_RETRIEVE_NON_CANONICAL_URL = "http://www.eclipse.org:80///ecf////ip_log.html";
	
	private static final String FTP_RETRIEVE = "https://ftp.osuosl.org/pub/eclipse/rt/ecf/3.14.0/org.eclipse.ecf.sdk_3.14.0.v20180518-0149.zip";
	
	// See bug 237936
	private static final String BUG_237936_URL = "https://www.eclipse.org/downloads/download.php?file=/webtools/updates/site.xml&format=xml&countryCode=us&timeZone=-5&responseType=xml";

	File tmpFile = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		tmpFile = Files.createTempFile("ECFTest", "").toFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (tmpFile != null)
			tmpFile.delete();
		tmpFile = null;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractRetrieveTestCase#handleStartConnectEvent(org.eclipse.ecf.filetransfer.events.IFileTransferConnectStartEvent)
	 */
	protected void handleStartConnectEvent(IFileTransferConnectStartEvent event) {
		super.handleStartConnectEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractRetrieveTestCase#handleStartEvent(org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent)
	 */
	protected void handleStartEvent(IIncomingFileTransferReceiveStartEvent event) {
		super.handleStartEvent(event);
		assertNotNull(event.getFileID());
		assertNotNull(event.getFileID().getFilename());
		Map responseHeaders = event.getResponseHeaders();
		assertNotNull(responseHeaders);
		trace("responseHeaders="+responseHeaders);
		try {
			incomingFileTransfer = event.receive(tmpFile);
		} catch (final IOException e) {
			fail(e.getLocalizedMessage());
		}
	}

	
	protected void testReceive(String url) throws Exception {
		assertNotNull(retrieveAdapter);
		final IFileTransferListener listener = createFileTransferListener();
		final IFileID fileID = createFileID(new URL(url));
		retrieveAdapter.sendRetrieveRequest(fileID, listener, null);

		waitForDone(360000);

		assertHasEvent(startEvents, IIncomingFileTransferReceiveStartEvent.class);
		assertHasMoreThanEventCount(dataEvents, IIncomingFileTransferReceiveDataEvent.class, 0);
		assertDoneOK();

		assertTrue(tmpFile.exists());
		assertTrue(tmpFile.length() > 0);
	}

	protected void testReceiveFails(String url) throws Exception {
		assertNotNull(retrieveAdapter);
		final IFileTransferListener listener = createFileTransferListener();
		try {
			final IFileID fileID = createFileID(new URL(url));
			retrieveAdapter.sendRetrieveRequest(fileID, listener, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		waitForDone(10000);

		assertHasNoEvent(startEvents, IIncomingFileTransferReceiveStartEvent.class);
		assertHasNoEvent(dataEvents, IIncomingFileTransferReceiveDataEvent.class);
		assertHasDoneEvent();
	}

	public void testReceiveFile() throws Exception {
		//addProxy("composent.com",3129,"foo\\bar","password");
		testReceive(HTTP_RETRIEVE);
	}

	public void testReceiveFile1() throws Exception {
		//addProxy("composent.com",3129,"foo\\bar","password");
		testReceive(HTTP_RETRIEVE1);
	}

	public void testReceiveHostOnly() throws Exception {
		//addProxy("composent.com",3129,"foo\\bar","password");
		testReceive(HTTP_RETRIEVE_HOST_ONLY);
	}
	
	public void testReceiveFilePort() throws Exception {
		testReceive(HTTP_RETRIEVE_PORT);
	}
	
	// Invalid test as double-slash is not a legal in the path section of a URI (see RFC2396, sections 3.2, 3.4). 
	/*
	public void testReceiveNonCanonicalURLPath() throws Exception {
		//addProxy("composent.com",3129,"foo\\bar","password");
		testReceive(HTTP_RETRIEVE_NON_CANONICAL_URL);
	}
	*/

	// Invalid test as double-slash is not a legal in the path section of a URI (see RFC2396, sections 3.2, 3.4). 
	/*
 	void testReceiveNonCanonicalURLPathLocalHost() throws Exception {
		String url = server.getServerURL() + "//foo";
		assertTrue(url, url.matches("\\Ahttp://localhost:[0-9]+//foo\\Z"));
		testReceive(url);
	}
	*/
	
	public void testFTPReceiveFile() throws Exception {
		testReceive(FTP_RETRIEVE);
	}
	
	public void testHttpsReceiveFile() throws Exception {
		testReceive(HTTPS_RETRIEVE);
	}

	public void testFailedReceive() throws Exception {
		try {
			testReceiveFails(HTTP_404_FAIL_RETRIEVE);
			assertDoneExceptionAfterServerResponse(HttpURLConnection.HTTP_NOT_FOUND);
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	
	public void testRetrieveBadURL() throws Exception {
		try {
			testReceiveFails(HTTP_BAD_URL);
			assertDoneExceptionBeforeServerResponse(ConnectException.class);
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	public void testReceiveGzip() throws Exception {
		testReceive(BUG_237936_URL);
	}

	public static final String HTTP_RETRIEVE_GZFILE = "https://download.eclipse.org/releases/2018-09/201809191002/content.xml.xz";
	public static final String HTTP_RETRIEVE_GZFILE_MIRROR = "http://mirrors.xmission.com/eclipse/eclipse/updates/3.4//plugins/javax.servlet.jsp_2.0.0.v200806031607.jar.pack.gz";

	public void testReceiveGzipWithGZFile() throws Exception {
		tmpFile = Files.createTempFile("foo", "something.pack.gz").toFile();
		testReceive(HTTP_RETRIEVE_GZFILE);
		if (tmpFile != null) {
			System.out.println(tmpFile.length());
			//assertTrue("4.0", tmpFile.length() < 50000);
		}
	}
	
	public void testReceiveGzipWithGZFileFromMirror() throws Exception {
		tmpFile = Files.createTempFile("foo", "something.pack.gz").toFile();
		testReceive(HTTP_RETRIEVE_GZFILE_MIRROR);
		if (tmpFile != null) {
			System.out.println(tmpFile.length());
			assertTrue("4.0", tmpFile.length() < 50000);
		}
	}
	
 }
