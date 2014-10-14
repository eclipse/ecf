/*******************************************************************************
 * Copyright (c) 2014 CohesionForce Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CohesionForce Inc - initial API and implementation
 *******************************************************************************/

package org.eclipse.ecf.tests.provider.filetransfer.scp;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemBrowseEvent;
import org.eclipse.ecf.tests.filetransfer.AbstractBrowseTestCase;

public class ScpFileBrowseTest extends AbstractBrowseTestCase {

	protected File[] roots;

	protected File[] files;

	// Using a countdown latch to wait until we get the proper number
	//  of browse results
	CountDownLatch latch = new CountDownLatch(1);

	String username;

	protected void setUp() throws Exception {
		super.setUp();
		username = System.getProperty("user.name"); //$NON-NLS-1$
		IConnectContext cctx = ConnectContextFactory.createUsernamePasswordConnectContext(username, null);

		this.adapter.setConnectContextForAuthentication(cctx);

		roots = File.listRoots();
		List fList = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			final File[] fs = roots[i].listFiles();
			if (fs != null)
				for (int j = 0; j < fs.length; j++) {
					if (fs[j].exists())
						fList.add(fs[j]);
				}
		}
		this.files = (File[]) fList.toArray(new File[] {});
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		this.roots = null;
		this.files = null;
	}

	public void testBrowseRoots() throws Exception {
		latch = new CountDownLatch(roots.length);
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].exists()) {
				URL url = new URL("scp://" + username + "@localhost:" + roots[i].getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.println("Browsing: " + url); //$NON-NLS-1$
				testBrowse(url);
			} else {
				System.out.println("Skipping: " + roots[i].toString()); //$NON-NLS-1$
				latch.countDown();
			}
			// Need to sleep to give the connection time to close out
			Thread.sleep(100);
		}
		assertTrue(latch.await(60, TimeUnit.SECONDS));
	}

	protected void handleFileSystemBrowseEvent(IRemoteFileSystemBrowseEvent event) {
		trace("handleFileSystemBrowseEvent(" + event + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		if (event.getException() != null) {
			trace(event.getException().toString());
		}
		latch.countDown();
	}

	public void testFileBrowse() throws Exception {
		latch = new CountDownLatch(files.length);
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory() && files[i].exists()) {
				URL url = new URL("scp://" + username + "@localhost:" + files[i].getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.println("Browsing: " + url); //$NON-NLS-1$
				testBrowse(url);
			} else {
				System.out.println("Skipping: " + files[i].toString()); //$NON-NLS-1$
				latch.countDown();
			}
			// Need to sleep to give the connection time to close out
			Thread.sleep(100);
		}
		assertTrue(latch.await(60, TimeUnit.SECONDS));
	}

}
