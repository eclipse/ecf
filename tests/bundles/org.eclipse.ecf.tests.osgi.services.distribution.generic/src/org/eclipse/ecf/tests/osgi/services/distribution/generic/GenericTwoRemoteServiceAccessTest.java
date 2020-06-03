/****************************************************************************
 * Copyright (c) 2011 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution.generic;

import org.eclipse.ecf.tests.osgi.services.distribution.AbstractTwoRemoteServiceAccessTest;


public class GenericTwoRemoteServiceAccessTest extends AbstractTwoRemoteServiceAccessTest {

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(1);
		createServerAndClients();
		connectClients();
		setupRemoteServiceAdapters();
	}

	
	protected void tearDown() throws Exception {
		cleanUpServerAndClients();
		super.tearDown();
	}

	protected String getClientContainerName() {
		return "ecf.generic.client";
	}

}
