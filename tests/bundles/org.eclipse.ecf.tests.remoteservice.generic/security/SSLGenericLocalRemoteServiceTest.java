/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.remoteservice.generic;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.tests.remoteservice.AbstractLocalRemoteServiceTest;

public class SSLGenericLocalRemoteServiceTest extends
		AbstractLocalRemoteServiceTest {

	protected void setUp() throws Exception {
		super.setUp();
		container = getContainerFactory().createContainer(SSLGeneric.CONSUMER_CONTAINER_TYPE);
		containerAdapter = (IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class);
	}
	
	protected void tearDown() throws Exception {
		container.disconnect();
		container.dispose();
		getContainerManager().removeAllContainers();
		super.tearDown();
	}
}
