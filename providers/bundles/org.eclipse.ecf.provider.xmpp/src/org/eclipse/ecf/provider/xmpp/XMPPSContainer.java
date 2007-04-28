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
package org.eclipse.ecf.provider.xmpp;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.provider.xmpp.XmppPlugin;
import org.eclipse.ecf.internal.provider.xmpp.identity.XMPPID;
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
		boolean google = false;
		boolean secure = false;
		if (remoteSpace instanceof XMPPID) {
			XMPPID theID = (XMPPID) remoteSpace;
			String host = theID.getHostname();
			if (host.toLowerCase().equals(GOOGLE_SERVICENAME)) {
				google = true;
				secure = false;
			} else {
				google = false;
				secure = true;
			}
		}
		return new ECFConnection(google, getConnectNamespace(), receiver,
				secure);
	}

}
