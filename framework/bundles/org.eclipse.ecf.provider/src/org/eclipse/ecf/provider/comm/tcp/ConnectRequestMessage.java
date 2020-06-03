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
import java.net.URI;

public class ConnectRequestMessage implements Serializable {
	private static final long serialVersionUID = 3257844363974226229L;
	URI target;
	Serializable data;

	public ConnectRequestMessage(URI target, Serializable data) {
		this.target = target;
		this.data = data;
	}

	public URI getTarget() {
		return target;
	}

	public Serializable getData() {
		return data;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ConnectRequestMessage["); //$NON-NLS-1$
		buf.append(target).append(";").append(data).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}