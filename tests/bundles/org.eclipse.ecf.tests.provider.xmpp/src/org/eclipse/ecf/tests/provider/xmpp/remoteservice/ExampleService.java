/****************************************************************************
 * Copyright (c) 2010 Eugen Reiswich.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Eugen Reiswich - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.xmpp.remoteservice;

import java.io.Serializable;

import org.eclipse.ecf.provider.xmpp.identity.XMPPID;

public class ExampleService implements IExampleService, Serializable {

	private static final long serialVersionUID = -333552101728391955L;

	private final XMPPID xmppID;

	public ExampleService(XMPPID xmppID) {
		this.xmppID = xmppID;
	}

	public XMPPID getClientID() {
		return xmppID;
	}
}