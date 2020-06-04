/****************************************************************************
 * Copyright (c) 2007 Markus Kuppe.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.jslp.identity;

import java.net.URI;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceID;

public class JSLPServiceID extends ServiceID {
	private static final long serialVersionUID = -8211896244921087422L;

	JSLPServiceID(Namespace namespace, IServiceTypeID type, URI anURI) {
		super(namespace, type, anURI);
	}
}
