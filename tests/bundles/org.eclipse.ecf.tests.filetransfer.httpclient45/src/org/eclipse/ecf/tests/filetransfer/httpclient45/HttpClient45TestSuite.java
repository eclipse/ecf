/****************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.filetransfer.httpclient45;

import org.eclipse.ecf.tests.filetransfer.FileBrowseTest;
import org.eclipse.ecf.tests.filetransfer.FileIDFactoryTest;
import org.eclipse.ecf.tests.filetransfer.FileSendTest;
import org.eclipse.ecf.tests.filetransfer.GetRemoteFileNameTest;
import org.eclipse.ecf.tests.filetransfer.NamespaceTest;
import org.eclipse.ecf.tests.filetransfer.URIProtocolFactoryRetrieveTest;
import org.eclipse.ecf.tests.filetransfer.URLBrowseTest;
import org.eclipse.ecf.tests.filetransfer.URLCancelTest;
import org.eclipse.ecf.tests.filetransfer.URLConnectionTestSuite;
import org.eclipse.ecf.tests.filetransfer.URLPartialRetrieveTest;
import org.eclipse.ecf.tests.filetransfer.URLRetrievePauseResumeTest;
import org.eclipse.ecf.tests.filetransfer.URLRetrieveTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A test suite that includes tests specific to httpclient filetransfer
 * providers and is run against the httpclient45 provider.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ FileBrowseTest.class, FileIDFactoryTest.class, FileSendTest.class, GetRemoteFileNameTest.class,
		NamespaceTest.class, URIProtocolFactoryRetrieveTest.class, URLBrowseTest.class, URLCancelTest.class,
		URLPartialRetrieveTest.class, URLRetrievePauseResumeTest.class, URLRetrieveTest.class,
		HttpClientGetPortFromURLTest.class })
public class HttpClient45TestSuite extends URLConnectionTestSuite {

}
