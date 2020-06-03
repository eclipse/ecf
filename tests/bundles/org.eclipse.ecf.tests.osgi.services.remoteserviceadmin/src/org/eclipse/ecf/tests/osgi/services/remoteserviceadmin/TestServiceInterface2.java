/****************************************************************************
 * Copyright (c) 2009 Jan S. Rellermeyer and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Jan S. Rellermeyer - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.osgi.services.remoteserviceadmin;

public interface TestServiceInterface2 extends TestServiceInterface1 {

	public static final String TEST_SERVICE_STRING2 = "TestService2";
	

	String doStuff2();
	
}
