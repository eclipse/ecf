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
package org.eclipse.ecf.tests.osgi.services.distribution.generic;


import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.osgi.services.distribution.AbstractRemoteServiceRegisterTest;

public class GenericRemoteServiceRegisterTest extends AbstractRemoteServiceRegisterTest {

	protected String getServerContainerTypeName() {
		return "ecf.generic.server";
	}

	protected String getClientContainerName() {
		return "ecf.generic.client";
	}

	protected ID getServerCreateID() {
		return IDFactory.getDefault().createStringID("ecftcp://localhost:3282/server");
	}
}
