/****************************************************************************
 * Copyright (c) 2007 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.provider.jmdns.identity;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.ServiceTypeID;

/**
 * 
 */
public class JMDNSServiceTypeID extends ServiceTypeID {

	private static final long serialVersionUID = 7549266915001431139L;

	protected JMDNSServiceTypeID(final Namespace namespace, final String type) {
		super(namespace, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.identity.ServiceTypeID#getInternal()
	 */
	public String getInternal() {
		final StringBuffer buf = new StringBuffer();
		//services
		buf.append("_"); //$NON-NLS-1$
		for (int i = 0; i < services.length; i++) {
			buf.append(services[i]);
			buf.append(DELIM);
		}

		buf.append(protocols[0]);
		buf.append("."); //$NON-NLS-1$

		buf.append(scopes[0]);
		buf.append("."); //$NON-NLS-1$

		return buf.toString();
	}
}
