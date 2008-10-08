package ch.ethz.iks.slp.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import ch.ethz.iks.slp.ServiceLocationEnumeration;
import ch.ethz.iks.slp.ServiceLocationException;
import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;
import junit.framework.Assert;
import junit.framework.TestCase;

public class SelfDiscoveryTest extends TestCase {

	private ServiceURL service;
	
	public SelfDiscoveryTest() {
		super("runTests");
	}

	public void setUp() throws InterruptedException {
		try {
			service = new ServiceURL("service:osgi://gantenbein:123", 10800);
			Dictionary properties = new Hashtable();
			properties.put("attr", Boolean.FALSE);
			properties.put("other", "value");
			TestActivator.advertiser.register(service, properties);
		} catch (ServiceLocationException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	public void tearDown() throws InterruptedException {
		try {
			TestActivator.advertiser.deregister(service);
		} catch (ServiceLocationException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	public void runTests() throws Exception {
		testService();
		testAttributes();
		testFilter();
		testFilter2();
	}
	
	public void testService() throws Exception {

		System.out.println("locating service:osgi");
		int count = 0;
		for (ServiceLocationEnumeration services = TestActivator.locator
				.findServices(new ServiceType("service:osgi"), null, null); services
				.hasMoreElements();) {
			assertEquals(services.next().toString(),
					"service:osgi://gantenbein:123");
			count++;
		}
		assertEquals(count, 1);

	}

	public void testAttributes() throws Exception {

		System.out.println("listing the attributes for service:osgi");
		int count = 0;
		for (ServiceLocationEnumeration attributes = TestActivator.locator
				.findAttributes(new ServiceType("service:osgi"), null, null); attributes
				.hasMoreElements();) {
			final String attribute = attributes.next().toString();
			System.out.println("attribute " + attribute);
			assertTrue("(attr=false)".equals(attribute)
					|| "(other=value)".equals(attribute));
			count++;
		}
		assertEquals(count, 2);
	}

	public void testFilter() throws Exception {

		System.out.println("locating service:osgi with filter (attr=false)");
		int count = 0;
		for (ServiceLocationEnumeration services = TestActivator.locator
				.findServices(new ServiceType("service:osgi"), null,
						"(attr=false)"); services.hasMoreElements();) {
			assertEquals(services.next().toString(),
					"service:osgi://gantenbein:123");
			count++;
		}
		assertEquals(count, 1);
	}

	public void testFilter2() throws Exception {

		System.out.println("locating service:osgi with filter (attr=*)");
		int count = 0;
		for (ServiceLocationEnumeration services = TestActivator.locator
				.findServices(new ServiceType("service:osgi"), null, "(attr=*)"); services
				.hasMoreElements();) {
			assertEquals(services.next().toString(),
					"service:osgi://gantenbein:123");
			count++;
		}
		assertEquals(count, 1);
	}
}
