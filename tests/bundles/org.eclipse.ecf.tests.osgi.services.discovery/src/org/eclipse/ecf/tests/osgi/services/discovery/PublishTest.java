/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.ecf.osgi.services.discovery.ECFServicePublication;
import org.osgi.framework.BundleContext;
import org.osgi.service.discovery.ServicePublication;

public class PublishTest extends TestCase {

	BundleContext context;
	
	protected void setUp() throws Exception {
		super.setUp();
		context = Activator.getDefault().getContext();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		context = null;
	}
	
	class TestServicePublication implements ECFServicePublication {
		
	}
	
	protected Properties createServicePublicationProperties(List interfaces) {
		Properties props = new Properties();
		props.put(ECFServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME, interfaces);
		return props;
	}
	
	protected ServicePublication createServicePublication() {
		return new TestServicePublication();
	}
	
	public void testServicePublish() throws Exception {
	    List interfaces = new ArrayList();
	    interfaces.add("foo.bar");
		context.registerService(ServicePublication.class.getName(), createServicePublication(), createServicePublicationProperties(interfaces));
		Thread.sleep(10000);
	}
}
