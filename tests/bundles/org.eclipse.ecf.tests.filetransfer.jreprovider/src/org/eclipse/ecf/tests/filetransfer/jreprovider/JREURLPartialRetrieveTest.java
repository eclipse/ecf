/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.filetransfer.jreprovider;

import org.eclipse.ecf.tests.filetransfer.URLPartialRetrieveTest;

public class JREURLPartialRetrieveTest extends URLPartialRetrieveTest {

	protected void setUp() throws Exception {
		super.setUp();
		HttpFactoryRemover.removeBrowseProvider();
		HttpFactoryRemover.removeRetrieveProvider();
		HttpFactoryRemover.removeSendProvider();
	}

}
