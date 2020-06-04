/****************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.irc.datashare;

import java.net.SocketAddress;

/**
 * An interface for interacting with the actual datashare container. This allows
 * the IRC provider to work even if the optional dependencies are not present.
 */
public interface IIRCDatashareContainer {

	public void enqueue(SocketAddress address);

	public void setIP(String ip);

}
