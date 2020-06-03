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
package org.eclipse.ecf.tests.remoteservice.r_osgi;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.tests.remoteservice.AbstractConcatConsumerTestCase;

public class ROsgiConcatConsumerTest extends AbstractConcatConsumerTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		IContainer container = createContainer("r-osgi://localhost:9279");
		rsContainer = createRemoteServiceContainer(container);
		targetID = createID(container, R_OSGi.HOST_CONTAINER_ENDPOINT_ID);
	}

	protected String getContainerType() {
		return R_OSGi.CONSUMER_CONTAINER_TYPE;
	}

	public void testGetRemoteServiceWithLocallyRegisteredService()
	throws Exception {
		// This test case is inappropriate for r-osgi, as it behaves differently
		// than other remote service API providers wrt local remote service registration.
	}
}
