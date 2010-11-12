package org.eclipse.ecf.tests.osgi.services.remoteserviceadmin;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultDiscoveredEndpointDescriptionFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultServiceInfoFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;

public class EndpointDescriptionFactoryTest extends AbstractMetadataFactoryTest {

	protected void setUp() throws Exception {
		super.setUp();
		discoveryAdvertiser = getDiscoveryAdvertiser();
		Assert.isNotNull(discoveryAdvertiser);
		serviceInfoFactory = new DefaultServiceInfoFactory();
		Assert.isNotNull(serviceInfoFactory);
		discoveryLocator = getDiscoveryLocator();
		Assert.isNotNull(discoveryLocator);
		endpointDescriptionFactory = new DefaultDiscoveredEndpointDescriptionFactory();
		Assert.isNotNull(endpointDescriptionFactory);
	}
	
	public void testCreateRequiredEndpointDescriptionFromServiceInfo() throws Exception {
		EndpointDescription published = createRequiredEndpointDescription();
		assertNotNull(published);
		IServiceInfo serviceInfo = createServiceInfoForDiscovery(published);
		assertNotNull(serviceInfo);
		org.osgi.service.remoteserviceadmin.EndpointDescription received = createEndpointDescriptionFromDiscovery(serviceInfo);
		assertNotNull(received);
		assertTrue(published.equals(received));
	}

	public void testCreateFullEndpointDescriptionFromServiceInfo() throws Exception {
		EndpointDescription published = createFullEndpointDescription();
		assertNotNull(published);
		IServiceInfo serviceInfo = createServiceInfoForDiscovery(published);
		assertNotNull(serviceInfo);
		org.osgi.service.remoteserviceadmin.EndpointDescription received = createEndpointDescriptionFromDiscovery(serviceInfo);
		assertNotNull(received);
		assertTrue(published.equals(received));
	}

}
