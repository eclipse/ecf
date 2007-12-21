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

/**
 *
 */
public class FileBrowseTestCase extends AbstractBrowseTestCase {

	public void testFileBrowse() throws Exception {
		testListDirectory(new URL("file:///c:/"));
	}
}
