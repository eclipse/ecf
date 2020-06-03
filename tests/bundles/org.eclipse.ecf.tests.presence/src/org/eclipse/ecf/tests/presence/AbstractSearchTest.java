/****************************************************************************
 * Copyright (c) 2008 Marcelo Mayworm.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: 	Marcelo Mayworm - initial API and implementation
 * 
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.presence;


public abstract class AbstractSearchTest extends AbstractPresenceTestCase {

	public static final int CLIENT_COUNT = 1;
	public static final int WAITTIME = 3000;

	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(CLIENT_COUNT);
		clients = createClients();
	}

}
