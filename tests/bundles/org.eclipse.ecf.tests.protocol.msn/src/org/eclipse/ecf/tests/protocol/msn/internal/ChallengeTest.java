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

import org.eclipse.ecf.protocol.msn.internal.encode.Challenge;

public class ChallengeTest extends TestCase {

	public void testChallenge() {
		assertEquals("85ecb0db8f32113df79ce0892b9a102c", Challenge //$NON-NLS-1$
				.createQuery("22210219642164014968")); //$NON-NLS-1$
		assertEquals("e7ad3cb09d3e9e4e7c720175984809e9", Challenge //$NON-NLS-1$
				.createQuery("36819795137093047918")); //$NON-NLS-1$
		assertEquals("59bcf63ed21f44906c3d3e121ddbed65", Challenge //$NON-NLS-1$
				.createQuery("21948129323261853323")); //$NON-NLS-1$
		assertEquals("dcb8ff529e4dd12cc43389851128d2db", Challenge //$NON-NLS-1$
				.createQuery("41525959199453244913")); //$NON-NLS-1$
		assertEquals("d15553d0ea89c9f63bbb98a208fa4235", Challenge //$NON-NLS-1$
				.createQuery("31744216663023315951")); //$NON-NLS-1$
		assertEquals("4e11d4cd56a65bdf04f60aa133db7ebc", Challenge //$NON-NLS-1$
				.createQuery("14494180082586329971")); //$NON-NLS-1$
	}

}
