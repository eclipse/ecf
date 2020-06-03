/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.httpservice;

import org.eclipse.ecf.internal.tests.httpservice.Activator;
import org.eclipse.ecf.tests.ECFAbstractTestCase;
import org.eclipse.ecf.tests.util.BundleUtil;
import org.osgi.framework.BundleException;

public abstract class AbstractHttpServiceTest extends ECFAbstractTestCase {

	public static final String JETTY_HTTP_SERVICE_BUNDLE = "org.eclipse.equinox.http.jetty";
	
	protected BundleUtil bundleUtil;
	protected String httpServiceBundleName = JETTY_HTTP_SERVICE_BUNDLE;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		bundleUtil = new BundleUtil(Activator.getDefault().getContext());
	}
	
	@Override
	protected void tearDown() throws Exception {
		bundleUtil.close();
		bundleUtil = null;
		super.tearDown();
	}
	
	protected void startHttpService() throws BundleException {
		bundleUtil.startBundle(httpServiceBundleName);
	}
	
	protected void stopHttpService() throws BundleException {
		bundleUtil.stopBundle(httpServiceBundleName);
	}
}
