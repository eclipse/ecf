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
package org.eclipse.ecf.provider.jmdns.identity;

import java.net.URI;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceID;

public class JMDNSServiceID extends ServiceID {

	private static final long serialVersionUID = 8389531866888790264L;

	/**
	 * @param namespace namespace for this ID
	 * @param type service type ID
	 * @param anURI uri for the service id
	 * @since 3.0
	 */
	public JMDNSServiceID(final Namespace namespace, final IServiceTypeID type, final URI anURI) {
		super(namespace, type, anURI);
	}
}
