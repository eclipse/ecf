/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/
package org.eclipse.ecf.tests.provider.jmdns;

import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.tests.discovery.DiscoveryTest;

public class JMDNSDiscoveryServiceTest extends DiscoveryTest {

	public JMDNSDiscoveryServiceTest() {
		super("ecf.discovery.jmdns");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		//TODO http://bugs.eclipse.org/259480
		IServiceProperties serviceProperties = serviceInfo.getServiceProperties();
		serviceProperties.setPropertyString(DiscoveryTest.class.getName() + "servicePropertiesIntegerMax", "FIXME: http://bugs.eclipse.org/259480");
		serviceProperties.setPropertyString(DiscoveryTest.class.getName() + "servicePropertiesIntegerMin", "FIXME: http://bugs.eclipse.org/259480");
		serviceProperties.setPropertyString(DiscoveryTest.class.getName() + "servicePropertiesBoolean", "FIXME: http://bugs.eclipse.org/259480");
		serviceProperties.setPropertyString(DiscoveryTest.class.getName() + "servicePropertiesByte", "FIXME: http://bugs.eclipse.org/259480");
	}
}
