/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.core;

import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.internal.tests.Activator;

import junit.framework.TestCase;

/**
 *
 */
public class ECFStartTest extends TestCase {

	IContainerManager containerManager = null;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		containerManager = Activator.getDefault().getContainerManager();
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		containerManager = null;
	}
	
	public void testStarted() throws Exception {
		assertNotNull(containerManager);
		assertTrue(ECFStartup.isSet);
	}
}
