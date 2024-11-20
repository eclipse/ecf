package org.eclipse.ecf.tests.core;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Hashtable;

import javax.net.ssl.SSLContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.security.SSLContextFactory;
import org.eclipse.ecf.internal.tests.core.Activator;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import junit.framework.TestCase;

public class SSLContextFactoryTest extends TestCase {

	protected SSLContextFactory sslContextFactory;
	protected static final String testProtocol = "TLS";
	protected void registerProviders() throws NoSuchProviderException, NoSuchAlgorithmException {
		// Register as Provider service
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(SSLContextFactory.PROTOCOL_PROPERTY_NAME, testProtocol);
		Activator.getContext().registerService(Provider.class, SSLContext.getDefault().getProvider(), props);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		registerProviders();
		ServiceTracker<SSLContextFactory,SSLContextFactory> tracker = new ServiceTracker<SSLContextFactory,SSLContextFactory>(Activator.getContext(), SSLContextFactory.class, null);
		tracker.open();
		this.sslContextFactory = tracker.getService();
		tracker.close();
		assertNotNull(this.sslContextFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSSLContextGetInstance1() throws Exception {
		SSLContext context = this.sslContextFactory.getInstance(testProtocol);
		assertNotNull(context);
	}
	
}
