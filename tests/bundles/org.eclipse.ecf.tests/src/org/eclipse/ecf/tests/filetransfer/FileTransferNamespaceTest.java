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

package org.eclipse.ecf.tests.filetransfer;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.tests.ECFAbstractTestCase;

public class FileTransferNamespaceTest extends ECFAbstractTestCase {

	private Namespace fixture;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fixture = IDFactory.getDefault().getNamespaceByName("ecf.provider.filetransfer");
		assertNotNull(fixture);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		fixture = null;
	}

	public void testNamespaceGetScheme() {
		String scheme = fixture.getScheme();
		assertNotNull(scheme);
	}
	
	public void testNamespaceGetName() {
		String name = fixture.getName();
		assertNotNull(name);
		assertTrue(name.equals("ecf.provider.filetransfer"));
	}
	
	public final void testSerializable() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(buf);
		try {
			out.writeObject(fixture);
		} catch (NotSerializableException ex) {
			fail(ex.getLocalizedMessage());
		} finally {
			out.close();
		}
	}
	
	public final void testCreateID() throws Exception {
		ID newID = IDFactory.getDefault().createID(fixture, "http://www.news.com");
		assertNotNull(newID);
	}
	
	public final void testGetSupportedSchemes() throws Exception {
		String [] supportedSchemes = fixture.getSupportedSchemes();
		System.out.println("supportedSchemes="+Arrays.asList(supportedSchemes));
		assertNotNull(supportedSchemes);
	}

}