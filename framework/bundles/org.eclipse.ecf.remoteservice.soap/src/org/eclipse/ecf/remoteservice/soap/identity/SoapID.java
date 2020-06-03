/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.soap.identity;

import java.net.URI;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.URIID;

public class SoapID extends URIID {

	private static final long serialVersionUID = -694490773145158986L;

	public SoapID(Namespace namespace, URI uri) {
		super(namespace, uri);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("SoapID["); //$NON-NLS-1$
		sb.append(getName()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}
