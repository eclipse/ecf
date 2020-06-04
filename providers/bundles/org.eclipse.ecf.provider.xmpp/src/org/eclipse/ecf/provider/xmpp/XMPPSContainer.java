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
package org.eclipse.ecf.provider.xmpp;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.CallbackHandler;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.provider.xmpp.XmppPlugin;
import org.eclipse.ecf.internal.provider.xmpp.smack.ECFConnection;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;

public class XMPPSContainer extends XMPPContainer {
	public XMPPSContainer() throws Exception {
		super();
	}

	/**
	 * @param ka
	 * @throws Exception
	 */
	public XMPPSContainer(int ka) throws Exception {
		super(ka);
	}

	/**
	 * @param userhost
	 * @param ka
	 * @throws Exception
	 */
	public XMPPSContainer(String userhost, int ka) throws Exception {
		super(userhost, ka);
	}

	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(
				XmppPlugin.getDefault().getSecureNamespaceIdentifier());
	}

	protected ISynchAsynchConnection createConnection(ID remoteSpace,
			Object data) throws ConnectionCreateException {
		boolean google = isGoogle(remoteSpace);
		CallbackHandler ch = data instanceof IConnectContext ? ((IConnectContext) data)
				.getCallbackHandler() : null;
		return new ECFConnection(google, getConnectNamespace(), receiver, ch);
	}

}
