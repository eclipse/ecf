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

public final class TestService1 implements TestServiceInterface1, TestServiceInterface2 {

	public String doStuff1() {
		return TestServiceInterface1.TEST_SERVICE_STRING1;
	}

	public String doStuff2() {
		return TestServiceInterface2.TEST_SERVICE_STRING2;
	}

}
