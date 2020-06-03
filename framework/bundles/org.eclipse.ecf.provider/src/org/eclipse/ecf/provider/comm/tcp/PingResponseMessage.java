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

public class PingResponseMessage implements Serializable {
	private static final long serialVersionUID = 3257569516165740857L;

	/**
	 * @since 4.3
	 */
	public PingResponseMessage() {
		//
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("PingResponseMessage[]"); //$NON-NLS-1$
		return buf.toString();
	}
}