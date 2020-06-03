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

package org.eclipse.ecf.provider.comm.tcp;

import java.io.Serializable;

public class ConnectResultMessage implements Serializable {
	private static final long serialVersionUID = 3833188038300938804L;
	Serializable data;

	public ConnectResultMessage(Serializable data) {
		this.data = data;
	}

	public Serializable getData() {
		return data;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ConnectResultMessage["); //$NON-NLS-1$
		buf.append(data).append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}