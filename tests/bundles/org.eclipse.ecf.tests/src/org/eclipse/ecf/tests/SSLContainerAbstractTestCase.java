/****************************************************************************
 * Copyright (c) 2012 Composent, Inc. and others.
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

package org.eclipse.ecf.tests;

public abstract class SSLContainerAbstractTestCase extends ContainerAbstractTestCase {

	protected void setUp() throws Exception {
		genericServerName = "ecf.generic.ssl.server";
		genericClientName = "ecf.generic.ssl.client";
		genericServerPort = 40000;
		genericServerIdentity = "ecfssl://localhost:{0}/secureserver";
		super.setUp();
	}
	
}
