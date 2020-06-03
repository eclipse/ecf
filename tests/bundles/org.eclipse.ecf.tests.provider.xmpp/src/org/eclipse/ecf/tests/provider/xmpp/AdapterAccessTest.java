/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
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

package org.eclipse.ecf.tests.provider.xmpp;

import org.eclipse.ecf.tests.presence.AbstractAdapterAccessTest;

/**
 *
 */
public class AdapterAccessTest extends AbstractAdapterAccessTest {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.presence.AbstractAdapterAccessTest#getClientContainerName()
	 */
	protected String getClientContainerName() {
		return XMPP.CONTAINER_NAME;
	}

}
