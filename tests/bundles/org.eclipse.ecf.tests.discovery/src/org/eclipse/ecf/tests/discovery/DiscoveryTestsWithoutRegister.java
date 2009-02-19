/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.discovery;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;


public abstract class DiscoveryTestsWithoutRegister extends AbstractDiscoveryTest {
	
	public DiscoveryTestsWithoutRegister(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		container.disconnect();
		container.dispose();
	}

	public void testConnect() {
		assertNull(container.getConnectedID());
		try {
			container.connect(null, null);
		} catch (final ContainerConnectException e) {
			fail("connect may not fail the first time");
		}
		assertNotNull(container.getConnectedID());
	}

	public void testConnectTwoTimes() {
		testConnect();
		try {
			container.connect(null, null);
		} catch (final ContainerConnectException e) {
			return;
		}
		fail("succeeding connects should fail");
	}

	public void testDisconnect() {
		testConnect();
		container.disconnect();
		assertNull(container.getConnectedID());
	}

	public void testReconnect() {
		testDisconnect();
		testConnect();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID)}.
	 */
	public void testGetServiceInfoWithNull() {
		try {
			discoveryContainer.getServiceInfo(null);
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null argument is not allowed in api");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)}.
	 */
	public void testGetServicesIServiceTypeIDWithNull() {
		try {
			discoveryContainer.getServices(null);
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null argument is not allowed in api");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)}.
	 */
	public void testUnregisterServiceWithNull() {
		testConnect();
		try {
			discoveryContainer.unregisterService(null);
		} catch (final ECFException e) {
			fail("null must cause AssertionFailedException");
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null must cause AssertionFailedException");
	}

	public void testDispose() {
		testConnect();
		container.dispose();
		assertNull(container.getConnectedID());
		try {
			container.connect(null, null);
		} catch (final ContainerConnectException e) {
			return;
		}
		fail("A disposed container must not be reusable");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceTypeListener(org.eclipse.ecf.discovery.IServiceTypeListener)}.
	 */
	public void testAddServiceTypeListenerWithNull() {
		try {
			discoveryContainer.addServiceTypeListener(null);
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null argument is not allowed in api");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.core.IContainer#getConnectNamespace()}.
	 */
	public void testGetConnectNamespace() {
		testConnect();
		assertNotNull(container.getConnectNamespace());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.core.IContainer#getID()}.
	 */
	public void testGetID() {
		testConnect();
		assertNotNull(container.getID());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServicesNamespace()}.
	 */
	public void testGetServicesNamespace() {
		testConnect();
		final Namespace namespace = discoveryContainer.getServicesNamespace();
		assertNotNull(namespace);
		final IServiceID serviceID = ServiceIDFactory.getDefault().createServiceID(namespace, serviceInfo.getServiceID().getServiceTypeID());
		assertNotNull("It must be possible to obtain a IServiceID", serviceID);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceTypeListener(org.eclipse.ecf.discovery.IServiceTypeListener)}.
	 */
	public void testRemoveServiceTypeListenerWithNull() {
		try {
			discoveryContainer.removeServiceTypeListener(null);
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null argument is not allowed in api");
	}


	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerService(org.eclipse.ecf.discovery.IServiceInfo)}.
	 */
	public void testRegisterServiceWithNull() {
		testConnect();
		try {
			discoveryContainer.registerService(null);
		} catch (final ECFException e) {
			fail("null must cause AssertionFailedException");
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null must cause AssertionFailedException");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceListener(org.eclipse.ecf.discovery.IServiceListener)}.
	 */
	public void testAddServiceListenerIServiceListenerWithNull() {
		try {
			discoveryContainer.addServiceListener(null);
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null argument is not allowed in api");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceListener(org.eclipse.ecf.discovery.identity.IServiceTypeID, org.eclipse.ecf.discovery.IServiceListener)}.
	 */
	public void testAddServiceListenerIServiceTypeIDIServiceListenerWithNull() {
		try {
			discoveryContainer.addServiceListener(null, null);
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null argument is not allowed in api");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceListener(org.eclipse.ecf.discovery.IServiceListener)}.
	 */
	public void testRemoveServiceListenerIServiceListenerWithNull() {
		try {
			discoveryContainer.removeServiceListener(null);
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null argument is not allowed in api");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceListener(org.eclipse.ecf.discovery.identity.IServiceTypeID, org.eclipse.ecf.discovery.IServiceListener)}.
	 */
	public void testRemoveServiceListenerIServiceTypeIDIServiceListenerWithNull() {
		try {
			discoveryContainer.removeServiceListener(null, null);
		} catch (final AssertionFailedException e) {
			return;
		}
		fail("null argument is not allowed in api");
	}

}
