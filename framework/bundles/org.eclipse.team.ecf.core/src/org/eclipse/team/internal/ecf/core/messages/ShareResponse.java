/****************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.team.internal.ecf.core.messages;

public class ShareResponse implements IResponse {

	private static final long serialVersionUID = -7783203563333880201L;

	private final boolean ok;

	public ShareResponse(boolean ok) {
		this.ok = ok;
	}

	public Object getResponse() {
		return ok ? Boolean.TRUE : Boolean.FALSE;
	}

	public String toString() {
		return "ShareResponse[ok=" + ok + ']'; //$NON-NLS-1$
	}

}
