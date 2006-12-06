/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.connect;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerConnectingEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectingEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public class ClientContainerConnectTest extends ContainerAbstractTestCase {

	List serverConnectEvents = new ArrayList();

	List serverDisconnectEvents = new ArrayList();

	List clientConnectingEvents = new ArrayList();

	List clientConnectedEvents = new ArrayList();

	List clientDisconnectingEvents = new ArrayList();

	List clientDisconnectedEvents = new ArrayList();


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.connect.ContainerConnectTestCase#createServerAndClients()
	 */
	protected void createServerAndClients() throws Exception {
		clientCount = 5;
		super.createServerAndClients();
		getServer().addListener(new IContainerListener() {
			public void handleEvent(IContainerEvent event) {
				if (event instanceof IContainerConnectedEvent)
					serverConnectEvents.add(event);
				if (event instanceof IContainerDisconnectedEvent)
					serverDisconnectEvents.add(event);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		createServerAndClients();
	}

	protected void clearClientEvents() {
		clientConnectingEvents.clear();
		clientConnectedEvents.clear();
		clientDisconnectingEvents.clear();
		clientDisconnectedEvents.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		cleanUpServerAndClients();
		super.tearDown();
		serverConnectEvents.clear();
		serverDisconnectEvents.clear();
		clearClientEvents();
	}

	public void testConnectClients() throws Exception {
		connectClients();
		assertTrue(serverConnectEvents.size() == getClientCount());
	}

	public void testClientsDisconnect() throws Exception {
		connectClients();
		assertTrue(serverConnectEvents.size() == getClientCount());
		disconnectClients();
		assertTrue(serverDisconnectEvents.size() == getClientCount());
	}

	public void testGetConnectedID() throws Exception {
		IContainer client = getClients()[0];
		assertNull(client.getConnectedID());
		client.connect(getServerID(), null);
		assertNotNull(client.getConnectedID());
		client.disconnect();
		assertNull(client.getConnectedID());
	}

	public void testClientDispose() throws Exception {
		IContainer client = getClients()[0];
		assertNull(client.getConnectedID());
		client.connect(getServerID(), null);
		assertNotNull(client.getConnectedID());
		client.dispose();
		assertNull(client.getConnectedID());
	}

	protected IContainerListener createListener() {
		return new IContainerListener() {

			public void handleEvent(IContainerEvent event) {
				if (event instanceof IContainerConnectingEvent)
					clientConnectingEvents.add(event);
				if (event instanceof IContainerConnectedEvent)
					clientConnectedEvents.add(event);
				if (event instanceof IContainerDisconnectingEvent)
					clientDisconnectingEvents.add(event);
				if (event instanceof IContainerDisconnectedEvent)
					clientDisconnectedEvents.add(event);
			}

		};
	}

	public void testClientListener() throws Exception {
		IContainer client = getClients()[0];
		client.addListener(createListener());
		assertTrue(clientConnectingEvents.size() == 0);
		assertTrue(clientConnectedEvents.size() == 0);
		client.connect(getServerID(), null);
		assertTrue(clientConnectingEvents.size() == 1);
		assertTrue(clientConnectedEvents.size() == 1);
		assertTrue(clientDisconnectingEvents.size() == 0);
		assertTrue(clientDisconnectedEvents.size() == 0);
		client.disconnect();
		assertTrue(clientDisconnectingEvents.size() == 1);
		assertTrue(clientDisconnectedEvents.size() == 1);
	}

	public void testListenerConnecting() throws Exception {
		IContainer client = getClients()[0];
		client.addListener(createListener());
		client.connect(getServerID(), null);
		Object o = clientConnectingEvents.get(0);
		assertTrue(o instanceof IContainerConnectingEvent);
		IContainerConnectingEvent cco = (IContainerConnectingEvent) o;
		assertTrue(cco.getLocalContainerID().equals(client.getID()));
		assertTrue(cco.getTargetID().equals(getServerID()));
		assertTrue(cco.getData() == null);
	}

	public void testListenerConnected() throws Exception {
		IContainer client = getClients()[0];
		client.addListener(createListener());
		client.connect(getServerID(), null);
		Object o = clientConnectedEvents.get(0);
		assertTrue(o instanceof IContainerConnectedEvent);
		IContainerConnectedEvent cco = (IContainerConnectedEvent) o;
		assertTrue(cco.getLocalContainerID().equals(client.getID()));
		assertTrue(cco.getTargetID().equals(getServerID()));
	}

	public void testListenerDisconnecting() throws Exception {
		IContainer client = getClients()[0];
		client.addListener(createListener());
		client.connect(getServerID(), null);
		client.disconnect();
		Object o = clientDisconnectingEvents.get(0);
		assertTrue(o instanceof IContainerDisconnectingEvent);
		IContainerDisconnectingEvent cco = (IContainerDisconnectingEvent) o;
		assertTrue(cco.getLocalContainerID().equals(client.getID()));
		assertTrue(cco.getTargetID().equals(getServerID()));
	}

	public void testListenerDisconnected() throws Exception {
		IContainer client = getClients()[0];
		client.addListener(createListener());
		client.connect(getServerID(), null);
		client.disconnect();
		Object o = clientDisconnectedEvents.get(0);
		assertTrue(o instanceof IContainerDisconnectedEvent);
		IContainerDisconnectedEvent cco = (IContainerDisconnectedEvent) o;
		assertTrue(cco.getLocalContainerID().equals(client.getID()));
		assertTrue(cco.getTargetID().equals(getServerID()));
	}
	
	public void testRemoveListener() throws Exception {
		IContainer client = getClients()[0];
		IContainerListener l = createListener();
		client.addListener(l);
		client.removeListener(l);
		client.connect(getServerID(), null);
		assertTrue(clientConnectingEvents.size() == 0);
	}

}
