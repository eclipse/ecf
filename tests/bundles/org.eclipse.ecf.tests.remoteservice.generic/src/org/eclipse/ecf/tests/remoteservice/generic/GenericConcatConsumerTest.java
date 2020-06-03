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
package org.eclipse.ecf.tests.remoteservice.generic;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.tests.remoteservice.AbstractConcatConsumerTestCase;

public class GenericConcatConsumerTest extends AbstractConcatConsumerTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		IContainer container = createContainer();
		rsContainer = createRemoteServiceContainer(container);
		targetID = createID(container, Generic.HOST_CONTAINER_ENDPOINT_ID);
	}


	protected String getContainerType() {
		return Generic.CONSUMER_CONTAINER_TYPE;
	}

}
