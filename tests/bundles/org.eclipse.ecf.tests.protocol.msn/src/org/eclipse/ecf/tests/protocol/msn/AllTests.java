/****************************************************************************
 * Copyright (c) 2005, 2007 Remy Suen
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.protocol.msn;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ecf.tests.protocol.msn.internal.ChallengeTest;
import org.eclipse.ecf.tests.protocol.msn.internal.EncryptionTest;
import org.eclipse.ecf.tests.protocol.msn.internal.StringUtilsTest;

public class AllTests extends TestCase {

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ChallengeTest.class);
		suite.addTestSuite(EncryptionTest.class);
		suite.addTestSuite(StringUtilsTest.class);
		return suite;
	}
}