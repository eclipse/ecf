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
package org.eclipse.ecf.provider.comm;

import org.eclipse.ecf.core.util.Event;

/**
 * Connection event super class.
 * 
 */
public class ConnectionEvent implements Event {
	private final Object data;

	private final IConnection connection;

	public ConnectionEvent(IConnection source, Object data) {
		this.connection = source;
		this.data = data;
	}

	public IConnection getConnection() {
		return connection;
	}

	public Object getData() {
		return data;
	}

	public String toString() {
		final StringBuffer buf = new StringBuffer("ConnectionEvent["); //$NON-NLS-1$
		buf.append("conn=").append(getConnection()).append(";").append("data=") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.append(getData());
		buf.append("]"); //$NON-NLS-1$
		return buf.toString();
	}

}