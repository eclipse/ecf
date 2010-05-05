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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.ecf.internal.tests.provider.discovery.local.Activator;
import org.eclipse.ecf.osgi.services.discovery.local.IServiceEndpointDescriptionPublisher;
import org.eclipse.ecf.tests.ECFAbstractTestCase;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractServiceDescriptionPublishTest extends ECFAbstractTestCase {

	private static final String SERVICE_DESCRIPTION_PATH = "/service-descriptions/";
	
	private IServiceEndpointDescriptionPublisher publisher;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ServiceTracker st = new ServiceTracker(Activator.getContext(),IServiceEndpointDescriptionPublisher.class.getName(),null);
		st.open();
		publisher = (IServiceEndpointDescriptionPublisher) st.getService();
		st.close();
	}
	
	@Override
	protected void tearDown() throws Exception {
		publisher = null;
		super.tearDown();
	}
	
	protected IServiceEndpointDescriptionPublisher getServiceDescriptionPublisher() {
		return publisher;
	}
	
	protected InputStream loadServiceDescription(String fileName) throws IOException {
		BundleContext context = Activator.getContext();
		URL url = context.getBundle().getEntry(SERVICE_DESCRIPTION_PATH + fileName);
		if (url == null) throw new IOException("Cannot find service description file name="+fileName);
		return url.openStream();
	}
}
