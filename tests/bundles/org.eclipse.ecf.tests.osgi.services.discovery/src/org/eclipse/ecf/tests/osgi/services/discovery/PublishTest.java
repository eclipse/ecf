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
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceTracker;
import org.eclipse.ecf.osgi.services.discovery.RemoteServicePublication;
import org.eclipse.ecf.osgi.services.discovery.ServicePublication;
import org.eclipse.ecf.tests.internal.osgi.discovery.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class PublishTest extends TestCase {

	BundleContext context;
	ID endpointID;

	protected void setUp() throws Exception {
		super.setUp();
		context = Activator.getDefault().getContext();
		endpointID = IDFactory.getDefault().createStringID("myid");
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		endpointID = null;
		context = null;
	}
	
	class TestServicePublication implements RemoteServicePublication {

		public ServiceReference getReference() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	class DiscoveredServiceTrackerImpl implements DiscoveredServiceTracker {

		public void serviceChanged(DiscoveredServiceNotification notification) {
			Trace.trace(Activator.BUNDLE_NAME, "DiscoveredServiceTrackerImpl.serviceChanged("+notification+")");
		}
		
	}
	
	protected Properties createServicePublicationProperties(List interfaces) {
		Properties props = new Properties();
		props.put(RemoteServicePublication.SERVICE_INTERFACE_NAME, interfaces);
		props.put(RemoteServicePublication.ENDPOINT_CONTAINERID, endpointID);
		byte[] serviceIdAsBytes = new Long(100).toString().getBytes();
		props.put("ecf.rsvc.id", serviceIdAsBytes);
		props.put("ecf.rsvc.ns", "namespace");
		return props;
	}
	
	protected ServicePublication createServicePublication() {
		return new TestServicePublication();
	}
	
	public void testServicePublish() throws Exception {
	    List interfaces = new ArrayList();
	    interfaces.add("foo.bar");
		ServiceRegistration reg = context.registerService(ServicePublication.class.getName(), createServicePublication(), (Dictionary) createServicePublicationProperties(interfaces));
		Thread.sleep(60000);
		reg.unregister();
		Thread.sleep(60000);
	}
	
}
