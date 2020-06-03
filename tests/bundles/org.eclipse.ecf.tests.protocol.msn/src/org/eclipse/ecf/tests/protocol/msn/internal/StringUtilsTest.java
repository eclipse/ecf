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
package org.eclipse.ecf.tests.protocol.msn.internal;

import junit.framework.TestCase;

import org.eclipse.ecf.protocol.msn.internal.encode.StringUtils;

public class StringUtilsTest extends TestCase {

	public void testSplitOnSpace() {
		String[] ret = StringUtils.splitOnSpace(""); //$NON-NLS-1$
		assertNotNull(ret);
		assertEquals(1, ret.length);
		assertEquals("", ret[0]); //$NON-NLS-1$

		ret = StringUtils.splitOnSpace("VER 1 MSNP11 CVR0"); //$NON-NLS-1$
		assertNotNull(ret);
		assertEquals(4, ret.length);
		assertEquals("VER", ret[0]); //$NON-NLS-1$
		assertEquals("1", ret[1]); //$NON-NLS-1$
		assertEquals("MSNP11", ret[2]); //$NON-NLS-1$
		assertEquals("CVR0", ret[3]); //$NON-NLS-1$
	}

	public void testSplitChar() {
		String[] ret = StringUtils.split("", ' '); //$NON-NLS-1$
		assertNotNull(ret);
		assertEquals(1, ret.length);
		assertEquals("", ret[0]); //$NON-NLS-1$

		ret = StringUtils.split("VER 1 MSNP11 CVR0", ' '); //$NON-NLS-1$
		assertNotNull(ret);
		assertEquals(4, ret.length);
		assertEquals("VER", ret[0]); //$NON-NLS-1$
		assertEquals("1", ret[1]); //$NON-NLS-1$
		assertEquals("MSNP11", ret[2]); //$NON-NLS-1$
		assertEquals("CVR0", ret[3]); //$NON-NLS-1$
	}

	public void testSplitSubstring() {
		String ret = StringUtils.splitSubstring("", " ", 1); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("", ret); //$NON-NLS-1$

		ret = StringUtils.splitSubstring("VER 1 MSNP11 CVR0", " ", 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("VER", ret); //$NON-NLS-1$
		ret = StringUtils.splitSubstring("VER 1 MSNP11 CVR0", " ", 1); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("1", ret); //$NON-NLS-1$
		ret = StringUtils.splitSubstring("VER 1 MSNP11 CVR0", " ", 2); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("MSNP11", ret); //$NON-NLS-1$
		ret = StringUtils.splitSubstring("VER 1 MSNP11 CVR0", " ", 3); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("CVR0", ret); //$NON-NLS-1$
	}

	public void testXmlDecode() {
		assertEquals("", StringUtils.xmlDecode("")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("&&&", StringUtils.xmlDecode("&amp;&amp;&amp;")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("<>", StringUtils.xmlDecode("&lt;&gt;")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("<><><>", StringUtils //$NON-NLS-1$
				.xmlDecode("&lt;&gt;&lt;&gt;&lt;&gt;")); //$NON-NLS-1$
		assertEquals("'\"'\"'", StringUtils //$NON-NLS-1$
				.xmlDecode("&apos;&quot;&apos;&quot;&apos;")); //$NON-NLS-1$
		assertEquals("I like <xml> tags", StringUtils //$NON-NLS-1$
				.xmlDecode("I like &lt;xml&gt; tags")); //$NON-NLS-1$
	}

	public void testXmlEncode() {
		assertEquals("", StringUtils.xmlEncode("")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("&amp;&amp;&amp;", StringUtils.xmlEncode("&&&")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("&lt;&gt;", StringUtils.xmlEncode("<>")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("&lt;&gt;&lt;&gt;&lt;&gt;", StringUtils //$NON-NLS-1$
				.xmlEncode("<><><>")); //$NON-NLS-1$
		assertEquals("&apos;&quot;&apos;&quot;&apos;", StringUtils //$NON-NLS-1$
				.xmlEncode("'\"'\"'")); //$NON-NLS-1$
		assertEquals("I like &lt;xml&gt; tags", StringUtils //$NON-NLS-1$
				.xmlEncode("I like <xml> tags")); //$NON-NLS-1$
	}
}
