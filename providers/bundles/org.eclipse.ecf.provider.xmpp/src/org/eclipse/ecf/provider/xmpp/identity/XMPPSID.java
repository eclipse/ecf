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
package org.eclipse.ecf.provider.xmpp.identity;

import java.net.URISyntaxException;
import org.eclipse.ecf.core.identity.Namespace;

public class XMPPSID extends XMPPID {

	private static final long serialVersionUID = -7665808387581704917L;

	public XMPPSID(Namespace namespace, String unamehost) throws URISyntaxException {
		super(namespace,unamehost);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("XMPPSID["); //$NON-NLS-1$
		sb.append(toExternalForm()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}

}
