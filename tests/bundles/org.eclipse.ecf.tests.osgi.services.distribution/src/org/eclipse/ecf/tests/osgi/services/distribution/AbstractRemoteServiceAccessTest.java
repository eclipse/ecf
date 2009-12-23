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

import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.tests.internal.osgi.services.distribution.Activator;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public abstract class AbstractRemoteServiceAccessTest extends
		AbstractDistributionTest {

	protected static final int REGISTER_WAIT = 30000;

	protected ServiceTracker createProxyServiceTracker(String clazz) throws InvalidSyntaxException {
		ServiceTracker st = new ServiceTracker(getContext(),getContext().createFilter("(&("+org.osgi.framework.Constants.OBJECTCLASS+"=" + clazz +")(" + SERVICE_IMPORTED + "=*))"),new ServiceTrackerCustomizer() {

			public Object addingService(ServiceReference reference) {
				Trace.trace(Activator.PLUGIN_ID, "addingService="+reference);
				return getContext().getService(reference);
			}

			public void modifiedService(ServiceReference reference,
					Object service) {
				Trace.trace(Activator.PLUGIN_ID, "modifiedService="+reference);
			}

			public void removedService(ServiceReference reference,
					Object service) {
				Trace.trace(Activator.PLUGIN_ID, "removedService="+reference+",svc="+service);
			}});
		st.open();
		return st;
	}
	
	protected Properties getServiceProperties() {
		Properties props = new Properties();
		props.put(SERVICE_EXPORTED_CONFIGS, getServerContainerName());
		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
		return props;
	}
	
	public void testGetProxy() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker for client
		ServiceTracker st = createProxyServiceTracker(classname);
		
		Properties props = getServiceProperties();
		// Server - register service with required OSGI property and some test properties
		// Actually register and wait a while
		ServiceRegistration registration = registerService(classname, new TestService1(),props);
		Thread.sleep(REGISTER_WAIT);
		
		// Client - Get service references that are proxies
		ServiceReference [] remoteReferences = st.getServiceReferences();
		assertTrue(remoteReferences != null);
		assertTrue(remoteReferences.length > 0);
		for(int i=0; i < remoteReferences.length; i++) {
			// Get OBJECTCLASS property from first remote reference
			String[] classes = (String []) remoteReferences[i].getProperty(org.osgi.framework.Constants.OBJECTCLASS);
			assertTrue(classes != null);
			// Check object class
			assertTrue(classname.equals(classes[0]));
		}
		// Now unregister original registration and wait
		registration.unregister();
		st.close();
		Thread.sleep(REGISTER_WAIT);
	}

	public void testGetProxyWithExtraProperties() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker for client
		ServiceTracker st = createProxyServiceTracker(classname);
		
		Properties props = getServiceProperties();
		// Put property foo with value bar into published properties
		String testPropKey = "foo";
		String testPropVal = "bar";
		props.put(testPropKey, testPropVal);

		// Server - register service with required OSGI property and some test properties
		// Actually register and wait a while
		ServiceRegistration registration = registerService(classname, new TestService1(),props);
		Thread.sleep(REGISTER_WAIT);
		
		// Client - Get service references that are proxies
		ServiceReference [] remoteReferences = st.getServiceReferences();
		assertTrue(remoteReferences != null);
		assertTrue(remoteReferences.length > 0);
		for(int i=0; i < remoteReferences.length; i++) {
			// Get OBJECTCLASS property from first remote reference
			String[] classes = (String []) remoteReferences[i].getProperty(org.osgi.framework.Constants.OBJECTCLASS);
			assertTrue(classes != null);
			// Check object class
			assertTrue(classname.equals(classes[0]));
			// Check the prop
			String prop = (String) remoteReferences[i].getProperty(testPropKey);
			assertTrue(prop != null);
			assertTrue(prop.equals(testPropVal));
		}
		// Now unregister original registration and wait
		registration.unregister();
		st.close();
		Thread.sleep(REGISTER_WAIT);
	}
	
	public void testGetAndUseProxy() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker for client
		ServiceTracker st = createProxyServiceTracker(classname);
		
		// Actually register and wait a while
		ServiceRegistration registration = registerService(classname, new TestService1(),getServiceProperties());
		Thread.sleep(REGISTER_WAIT);
		
		// Client - Get service references from service tracker
		ServiceReference [] remoteReferences = st.getServiceReferences();
		assertTrue(remoteReferences != null);
		assertTrue(remoteReferences.length > 0);
		
		for(int i=0; i < remoteReferences.length; i++) {
			// Get proxy/service
			TestServiceInterface1 proxy = (TestServiceInterface1) getContext().getService(remoteReferences[0]);
			assertNotNull(proxy);
			// Now use proxy
			String result = proxy.doStuff1();
			Trace.trace(Activator.PLUGIN_ID, "proxy.doStuff1 result="+result);
			assertTrue(TestServiceInterface1.TEST_SERVICE_STRING1.equals(result));
		}
		
		// Unregister on server and wait
		registration.unregister();
		st.close();
		Thread.sleep(REGISTER_WAIT);
	}

	public void testGetAndUseIRemoteService() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker for client
		ServiceTracker st = createProxyServiceTracker(classname);
		
		// Actually register and wait a while
		ServiceRegistration registration = registerService(classname, new TestService1(),getServiceProperties());
		Thread.sleep(REGISTER_WAIT);
		
		// Client - Get service references from service tracker
		ServiceReference [] remoteReferences = st.getServiceReferences();
		assertTrue(remoteReferences != null);
		assertTrue(remoteReferences.length > 0);
		
		for(int i=0; i < remoteReferences.length; i++) {
			Object o = remoteReferences[i].getProperty(SERVICE_IMPORTED);
			assertNotNull(o);
			assertTrue(o instanceof IRemoteService);
			IRemoteService rs = (IRemoteService) o;
			// Now call rs methods
			IRemoteCall call = createRemoteCall(TestServiceInterface1.class);
			if (call != null) {
				// Call synchronously
				Object result = rs.callSync(call);
				Trace.trace(Activator.PLUGIN_ID, "callSync.doStuff1 result="+result);
				assertNotNull(result);
				assertTrue(result instanceof String);
				assertTrue(TestServiceInterface1.TEST_SERVICE_STRING1.equals(result));
			}
		}
		
		// Unregister on server
		registration.unregister();
		st.close();
		Thread.sleep(REGISTER_WAIT);
	}

	protected IRemoteCall createRemoteCall(Class clazz) {
		if (clazz.equals(TestServiceInterface1.class)) {
			return new IRemoteCall() {

				public String getMethod() {
					return "doStuff1";
				}

				public Object[] getParameters() {
					return new Object[] {};
				}

				public long getTimeout() {
					return 30000;
				}
				
			};
		}
		return null;
	}
}
