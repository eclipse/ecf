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

import org.eclipse.ecf.protocol.msn.internal.encode.Encryption;

public class EncryptionTest extends TestCase {

	public void testSHA() {
		String object = "Creatorbuddy1@hotmail.comSize24539Type3Location" //$NON-NLS-1$
				+ "TFR2C.tmpFriendlyAAA=SHA1DtrC8SlFx2sWQxZMIBAWSEnXc8oQ="; //$NON-NLS-1$
		assertEquals(Encryption.computeSHA(object.getBytes()),
				"U32o6bosZzluJq82eAtMpx5dIEI="); //$NON-NLS-1$
	}
}
