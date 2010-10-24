/****************************************************************************
 * Copyright (c) 2010 Eugen Reiswich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugen Reiswich - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.xmpp.remoteservice;

import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.xmpp.identity.XMPPID;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.tests.provider.xmpp.XMPPS;
import org.osgi.framework.InvalidSyntaxException;

public class RemoteServiceRetrieval extends TestCase {

	private XMPPClient[] xmppClients;

	public void setUp() {
		try {
			createXMPPClients();
		} catch (ContainerConnectException e) {
			e.printStackTrace();
			fail();
		} catch (ContainerCreateException e) {
			e.printStackTrace();
			fail();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail();
		}
		registerRemoteServicesNoFilterIDs();
	}

	private XMPPClient getClient(int clientNr) {
		assertTrue(0 <= clientNr);
		assertTrue(clientNr <= 2);
		return xmppClients[clientNr];
	}

	/*
	 * Make sure that the right service is returned for the right client.
	 */
	public void testRightServiceForClients() throws InvalidSyntaxException,
			ECFException {

		// Client 1 tries to retrieve services registered by client 2 & 3
		IExampleService remoteService1 = getClient(0).getRemoteService(
				getClient(1));
		assertNotNull(remoteService1);
		assertEquals(getClient(1).getClientID(), remoteService1.getClientID());

		IExampleService remoteService2 = getClient(0).getRemoteService(
				getClient(2));
		assertNotNull(remoteService2);
		assertEquals(getClient(2).getClientID(), remoteService2.getClientID());

		// Client 2 tries to retrieve services registered by client 1 & 3
		IExampleService remoteService3 = getClient(1).getRemoteService(
				getClient(0));
		assertNotNull(remoteService3);
		assertEquals(getClient(0).getClientID(), remoteService3.getClientID());

		IExampleService remoteService4 = getClient(1).getRemoteService(
				getClient(2));
		assertNotNull(remoteService4);
		assertEquals(getClient(2).getClientID(), remoteService4.getClientID());

		// Client 3 tries to retrieve services registered by client 1 & 2
		IExampleService remoteService5 = getClient(2).getRemoteService(
				getClient(0));
		assertNotNull(remoteService5);
		assertEquals(getClient(0).getClientID(), remoteService5.getClientID());

		IExampleService remoteService6 = getClient(2).getRemoteService(
				getClient(1));
		assertNotNull(remoteService6);
		assertEquals(getClient(1).getClientID(), remoteService6.getClientID());
	}

	public void testConnectAndDisconnectClients() throws ECFException,
			InvalidSyntaxException {
		// Client 0 tries to get service from client 1
		IExampleService remoteService1 = getClient(0).getRemoteService(
				getClient(1));
		assertNotNull(remoteService1);

		// disconnect client 1, no services available for client 1
		getClient(1).disconnect();
		IExampleService remoteService2 = getClient(0).getRemoteService(
				getClient(1));
		assertNull(remoteService2);

		// connect client 1 again, services are available again
		getClient(1).connect();
		IExampleService remoteService3 = getClient(0).getRemoteService(
				getClient(1));
		assertNotNull(remoteService3);
	}

	/*
	 * Remote service registration without filterIDs.
	 */
	private void registerRemoteServicesNoFilterIDs() {
		IExampleService clientService1 = new ExampleService(getClient(0)
				.getClientID());
		IExampleService clientService2 = new ExampleService(getClient(1)
				.getClientID());
		IExampleService clientService3 = new ExampleService(getClient(2)
				.getClientID());

		getClient(0).getRemoteServiceAdapter().registerRemoteService(
				new String[] { IExampleService.class.getName() },
				clientService1, null);
		getClient(1).getRemoteServiceAdapter().registerRemoteService(
				new String[] { IExampleService.class.getName() },
				clientService2, null);
		getClient(2).getRemoteServiceAdapter().registerRemoteService(
				new String[] { IExampleService.class.getName() },
				clientService3, null);
	}

	public void tearDown() {
		for (int clientNumber = 0; clientNumber <= 2; clientNumber++) {
			getClient(clientNumber).disconnect();
		}
	}

	protected void createXMPPClients() throws URISyntaxException,
			ContainerConnectException, ContainerCreateException {
		xmppClients = new XMPPClient[3];
		for (int clientNr = 0; clientNr <= 2; clientNr++) {
			// usernames already contain server address e.g.
			// ecf-test1@ecf-project.org
			String username = System.getProperty("username" + clientNr);
			String password = System.getProperty("password" + clientNr);
			assertNotNull(username);
			assertNotNull(password);

			XMPPClient xmppClient = new XMPPClient(username, password);
			xmppClient.connect();
			xmppClients[clientNr] = xmppClient;
		}
		assertEquals(3, xmppClients.length);
	}

	/*
	 * Convenience class for Client data
	 */
	private class XMPPClient {

		private IContainer container;
		private XMPPID clientID;
		private IConnectContext connectContext;
		private IRemoteServiceContainerAdapter adapter;

		public XMPPClient(String username, String password)
				throws ContainerCreateException, URISyntaxException {
			container = ContainerFactory.getDefault().createContainer(
					XMPPS.CONTAINER_NAME);
			assertNotNull(container);

			adapter = (IRemoteServiceContainerAdapter) container
					.getAdapter(IRemoteServiceContainerAdapter.class);
			assertNotNull(adapter);

			clientID = new XMPPID(container.getConnectNamespace(), username);
			assertNotNull(clientID);

			connectContext = ConnectContextFactory
					.createUsernamePasswordConnectContext(username, password);
			assertNotNull(connectContext);
		}

		IRemoteServiceContainerAdapter getRemoteServiceAdapter() {
			return adapter;
		}

		XMPPID getClientID() {
			return clientID;
		}

		void connect() throws ContainerConnectException {
			container.connect(clientID, connectContext);
		}

		void disconnect() {
			container.disconnect();
		}

		private IExampleService getRemoteService(XMPPClient toClient)
				throws InvalidSyntaxException, ECFException {
			IRemoteServiceReference[] remoteServiceReferences = adapter
					.getRemoteServiceReferences(toClient.getClientID(),
							IExampleService.class.getName(), null);
			assertEquals(1, remoteServiceReferences.length);

			IRemoteService remoteService = adapter
					.getRemoteService(remoteServiceReferences[0]);
			IExampleService remoteServiceUser = (IExampleService) remoteService
					.getProxy();
			return remoteServiceUser;
		}
	}
}