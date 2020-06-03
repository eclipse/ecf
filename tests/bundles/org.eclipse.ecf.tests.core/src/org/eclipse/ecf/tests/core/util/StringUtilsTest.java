/****************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster.de <dot> de) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.tests.core.util;

import org.eclipse.ecf.core.util.StringUtils;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.core.util.StringUtils#replaceFirst(java.lang.String, java.lang.String, java.lang.String)}
	 * .
	 */
	public final void testReplaceFirstNoCase() {
		String input = "barbar";
		String toReplace = "bar";
		String withReplace = "foo";
		assertEquals("foobar",
				StringUtils.replaceFirst(input, toReplace, withReplace));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.core.util.StringUtils#replaceFirst(java.lang.String, java.lang.String, java.lang.String)}
	 * .
	 */
	public final void testReplaceFirstWithCase() {
		String input = "Barbarbar";
		String toReplace = "bar";
		String withReplace = "foo";
		assertEquals("Barfoobar",
				StringUtils.replaceFirst(input, toReplace, withReplace));
	}
}
