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
package org.eclipse.ecf.tests.discovery.identity;

import java.net.URI;

import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceID;

public class TestServiceID extends ServiceID {

	private static final long serialVersionUID = 2115324301690822446L;

	public TestServiceID(TestNamespace aNamespace, IServiceTypeID aServiceTypeID, URI anURI) {
		super(aNamespace, aServiceTypeID, anURI);
	}
}
