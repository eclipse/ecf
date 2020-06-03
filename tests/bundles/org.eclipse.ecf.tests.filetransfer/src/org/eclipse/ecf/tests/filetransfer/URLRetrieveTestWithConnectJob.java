/****************************************************************************
 * Copyright (c) 2009 IBM, and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.filetransfer;

import org.eclipse.ecf.filetransfer.FileTransferJob;
import org.eclipse.ecf.filetransfer.events.IFileTransferConnectStartEvent;

public class URLRetrieveTestWithConnectJob extends URLRetrieveTest {

	protected void handleStartConnectEvent(IFileTransferConnectStartEvent event) {
		super.handleStartConnectEvent(event);
		assertNotNull(event.getFileID());
		assertNotNull(event.getFileID().getFilename());

		FileTransferJob connectJob = event.prepareConnectJob(null);
		connectJob.addJobChangeListener(new JobChangeTraceListener(startTime));
		event.connectUsingJob(connectJob);
	}
}
