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
package org.eclipse.ecf.provider.xmpp.container;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.xmpp.XmppPlugin;
import org.eclipse.ecf.provider.xmpp.identity.XMPPID;
import org.eclipse.ecf.provider.xmpp.smack.ECFConnection;

public class XMPPSClientSOContainer extends XMPPClientSOContainer {
	public XMPPSClientSOContainer() throws Exception {
		super();
	}
	/**
	 * @param ka
	 * @throws Exception
	 */
	public XMPPSClientSOContainer(int ka) throws Exception {
		super(ka);
	}
	/**
	 * @param userhost
	 * @param ka
	 * @throws Exception
	 */
	public XMPPSClientSOContainer(String userhost, int ka) throws Exception {
		super(userhost, ka);
	}
	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(XmppPlugin.getDefault().getSecureNamespaceIdentifier());
	}
	protected ISynchAsynchConnection createConnection(ID remoteSpace,
			Object data) throws ConnectionCreateException {
		ISynchAsynchConnection conn = null;
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
		conn = new ECFConnection(google,getConnectNamespace(),receiver,secure);
		Object res = conn.getAdapter(IIMMessageSender.class);
		if (res != null) {
			// got it
			messageSender = (IIMMessageSender) res;
		}
		return conn;
	}

}
