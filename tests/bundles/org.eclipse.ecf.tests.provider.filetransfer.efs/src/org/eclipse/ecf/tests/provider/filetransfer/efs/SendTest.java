/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
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

package org.eclipse.ecf.tests.provider.filetransfer.efs;

import java.io.File;
import java.net.URL;

import org.eclipse.ecf.tests.filetransfer.AbstractSendTestCase;

public class SendTest extends AbstractSendTestCase {

	File inputFile = null;
	URL outputFile = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractSendTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		inputFile = new File("test.txt");
		outputFile = File.createTempFile("ECFTest", "").toURL();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractSendTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		inputFile = null;
		outputFile = null;
	}

	public void testSend() throws Exception {
		testSendForFile(outputFile, inputFile);
		waitForDone(5000);
	}
}
