package ch.ethz.iks.slp.test;

import java.util.Enumeration;

import junit.framework.TestFailure;
import junit.framework.TestResult;
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

	private void startTests() {
		TestResult result = TestRunner.run(new SelfDiscoveryTest());
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
		}

	}
		
	public void stop(BundleContext context) throws Exception {
		advertiser = null;
		locator = null;
	}

}
