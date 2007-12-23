/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.provider.filetransfer.efs;

import java.net.URL;

import org.eclipse.ecf.filetransfer.IRemoteFile;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemDirectoryEvent;
import org.eclipse.ecf.tests.filetransfer.AbstractBrowseTestCase;

/**
 *
 */
public class BrowseTest extends AbstractBrowseTestCase {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractBrowseTestCase#handleDirectoryEvent(org.eclipse.ecf.filetransfer.events.IRemoteFileSystemDirectoryEvent)
	 */
	protected void handleDirectoryEvent(IRemoteFileSystemDirectoryEvent event) {
		super.handleDirectoryEvent(event);
		assertNotNull(event);
		final IRemoteFile[] remoteFiles = event.getRemoteFiles();
		assertNotNull(remoteFiles);
		assertTrue(remoteFiles.length > 0);
		verifyRemoteFiles(remoteFiles);
		done = true;
	}

	public void testBrowseRoot() throws Exception {
		super.testListDirectory(new URL("efs:file:///c:/"));
		waitForDone(5000);
	}

}
