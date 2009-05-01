/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.discovery;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.osgi.services.discovery.IHostDiscoveryListener;
import org.eclipse.ecf.osgi.services.discovery.IProxyDiscoveryListener;
import org.eclipse.ecf.tests.internal.osgi.discovery.Activator;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ListenerTest extends PublishTest {

	ServiceRegistration hostDiscoveryListenerRegistration;
	ServiceRegistration proxyDiscoveryListenerRegistration;
	
	protected IHostDiscoveryListener createHostDiscoveryListener() {
		return new IHostDiscoveryListener() {

			public void publish(ServiceReference publicationServiceReference,
					IServiceInfo serviceInfo) {
				System.out.println("hostPublish\n\tpublicationServiceReference="+publicationServiceReference+"\n\tserviceInfo="+serviceInfo);
			}

			public void unpublish(ServiceReference publicationServiceReference,
					IServiceInfo serviceInfo) {
				System.out.println("hostUnpublish\n\tpublicationServiceReference="+publicationServiceReference+"\n\tserviceInfo="+serviceInfo);
			}
			
		};
	}

	protected IProxyDiscoveryListener createProxyDiscoveryListener() {
		return new IProxyDiscoveryListener() {

			public void discovered(IServiceInfo serviceInfo) {
				System.out.println("proxyDiscovered serviceInfo="+serviceInfo);
			}

			public void undiscovered(IServiceInfo serviceInfo) {
				System.out.println("proxyUndiscovered serviceInfo="+serviceInfo);
			}
			
		};
	}

	protected void setUp() throws Exception {
		// Register listeners
		hostDiscoveryListenerRegistration = Activator.getDefault().getContext().registerService(IHostDiscoveryListener.class.getName(), createHostDiscoveryListener(), null);
		proxyDiscoveryListenerRegistration = Activator.getDefault().getContext().registerService(IProxyDiscoveryListener.class.getName(), createProxyDiscoveryListener(), null);
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		hostDiscoveryListenerRegistration.unregister();
		proxyDiscoveryListenerRegistration.unregister();
	}

}
