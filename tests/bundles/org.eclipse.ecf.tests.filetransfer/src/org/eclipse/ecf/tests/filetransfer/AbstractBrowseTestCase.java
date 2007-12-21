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

package org.eclipse.ecf.tests.filetransfer;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.util.TimeoutException;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemBrowserContainerAdapter;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemRequest;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemDirectoryEvent;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemEvent;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;

/**
 *
 */
public abstract class AbstractBrowseTestCase extends TestCase {

	protected IRemoteFileSystemBrowserContainerAdapter adapter = null;

	protected Object lock = new Object();

	protected boolean done = false;

	protected IRemoteFileSystemRequest request = null;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		final IContainer container = ContainerFactory.getDefault().createContainer();
		adapter = (IRemoteFileSystemBrowserContainerAdapter) container.getAdapter(IRemoteFileSystemBrowserContainerAdapter.class);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		adapter = null;
		if (request != null) {
			request.cancel();
			request = null;
		}
	}

	protected IRemoteFileSystemListener createRemoteFileSystemListener() {
		return new IRemoteFileSystemListener() {
			public void handleRemoteFileEvent(IRemoteFileSystemEvent event) {
				if (event instanceof IRemoteFileSystemDirectoryEvent) {
					handleDirectoryEvent((IRemoteFileSystemDirectoryEvent) event);
				} else
					handleUnknownEvent(event);
			}

		};
	}

	protected IFileID createDirectoryFileID(URL directory) throws Exception {
		return FileIDFactory.getDefault().createFileID(adapter.getDirectoryNamespace(), directory);
	}

	protected void testListDirectory(URL directory) throws Exception {
		Assert.isNotNull(adapter);
		request = adapter.sendDirectoryRequest(createDirectoryFileID(directory), createRemoteFileSystemListener());
	}

	/**
		 * @param event
		 */
	protected void handleUnknownEvent(IRemoteFileSystemEvent event) {
		System.out.println("handleUnknownEvent(" + event + ")");
	}

	/**
	 * @param event
	 */
	protected void handleDirectoryEvent(IRemoteFileSystemDirectoryEvent event) {
		System.out.println("handleDirectoryEvent(" + event + ")");
	}

	protected void waitForDone(int timeout) throws Exception {
		final long start = System.currentTimeMillis();
		synchronized (lock) {
			while (!done && ((System.currentTimeMillis() - start) < timeout)) {
				lock.wait(timeout / 20);
			}
			if (!done)
				throw new TimeoutException(timeout);
		}
	}

	protected void assertHasEvent(Collection collection, Class eventType) {
		assertHasEventCount(collection, eventType, 1);
	}

	protected void assertHasEventCount(Collection collection, Class eventType, int eventCount) {
		int count = 0;
		for (final Iterator i = collection.iterator(); i.hasNext();) {
			final Object o = i.next();
			if (eventType.isInstance(o))
				count++;
		}
		assertTrue(count == eventCount);
	}

	protected void assertHasMoreThanEventCount(Collection collection, Class eventType, int eventCount) {
		int count = 0;
		for (final Iterator i = collection.iterator(); i.hasNext();) {
			final Object o = i.next();
			if (eventType.isInstance(o))
				count++;
		}
		assertTrue(count > eventCount);
	}

}
