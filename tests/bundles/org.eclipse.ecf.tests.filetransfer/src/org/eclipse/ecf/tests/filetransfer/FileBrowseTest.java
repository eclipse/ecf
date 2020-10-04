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

package org.eclipse.ecf.tests.filetransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FileBrowseTest extends AbstractBrowseTestCase {

	protected File[] roots;

	protected File[] files;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractBrowseTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		roots = File.listRoots();
		final List files = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			final File[] fs = roots[i].listFiles();
			if (fs != null)
				for (int j = 0; j < fs.length; j++) {
					if (fs[j].exists())
						files.add(fs[j]);
				}
		}
		this.files = (File[]) files.toArray(new File[] {});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.filetransfer.AbstractBrowseTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		this.roots = null;
		this.files = null;
	}

	public void testBrowseRoots() throws Exception {
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].exists())
				testBrowse(roots[i].toURI().toURL());
			Thread.sleep(100);
		}
	}

	public void testFileBrowse() throws Exception {
		for (int i = 0; i < files.length; i++) {
			testBrowse(files[i].toURL());
			Thread.sleep(100);
		}
	}

}
