/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.provider.discovery.local;

import java.io.InputStream;

import org.eclipse.ecf.osgi.services.discovery.local.IServiceEndpointDescriptionPublisher;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;

@SuppressWarnings("restriction")
public class ServiceEndpoingDescriptionFilePublishTest extends AbstractServiceDescriptionPublishTest implements IDistributionConstants {

	private static final String HELLO_SED_GENERIC_FILE = "hello-service-description-generic.xml";
	private static final String HELLO_SED_ROSGI_FILE = "hello-service-description-rosgi.xml";

	public void testServiceDescriptionGenericPublishFromFile() throws Exception {
		InputStream serviceDescriptionStream = loadServiceDescription(HELLO_SED_GENERIC_FILE);
		IServiceEndpointDescriptionPublisher publisher = getServiceDescriptionPublisher();
		publisher.publishServiceDescription(serviceDescriptionStream);
		sleep(4000);
		serviceDescriptionStream = loadServiceDescription(HELLO_SED_GENERIC_FILE);
		publisher.unpublishServiceDescription(serviceDescriptionStream);
	}
	
	public void testServiceDescriptionROSGiPublishFromFile() throws Exception {
		InputStream serviceDescriptionStream = loadServiceDescription(HELLO_SED_ROSGI_FILE);
		IServiceEndpointDescriptionPublisher publisher = getServiceDescriptionPublisher();
		publisher.publishServiceDescription(serviceDescriptionStream);
		sleep(4000);
		serviceDescriptionStream = loadServiceDescription(HELLO_SED_ROSGI_FILE);
		publisher.unpublishServiceDescription(serviceDescriptionStream);
	}

}
