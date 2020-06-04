/****************************************************************************
 * Copyright (c) 2008 Versant Corp.
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
package org.eclipse.ecf.internal.provider.discovery;

import java.net.URI;

import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceID;

public class CompositeServiceID extends ServiceID {

	private static final long serialVersionUID = -5296876662431183581L;

	/**
	 * @param compositeNamespace
	 * @param serviceTypeID
	 * @param anURI
	 */
	public CompositeServiceID(final CompositeNamespace compositeNamespace, final IServiceTypeID serviceTypeID, final URI anURI) {
		super(compositeNamespace, serviceTypeID, anURI);
	}
}
