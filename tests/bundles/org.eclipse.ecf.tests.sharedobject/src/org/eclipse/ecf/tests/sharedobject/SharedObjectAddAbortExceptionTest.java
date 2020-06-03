/****************************************************************************
 * Copyright (c) 2006 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Suen - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.sharedobject;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.ecf.core.sharedobject.SharedObjectAddAbortException;

public class SharedObjectAddAbortExceptionTest extends TestCase {

	public void testGetTimeout() {
		try {
			throw new SharedObjectAddAbortException(null, (Throwable) null, 10);
		} catch (SharedObjectAddAbortException e) {
			assertEquals(10, e.getTimeout());
		}

		try {
			// Regression test for bug #167019
			throw new SharedObjectAddAbortException(null, (Map) null, 10);
		} catch (SharedObjectAddAbortException e) {
			assertEquals(10, e.getTimeout());
		}

		try {
			// Regression test for bug #167019
			throw new SharedObjectAddAbortException(null, (List) null,
					(Map) null, 10);
		} catch (SharedObjectAddAbortException e) {
			assertEquals(10, e.getTimeout());
		}
	}

}
