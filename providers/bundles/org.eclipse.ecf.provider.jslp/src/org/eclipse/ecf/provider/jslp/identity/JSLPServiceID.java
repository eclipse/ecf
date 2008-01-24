/*******************************************************************************
 * Copyright (c) 2007 Markus Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe
 ******************************************************************************/
package org.eclipse.ecf.provider.jslp.identity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceID;
import org.eclipse.ecf.internal.provider.jslp.Activator;
import org.eclipse.ecf.internal.provider.jslp.JSLPDebugOptions;

public class JSLPServiceID extends ServiceID {
	private static final long serialVersionUID = -8211896244921087422L;

	JSLPServiceID(Namespace namespace, IServiceTypeID type, String name) {
		super(namespace, type, name);
	}

	public InetAddress getAddress() {
		try {
			return InetAddress.getByName(name);
		} catch (UnknownHostException e) {
			Trace.catching(Activator.PLUGIN_ID, JSLPDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "getAddress()", e); //$NON-NLS-1$
			try {
				return InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				Trace.catching(Activator.PLUGIN_ID, JSLPDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "getAddress()", e1); //$NON-NLS-1$
			}
		}
		return null;
	}
}
