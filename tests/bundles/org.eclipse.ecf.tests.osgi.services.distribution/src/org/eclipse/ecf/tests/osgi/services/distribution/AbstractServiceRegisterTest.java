/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution;

import java.util.Properties;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.Constants;

public abstract class AbstractServiceRegisterTest extends
		AbstractDistributionTest {

	public void testRegisterAllContainers() throws Exception {
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		props.put("foo", "bar");
		registerDefaultService(props);
		Thread.sleep(60000);
	}

	public void testRegisterServerContainer() throws Exception {
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		props.put("foo", "bar");
		registerDefaultService(props);
		Thread.sleep(60000);
	}

}
