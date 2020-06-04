/****************************************************************************
 * Copyright (c) 2013 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server.generic;

import org.eclipse.ecf.core.identity.ID;

/**
 * @since 6.0
 */
public class SSLSimpleGenericServer extends SSLAbstractGenericServer {

	public SSLSimpleGenericServer(String host, int port) {
		super(host, port);
	}

	protected void handleDisconnect(ID targetId) {
		// nothing
	}

	protected void handleEject(ID targetId) {
		// nothing
	}

}
