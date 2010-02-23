package ch.ethz.iks.slp.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.Locator;

public class TestActivator implements BundleActivator {

	static Advertiser advertiser;
	static Locator locator;

	public void start(BundleContext context) throws Exception {
		ServiceReference advertiserRef = null;
		try {
			ServiceReference[] aSrefs = context
						.getServiceReferences(Advertiser.class.getName(), null);
			for (int i = 0; i < aSrefs.length; i++) {
				ServiceReference serviceReference = aSrefs[i];
				String version = (String) serviceReference.getBundle().getHeaders().get("Bundle-Version");
				if(version.equals(System.getProperty("net.slp.versionUnderTest"))) {
					advertiserRef = serviceReference;
				} else { 
					context.getService(serviceReference);
				}
			}
			
			advertiser = (Advertiser) context.getService(advertiserRef);
			locator = (Locator) context.getService(context
					.getServiceReference(Locator.class.getName()));
		} catch (Exception e) {
			System.exit(1);
		}
		
		startTests();
	}

	protected void startTests() {
		new Thread(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				TestSuite suite = new TestSuite();
				Collection collection = new ArrayList();
				
				collection.add(SelfDiscoveryTest.class);
				
				for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
					Class clazz = (Class) iterator.next();
					// run all methods starting with "test*"
					Method[] methods = clazz.getMethods();
					for (int i = 0; i < methods.length; i++) {
						if (methods[i].getName().startsWith("test")) {
							TestCase testCase;
							try {
								testCase = (TestCase) clazz.newInstance();
								testCase.setName(methods[i].getName());
								suite.addTest(testCase);
							} catch (InstantiationException e) {
								// may never happen
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// may never happen
								e.printStackTrace();
							}
						}
					}
				}
				TestResult result = TestRunner.run(suite);
				if (result.wasSuccessful()) {
					System.exit(0);
				} else {
					if (result.errorCount() > 0) {
						System.err.println("Errors:");
						for (Enumeration errors = result.errors(); errors
								.hasMoreElements();) {
							TestFailure error = (TestFailure) errors.nextElement();
							System.err.println(error.trace());
						}
					}
					if (result.failureCount() > 0) {
						System.err.println("Failures:");
						for (Enumeration failures = result.failures(); failures
								.hasMoreElements();) {
							TestFailure failure = (TestFailure) failures.nextElement();
							System.err.println(failure.trace());
						}
					}
					System.exit(1);
				};
			}
		}).start();
	}
		
	public void stop(BundleContext context) throws Exception {
		advertiser = null;
		locator = null;
	}

}
