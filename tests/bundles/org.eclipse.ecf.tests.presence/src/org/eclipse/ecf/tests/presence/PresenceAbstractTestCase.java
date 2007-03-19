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

package org.eclipse.ecf.tests.presence;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

/**
 *
 */
public abstract class PresenceAbstractTestCase extends ContainerAbstractTestCase {

	static final String XMPP_CONTAINER = "ecf.xmpp.smack";

	protected String getClientContainerName() {
		return XMPP_CONTAINER;
	}

	protected void setUp() throws Exception {
		setClientCount(2);
		clients = createClients();
	}

	protected String getSecondUsername() {
		return System.getProperty("secondusername");
	}

	protected String getSecondPassword() {
		return System.getProperty("secondpassword");
	}

	protected ID getServerConnectID(IContainer client) {
		Namespace connectNamespace = client.getConnectNamespace();
		String username = (client.getID().equals(getClients()[0].getID()))?getUsername():getSecondUsername();
		try {
			return IDFactory.getDefault().createID(connectNamespace,username);
		} catch (IDCreateException e) {
			fail("Could not create server connect ID");
			return null;
		}
	}

	protected IConnectContext getConnectContext(IContainer client) {
		String password = (client.getID().equals(getClients()[0].getID()))?getPassword():getSecondPassword();
		return createPasswordConnectContext(password);
	}

	protected IPresenceContainerAdapter getPresenceAdapter() {
		return (IPresenceContainerAdapter) getClients()[0].getAdapter(IPresenceContainerAdapter.class);
	}

}
