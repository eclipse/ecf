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

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.discovery.DiscoveredServiceNotification;
import org.osgi.service.discovery.DiscoveredServiceTracker;
import org.osgi.service.discovery.ServicePublication;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractServiceRegisterTest extends
		AbstractDistributionTest {

	public void testRegisterAllContainers() throws Exception {
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		props.put("foo", "bar");
		registerDefaultService(props);
		Thread.sleep(60000);
	}

	public void testRegisterServerContainer() throws Exception {
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		props.put("foo", "bar");
		registerDefaultService(props);
		Thread.sleep(60000);
	}

	public void testRegisterServicePublication() throws Exception {
		// First set up service tracker for ServicePublication
		ServiceTracker servicePublicationTracker = new ServiceTracker(getContext(), ServicePublication.class.getName(), null);
		servicePublicationTracker.open();
		
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		ServiceRegistration reg = registerDefaultService(props);

		Thread.sleep(10000);
		
		// Now get ServicePublications from tracker
		ServiceReference [] servicePublicationSRs = servicePublicationTracker.getServiceReferences();
		assertTrue(servicePublicationSRs != null);
		assertTrue(servicePublicationSRs.length > 0);
		
		// Now unregister
		registrations.remove(reg);
		reg.unregister();
		
		// Now get new service references again.
		ServiceReference[] servicePublicationSRsNew = servicePublicationTracker.getServiceReferences();
		if (servicePublicationSRsNew != null) {
			assertTrue(servicePublicationSRsNew.length < servicePublicationSRs.length);
		}
	}
	
	DiscoveredServiceNotification dsNotification = null;
	
	DiscoveredServiceTracker createDiscoveredServiceTracker() {
		return new DiscoveredServiceTracker() {
			public void serviceChanged(
					DiscoveredServiceNotification notification) {
				if (notification.getType()==DiscoveredServiceNotification.AVAILABLE) {
					System.out.println("serviceAvailable("+notification.getServiceEndpointDescription()+")");
					if (dsNotification == null) dsNotification = notification;
				} else {
					System.out.println("serviceUnavailable("+notification.getServiceEndpointDescription()+")");
					dsNotification = null;
				}
			}
		};
	}
	
	public void testLocalDiscoveredServiceTracker() throws Exception {
		// First set up discovered service tracker
		ServiceRegistration dstReg = getContext().registerService(DiscoveredServiceTracker.class.getName(), createDiscoveredServiceTracker(), null);
		
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		ServiceRegistration svcReg = getContext().registerService(getDefaultServiceClasses(), getDefaultService(), props);

		Thread.sleep(10000);
		
		assertNotNull(dsNotification);
		// unregister service
		
		svcReg.unregister();
		
		Thread.sleep(10000);
		
		assertNull(dsNotification);
		
		dstReg.unregister();
	}

}
