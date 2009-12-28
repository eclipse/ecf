/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.core;

import junit.framework.TestCase;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.internal.tests.Activator;

public class ContainerExtensionTest extends TestCase {

	private static final String TEST_CLIENTID = "foo1";
	private static final String TEST_SERVERID = "ecftcp://localhost:32111/server";
	
	IContainerManager containerManager;
	
	protected void setUp() throws Exception {
		super.setUp();
		containerManager = Activator.getDefault().getContainerManager();
	}
	
	public void testContainerExtension() {
		IContainer[] containers = containerManager.getAllContainers();
		assertTrue(containers != null);
		assertNotNull(containerManager.getContainer(Activator.getDefault().getIDFactory().createStringID(TEST_CLIENTID)));
		assertNotNull(containerManager.getContainer(Activator.getDefault().getIDFactory().createStringID(TEST_SERVERID)));
	}
}
