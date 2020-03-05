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

package org.eclipse.ecf.tests.filetransfer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * A test suite that prints out all the available filetransfer providers before
 * executing the tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ FileBrowseTest.class, FileIDFactoryTest.class, FileSendTest.class, GetRemoteFileNameTest.class,
		NamespaceTest.class, URIProtocolFactoryRetrieveTest.class, URLBrowseTest.class, URLCancelTest.class,
		URLPartialRetrieveTest.class, URLRetrievePauseResumeTest.class, URLRetrieveTest.class })
public class URLConnectionTestSuite {

	private static void displayAllProviders() {
		Bundle[] allBundles = FrameworkUtil.getBundle(AbstractFileTransferTestCase.class).getBundleContext()
				.getBundles();

		Set<String> bundles = new HashSet<String>();

		IExtensionPoint[] extPoints = Platform.getExtensionRegistry()
				.getExtensionPoints("org.eclipse.ecf.provider.filetransfer");
		for (IExtensionPoint extPoint : extPoints) {
			for (IExtension ext : extPoint.getExtensions()) {
				for (Bundle bundle : allBundles) {
					if (ext.getContributor().getName().equals(bundle.getSymbolicName())) {
						bundles.add(bundle.getSymbolicName() + "/" + bundle.getVersion());
					}
				}
			}
		}

		System.out.println("Installed Filetransfer Provider Bundles:");
		for (String bundle : bundles) {
			System.out.println("  " + bundle);
		}
	}

	@BeforeClass
	public static void setUp() throws Exception {
		displayAllProviders();
	}
}
