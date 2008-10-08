package ch.ethz.iks.slp.impl;

import java.util.Enumeration;

import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.textui.TestRunner;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.Locator;

public class TestActivator implements BundleActivator {

	static Advertiser advertiser;
	static Locator locator;

	public void start(BundleContext context) throws Exception {
		try {
		advertiser = (Advertiser) context.getService(context
				.getServiceReference(Advertiser.class.getName()));
		locator = (Locator) context.getService(context
				.getServiceReference(Locator.class.getName()));
		} catch (Exception e) {
			System.exit(1);
		}
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
