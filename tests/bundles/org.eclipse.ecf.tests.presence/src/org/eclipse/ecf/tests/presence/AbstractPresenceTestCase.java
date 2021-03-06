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

package org.eclipse.ecf.tests.presence;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

/**
 *
 */
public abstract class AbstractPresenceTestCase extends ContainerAbstractTestCase {

	protected abstract String getClientContainerName();

	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(2);
		clients = createClients();
	}

	protected ID getServerConnectID(int client) {
		final IContainer container = getClient(client);
		final Namespace connectNamespace = container.getConnectNamespace();
		final String username = getUsername(client);
		try {
			return IDFactory.getDefault().createID(connectNamespace, username);
		} catch (final IDCreateException e) {
			fail("Could not create server connect ID");
			return null;
		}
	}

	protected IPresenceContainerAdapter getPresenceAdapter(int client) {
		final IContainer c = getClient(client);
		if (c == null)
			return null;
		return (IPresenceContainerAdapter) c.getAdapter(IPresenceContainerAdapter.class);
	}
}
