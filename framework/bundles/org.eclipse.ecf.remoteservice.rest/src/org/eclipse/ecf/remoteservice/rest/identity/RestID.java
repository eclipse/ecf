/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.rest.identity;

import java.net.URI;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.URIID;

public class RestID extends URIID {

	private static final long serialVersionUID = 2082626839598770167L;

	private long rsId = 0;

	public RestID(Namespace namespace, URI uri) {
		super(namespace, uri);
	}

	public long getRsId() {
		return rsId;
	}

	public void setRsId(long rsId) {
		this.rsId = rsId;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("RestID["); //$NON-NLS-1$
		sb.append(getName()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}
