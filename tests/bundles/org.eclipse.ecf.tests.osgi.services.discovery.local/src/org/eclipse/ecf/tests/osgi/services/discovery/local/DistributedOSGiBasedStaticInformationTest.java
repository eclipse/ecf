/* 
 * Copyright (c) 2009 Siemens Enterprise Communications GmbH & Co. KG, 
 * Germany. All rights reserved.
 *
 * Siemens Enterprise Communications GmbH & Co. KG is a Trademark Licensee 
 * of Siemens AG.
 *
 * This material, including documentation and any related computer programs,
 * is protected by copyright controlled by Siemens Enterprise Communications 
 * GmbH & Co. KG and its licensors. All rights are reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ecf.tests.osgi.services.discovery.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceTracker;
import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * @author Thomas Kiesslich
 * 
 */
public class DistributedOSGiBasedStaticInformationTest extends TestCase {

	/**
	 * @throws java.lang.Exception
	 */
	protected void setUp() throws Exception {
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * These services come from the default location of this bundle.
	 */
	public void testReadingFromXMLBundle() {
		String interfaceName1 = "org.eclipse.ecf.pojo.hello.HelloService";
		String interfaceName2 = "org.eclipse.ecf.pojo.hello.GreeterService";
		DiscoveredServiceTrackerImpl discoServiceTracker = new DiscoveredServiceTrackerImpl();
		registerDiscoveredServiceTracker(null, null, null, discoServiceTracker);
		assertEquals(2, discoServiceTracker.getAvailNotifications());
		Iterator /* <ServiceEndpointDescription> */result = discoServiceTracker
				.getAvailableDescriptions().iterator();
		boolean foundInterface1 = false;
		boolean foundInterface2 = false;
		while (result.hasNext()) {
			ServiceEndpointDescription sed = (ServiceEndpointDescription) result
					.next();
			if (sed.getProvidedInterfaces().contains(interfaceName1)) {
				Map props = sed.getProperties();
				assertNotNull(props);
				assertEquals("SOAP HTTP", props.get("service.intents"));
				assertEquals("pojo", props
						.get("osgi.remote.configuration.type"));
				assertEquals("http://localhost:9000/hello", props
						.get("osgi.remote.configuration.pojo.address"));
				foundInterface1 = true;
			} else if (sed.getProvidedInterfaces().contains(interfaceName2)) {
				Map props = sed.getProperties();
				assertNotNull(props);
				assertEquals("SOAP HTTP", props.get("service.intents"));
				assertEquals("pojo", props
						.get("osgi.remote.configuration.type"));
				assertEquals("http://localhost:9005/greeter", props
						.get("osgi.remote.configuration.pojo.address"));
				foundInterface2 = true;
			} else {
				fail("a ServiceEndpointDescription found that is not expected");
			}
		}
		assertTrue(foundInterface1);
		assertTrue(foundInterface2);
	}

	/**
	 * This service comes from the default location in bundle
	 * org.eclipse.ecf.tests.osgi.services.discovery.local.poststarted.
	 */
	public void testGetServicesFromDefaultLocation() {
		String interfaceName1 = "org.eclipse.ecf.helloworld.HelloWorldService"; //$NON-NLS-1$
		DiscoveredServiceTrackerImpl discoServiceTracker = new DiscoveredServiceTrackerImpl();
		Activator ac = Activator.getDefault();
		assertNotNull(ac);
		registerDiscoveredServiceTracker(interfaceName1, null, null,
				discoServiceTracker);
		// start up post started
		boolean startBundle = false;
		try {
			startBundle = ac
					.startBundle("org.eclipse.ecf.tests.osgi.services.discovery.local.poststarted");
		} catch (BundleException e) {
			fail(e.getMessage());
		}
		assertTrue(startBundle);
		assertEquals(1, discoServiceTracker.getAvailNotifications());
		Iterator /* <ServiceEndpointDescription> */result = discoServiceTracker
				.getAvailableDescriptions().iterator();
		boolean foundInterface1 = false;
		while (result.hasNext()) {
			ServiceEndpointDescription sed = (ServiceEndpointDescription) result
					.next();
			if (sed.getProvidedInterfaces().contains(interfaceName1)) {
				Map props = sed.getProperties();
				assertNotNull(props);
				assertEquals("pojo", props
						.get("osgi.remote.configuration.type"));
				assertEquals("https://localhost:8080/helloworld", props
						.get("osgi.remote.configuration.pojo.address"));
				foundInterface1 = true;
			} else {
				fail("a ServiceEndpointDescription found that is not expected");
			}
		}
		assertTrue(foundInterface1);
	}

	/**
	 * This services are all referenced from the Manifest Header entry
	 * remote-service in the bundle
	 * org.eclipse.ecf.tests.osgi.services.discovery.local.poststarted2 .
	 */
	public void testGetServicesFromManifestDefinedLocations() {
		createDataFilesInTmp();
		
		String interfaceName1 = "org.eclipse.ecf.hellomoon.HelloMoonService";
		String interfaceName2 = "org.eclipse.ecf.galileo.HelloGalileoService";
		String interfaceName3 = "org.eclipse.ecf.ganymede.HelloGanymedeService";
		DiscoveredServiceTrackerImpl discoServiceTracker = new DiscoveredServiceTrackerImpl();
		Activator ac = Activator.getDefault();
		assertNotNull(ac);
		registerDiscoveredServiceTracker(interfaceName1, interfaceName2,
				interfaceName3, discoServiceTracker);
		// start up post started
		boolean startBundle = false;
		try {
			startBundle = ac
					.startBundle("org.eclipse.ecf.tests.osgi.services.discovery.local.poststarted2");
		} catch (BundleException e) {
			fail(e.getMessage());
		}
		assertTrue(startBundle);
		assertEquals(3, discoServiceTracker.getAvailNotifications());
		Iterator /* <ServiceEndpointDescription> */result = discoServiceTracker
				.getAvailableDescriptions().iterator();
		boolean foundInterface1 = false;
		boolean foundInterface2 = false;
		boolean foundInterface3 = false;
		while (result.hasNext()) {
			ServiceEndpointDescription sed = (ServiceEndpointDescription) result
					.next();
			if (sed.getProvidedInterfaces().contains(interfaceName1)) {
				Map props = sed.getProperties();
				assertNotNull(props);
				assertEquals("pojo", props
						.get("osgi.remote.configuration.type"));
				assertEquals("jsoc://moon:4711/hellomoon", props
						.get("osgi.remote.configuration.pojo.address"));
				foundInterface1 = true;
			} else if (sed.getProvidedInterfaces().contains(interfaceName2)) {
				Map props = sed.getProperties();
				assertNotNull(props);
				assertEquals("pojo", props
						.get("osgi.remote.configuration.type"));
				assertEquals("jssoc://galileo:4712/hellogalileo", props
						.get("osgi.remote.configuration.pojo.address"));
				foundInterface2 = true;
			} else if (sed.getProvidedInterfaces().contains(interfaceName3)) {
				Map props = sed.getProperties();
				assertNotNull(props);
				assertEquals("pojo", props
						.get("osgi.remote.configuration.type"));
				assertEquals("jssoc://ganymede:4713/helloganymede", props
						.get("osgi.remote.configuration.pojo.address"));
				foundInterface3 = true;
			} else {
				fail("a ServiceEndpointDescription found that is not expected");
			}
		}
		assertTrue(foundInterface1);
		assertTrue(foundInterface2);
		assertTrue(foundInterface3);
	}

	// create the test data in java.io.tmpdir first for the test to read it later
	private void createDataFilesInTmp() {
		String property = System.getProperty("java.io.tmpdir");
		Activator ac = Activator.getDefault();
		Bundle bundle = ac.getBundleContext().getBundle();
		Enumeration e1 = bundle.findEntries("data", "*.xml", false);
		while (e1.hasMoreElements()) {
			try {
				URL url = (URL) e1.nextElement();
				File file = new File(FileLocator.toFileURL(url).getFile());
				File tempFile = null;
				if(file.getName().equals("HelloGalileoService.xml")) {
					tempFile = new File(property + File.separator + file.getName());
				} else {
					File dir = new File(property + File.separator + "poststart2" + File.separator);
					dir.mkdir();
					dir.deleteOnExit();
					tempFile = new File(property + File.separator + "poststart2" + File.separator + file.getName());
				}
				copyTo(file, tempFile);
				tempFile.deleteOnExit();
			} catch (IOException e) {
				fail(e.getMessage());
			}
		}
	}
	
	private void copyTo(File from, File to) throws IOException {
		InputStream in = new FileInputStream(from);
		OutputStream out = new FileOutputStream(to);
		
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * @param interfaceName1
	 * @param interfaceName2
	 * @param discoServiceTracker
	 */
	private void registerDiscoveredServiceTracker(String interfaceName1,
			String interfaceName2, String interfaceName3,
			DiscoveredServiceTrackerImpl discoServiceTracker) {
		Activator ac = Activator.getDefault();
		assertNotNull(ac);
		BundleContext bc = ac.getBundleContext();

		Dictionary properties = new Hashtable();
		if (interfaceName1 != null) {
			List interfaces = new ArrayList();
			interfaces.add(interfaceName1);
			if (interfaceName2 != null) {
				interfaces.add(interfaceName2);
			}
			if (interfaceName3 != null) {
				interfaces.add(interfaceName3);
			}
			properties
					.put(
							DiscoveredServiceTracker.INTERFACE_MATCH_CRITERIA,
							interfaces);
		}
		bc.registerService(DiscoveredServiceTracker.class.getName(),
				discoServiceTracker, properties);
	}
}
