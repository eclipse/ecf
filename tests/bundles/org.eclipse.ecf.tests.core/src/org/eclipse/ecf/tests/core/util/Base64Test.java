/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.tests.core.util;

import org.eclipse.ecf.core.util.Base64;

import junit.framework.TestCase;

public class Base64Test extends TestCase {

	private static final String INPUT = "the quick brown fox jumped over the lazy dog";

	protected String encode() {
		return Base64.encode(INPUT.getBytes());
	}

	public void testEncode() {
		String encoded = encode();
		assertNotNull(encoded);
		assertTrue(encoded.length() > 0);
	}

	public void testDecode() {
		String encoded = encode();
		byte[] bytes = Base64.decode(encoded);
		assertNotNull(bytes);
		assertTrue(INPUT.equals(new String(bytes)));
	}
}
