/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.jslp;

import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.provider.jslp.container.JSLPServiceInfo;
import org.eclipse.ecf.provider.jslp.identity.JSLPNamespace;
import org.eclipse.ecf.tests.discovery.DiscoveryTestHelper;
import org.eclipse.ecf.tests.discovery.ServiceInfoTest;

public class JSLPServiceInfoTest extends ServiceInfoTest {

	public JSLPServiceInfoTest() {
		super();
		uri = DiscoveryTestHelper.createDefaultURI();
		priority = DiscoveryTestHelper.PRIORITY;
		weight = DiscoveryTestHelper.WEIGHT;
		serviceProperties = new ServiceProperties();
		serviceProperties.setProperty("foobar", new String("foobar"));
		Namespace namespace = IDFactory.getDefault().getNamespaceByName(
				JSLPNamespace.NAME);
		try {
			serviceID = (IServiceID) IDFactory.getDefault().createID(namespace,
					new Object[] {DiscoveryTestHelper.SERVICE_TYPE, DiscoveryTestHelper.getHost()});
		} catch (IDCreateException e) {
			fail(e.getMessage());
		}
		serviceInfo = new JSLPServiceInfo(uri, serviceID, priority, weight,
				serviceProperties);
	}

	protected IServiceInfo getServiceInfo(IServiceInfo aServiceInfo) {
		return new JSLPServiceInfo(aServiceInfo);
	}
}
