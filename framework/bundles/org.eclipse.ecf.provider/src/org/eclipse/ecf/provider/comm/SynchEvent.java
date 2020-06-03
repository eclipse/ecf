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

/**
 * Synchronous connection event.
 * 
 */
public class SynchEvent extends ConnectionEvent {
	public SynchEvent(ISynchConnection conn, Object data) {
		super(conn, data);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("SynchEvent["); //$NON-NLS-1$
		buf.append("conn=").append(getConnection()).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("data=").append(getData()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

}