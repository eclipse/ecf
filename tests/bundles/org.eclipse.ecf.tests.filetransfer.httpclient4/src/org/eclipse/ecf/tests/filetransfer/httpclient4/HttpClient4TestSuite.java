/****************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Red Hat, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.tests.filetransfer.httpclient4;

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
 * providers and is run against the httpclient4 provider.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ FileBrowseTest.class, FileIDFactoryTest.class, FileSendTest.class, GetRemoteFileNameTest.class,
		NamespaceTest.class, URIProtocolFactoryRetrieveTest.class, URLBrowseTest.class, URLCancelTest.class,
		URLPartialRetrieveTest.class, URLRetrievePauseResumeTest.class, URLRetrieveTest.class,
		HttpClientGetPortFromURLTest.class })
public class HttpClient4TestSuite extends URLConnectionTestSuite {

}
