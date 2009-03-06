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

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.distribution.DistributionProvider;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public abstract class AbstractServiceRegisterTest extends
		AbstractDistributionTest {

	private static final int REGISTER_WAIT = 10000;

	public void testRegisterServer() throws Exception {
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		props.put("foo", "bar");
		ServiceRegistration registration = registerDefaultService(props);
		Thread.sleep(REGISTER_WAIT);
		registration.unregister();
		Thread.sleep(REGISTER_WAIT);
	}

	public void testGetProxy() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker
		ServiceTracker st = new ServiceTracker(getContext(),getContext().createFilter("(&("+org.osgi.framework.Constants.OBJECTCLASS+"=" + classname +")(" + OSGI_REMOTE + "=*))"),new ServiceTrackerCustomizer() {

			public Object addingService(ServiceReference reference) {
				System.out.println("addingService="+reference);
				return getContext().getService(reference);
			}

			public void modifiedService(ServiceReference reference,
					Object service) {
				System.out.println("modifiedService="+reference);
				
			}

			public void removedService(ServiceReference reference,
					Object service) {
				System.out.println("removedService="+reference+",svc="+service);
			}});
		st.open();
		
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		// Put property foo with value bar into published properties
		String testPropKey = "foo";
		String testPropVal = "bar";
		props.put(testPropKey, testPropVal);
		ServiceRegistration registration = registerService(classname, new TestService1(),props);
		Thread.sleep(REGISTER_WAIT);
		// Get service references that are proxies
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
		// Now unregister original registration
		registration.unregister();
		Thread.sleep(REGISTER_WAIT);
	}
	
	public void testGetAndUseProxy() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker
		ServiceTracker st = new ServiceTracker(getContext(),getContext().createFilter("(&("+org.osgi.framework.Constants.OBJECTCLASS+"=" + classname +")(" + OSGI_REMOTE + "=*))"),null);
		st.open();
		
		// Register service on server
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		ServiceRegistration registration = registerService(classname, new TestService1(),props);
		Thread.sleep(REGISTER_WAIT);
		
		// Get service references from service tracker
		ServiceReference [] remoteReferences = st.getServiceReferences();
		assertTrue(remoteReferences != null);
		assertTrue(remoteReferences.length > 0);
		
		for(int i=0; i < remoteReferences.length; i++) {
			// Get proxy/service
			TestServiceInterface1 proxy = (TestServiceInterface1) getContext().getService(remoteReferences[0]);
			assertNotNull(proxy);
			// Now use proxy
			String result = proxy.doStuff1();
			System.out.println("proxy.doStuff1 result="+result);
			assertTrue(TestServiceInterface1.TEST_SERVICE_STRING1.equals(result));
		}
		
		// Unregister on server
		registration.unregister();
		
		Thread.sleep(REGISTER_WAIT);
	}

	public void testGetAndUseIRemoteService() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker
		ServiceTracker st = new ServiceTracker(getContext(),getContext().createFilter("(&("+org.osgi.framework.Constants.OBJECTCLASS+"=" + classname +")(" + OSGI_REMOTE + "=*))"),null);
		st.open();
		
		// Register service on server
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		ServiceRegistration registration = registerService(classname, new TestService1(),props);
		Thread.sleep(REGISTER_WAIT);
		
		// Get service references from service tracker
		ServiceReference [] remoteReferences = st.getServiceReferences();
		assertTrue(remoteReferences != null);
		assertTrue(remoteReferences.length > 0);
		
		for(int i=0; i < remoteReferences.length; i++) {
			Object o = remoteReferences[i].getProperty(OSGI_REMOTE);
			assertNotNull(o);
			assertTrue(o instanceof IRemoteService);
			IRemoteService rs = (IRemoteService) o;
			// Now call rs methods
			IRemoteCall call = createRemoteCall(TestServiceInterface1.class);
			if (call != null) {
				// Call synchronously
				Object result = rs.callSync(call);
				System.out.println("callSync.doStuff1 result="+result);
				assertNotNull(result);
				assertTrue(result instanceof String);
				assertTrue(TestServiceInterface1.TEST_SERVICE_STRING1.equals(result));
			}
		}
		
		// Unregister on server
		registration.unregister();
		Thread.sleep(REGISTER_WAIT);
	}

	public void testGetExposedServicesFromDistributionProvider() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker for distribution provider
		ServiceTracker st = new ServiceTracker(getContext(),DistributionProvider.class.getName(),null);
		st.open();
		DistributionProvider distributionProvider = (DistributionProvider) st.getService();
		assertNotNull(distributionProvider);
		
		Collection exposedServices = distributionProvider.getExposedServices();
		assertNotNull(exposedServices);

		// Register service on server
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		ServiceRegistration registration = registerService(classname, new TestService1(),props);
		Thread.sleep(REGISTER_WAIT);
		
		exposedServices = distributionProvider.getExposedServices();
		assertNotNull(exposedServices);
		int exposedLength = exposedServices.size();
		assertTrue(exposedLength > 0);
		for(Iterator i=exposedServices.iterator(); i.hasNext(); ) {
			Object o = ((ServiceReference) i.next()).getProperty(OSGI_REMOTE_INTERFACES);
			assertTrue(o != null);
		}

		// Unregister on server
		registration.unregister();
		Thread.sleep(REGISTER_WAIT);
		
		exposedServices= distributionProvider.getExposedServices();
		assertNotNull(exposedServices);
		assertTrue(exposedServices.size() == (exposedLength - 1));

	}

	public void testGetRemoteServicesFromDistributionProvider() throws Exception {
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker for distribution provider
		ServiceTracker st = new ServiceTracker(getContext(),DistributionProvider.class.getName(),null);
		st.open();
		DistributionProvider distributionProvider = (DistributionProvider) st.getService();
		assertNotNull(distributionProvider);
		
		Collection remoteServices = distributionProvider.getRemoteServices();
		assertNotNull(remoteServices);

		// Register service on server
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		ServiceRegistration registration = registerService(classname, new TestService1(),props);
		Thread.sleep(REGISTER_WAIT);
		
		remoteServices = distributionProvider.getRemoteServices();
		assertNotNull(remoteServices);
		int remotesLength = remoteServices.size();
		assertTrue(remotesLength > 0);
		for(Iterator i=remoteServices.iterator(); i.hasNext(); ) {
			Object o = ((ServiceReference) i.next()).getProperty(OSGI_REMOTE);
			assertTrue(o != null);
		}
		// Unregister on server
		registration.unregister();
		Thread.sleep(REGISTER_WAIT);
		
		remoteServices= distributionProvider.getRemoteServices();
		assertNotNull(remoteServices);
		assertTrue(remoteServices.size() == (remotesLength - 1));

	}

	protected IRemoteCall createRemoteCall(Class clazz) {
		if (clazz.equals(TestServiceInterface1.class)) {
			return new IRemoteCall() {

				public String getMethod() {
					return "doStuff1";
				}

				public Object[] getParameters() {
					return null;
				}

				public long getTimeout() {
					return 30000;
				}
				
			};
		}
		return null;
	}
}
