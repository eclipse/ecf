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

public class AsynchMessage implements Serializable {
	private static final long serialVersionUID = 3258689905679873075L;
	Serializable data;

	/**
	 * @since 4.3
	 */
	public AsynchMessage() {
		//
	}

	/**
	 * @param data data for message
	 * @since 4.3
	 */
	public AsynchMessage(Serializable data) {
		this.data = data;
	}

	/**
	 * @return Serializable data from this message
	 * @since 4.3
	 */
	public Serializable getData() {
		return data;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("AsynchMessage["); //$NON-NLS-1$
		buf.append(data).append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}