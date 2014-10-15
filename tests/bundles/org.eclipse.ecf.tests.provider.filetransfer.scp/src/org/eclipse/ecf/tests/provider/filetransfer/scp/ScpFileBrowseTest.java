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

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemBrowseEvent;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemEvent;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;

public class ScpFileBrowseTest extends AbstractSCPTest {

	private String browseDir = System.getProperty("browseDirectory", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	private IRemoteFileSystemBrowserContainerAdapter adapter = null;

	Throwable exception;
	IRemoteFileSystemBrowseEvent browseEvent;

	protected void setUp() throws Exception {
		super.setUp();
		this.adapter = (IRemoteFileSystemBrowserContainerAdapter) ContainerFactory.getDefault().createContainer().getAdapter(IRemoteFileSystemBrowserContainerAdapter.class);
	}

	protected void tearDown() throws Exception {
		this.adapter = null;
		super.tearDown();
	}

	public void testFileBrowse() throws Exception {
		assertNotNull(adapter);
		IRemoteFileSystemListener listener = new IRemoteFileSystemListener() {
			public void handleRemoteFileEvent(IRemoteFileSystemEvent event) {
				System.out.println("localhost.handleRemoteFileEvent=" + event); //$NON-NLS-1$
				if (event instanceof IRemoteFileSystemBrowseEvent) {
					exception = event.getException();
					if (exception == null) {
						browseEvent = (IRemoteFileSystemBrowseEvent) event;
						syncNotify();
					}
				}
			}
		};
		String targetURL = "scp://" + host + browseDir; //$NON-NLS-1$
		System.out.println("Browsing targetURL=" + targetURL + " with username=" + username); //$NON-NLS-1$ //$NON-NLS-2$
		adapter.setConnectContextForAuthentication(ConnectContextFactory.createUsernamePasswordConnectContext(username, password));
		adapter.sendBrowseRequest(FileIDFactory.getDefault().createFileID(adapter.getBrowseNamespace(), targetURL), listener);

		syncWaitForNotify(60000);

		assertNotNull(browseEvent);

		IRemoteFile[] remoteFiles = browseEvent.getRemoteFiles();
		assertNotNull(remoteFiles);
		assertTrue(remoteFiles.length > 1);

		for (int i = 0; i < remoteFiles.length; i++) {
			IRemoteFileInfo fInfo = remoteFiles[i].getInfo();
			System.out.println("directory entry=" + i + ";id=" + remoteFiles[i].getID() + ";name=" + fInfo.getName() + ";isDirectory=" + fInfo.isDirectory() + ";size=" + fInfo.getLength() + ";lastModified=" + fInfo.getLastModified()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
	}

	public void syncNotify() {
		super.syncNotify();
	}

}
