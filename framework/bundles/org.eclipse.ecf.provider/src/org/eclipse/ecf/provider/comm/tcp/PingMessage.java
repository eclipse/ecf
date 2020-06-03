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

public class PingMessage implements Serializable {
	private static final long serialVersionUID = 3258407318374004536L;

	/**
	 * @since 4.3
	 */
	public PingMessage() {
		//
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("PingMessage"); //$NON-NLS-1$
		return buf.toString();
	}
}