/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.tests.remoteservice.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectingEvent;
import org.eclipse.ecf.core.events.IContainerDisposeEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.rest.RestContainer;
import org.eclipse.ecf.remoteservice.rest.RestContainerInstantiatior;
import org.eclipse.ecf.remoteservice.rest.RestService;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;

public class RestContainerTest extends TestCase {
	
	private RestContainer container;

	protected void setUp() throws Exception {
		container = ( RestContainer ) createRestContainer();
	}
	
	protected void tearDown() throws Exception {
		container.disconnect();
		container.dispose();
		container = null;
	}
	
	public void testContainerCreation() {
		IContainer container = createRestContainer();
		assertNotNull(container);		
	}
	
	public void testContainerCreation2() {
		RestNamespace namespace = new RestNamespace(RestNamespace.NAME, "desc");
		try {
			IContainer container = ContainerFactory.getDefault().createContainer(new RestID(namespace, new URL("http://www.twitter.com")));
			assertNotNull(container);
		} catch (ContainerCreateException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void testRegisterService() {
		IContainer container = createRestContainer();
		assertTrue(container instanceof IRemoteServiceContainerAdapter);
		IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) container;
		String[] clazzes = new String[] {IRemoteService.class.getName()};
		RestService service = createRestService();
		Dictionary properties = new Hashtable();
		properties.put("user", "null");
		IRemoteServiceRegistration registration = adapter.registerRemoteService(clazzes, service, properties);
		assertNotNull(registration);
	}
	
	public void testConnect() {
		RestNamespace namespace = new RestNamespace(RestNamespace.NAME, null);
		ID id;
		try {
			id = namespace.createInstance(new Object[]{new URL("http://test.de")});
			assertNotNull(id);
			container.connect(id, null);
			Namespace connectNamespace = container.getConnectNamespace();
			assertEquals(namespace, connectNamespace);
		} catch (IDCreateException e) {
			fail();
		} catch (MalformedURLException e) {
			fail();
		} catch (ContainerConnectException e) {
			fail();
		}		
	}
	
	public void testGetId() {
		ID id = container.getID();
		assertNotNull(id);
		Namespace namespace = new RestNamespace(RestNamespace.NAME, null);
		try {		
			ContainerTypeDescription desc = new ContainerTypeDescription(RestContainer.NAME, new RestContainerInstantiatior(), null);
			id = new RestID(namespace, new URL("http://test.de"));
			container = (RestContainer) ContainerFactory.getDefault().createContainer(desc, id);
			ID id2 = container.getID();
			assertEquals(id, id2);
		} catch (ContainerCreateException e) {
			fail();
		} catch (MalformedURLException e) {
			fail();
		}
	}
	
	public void testGetRemoteServiceNamespace() {
		Namespace namespace = new RestNamespace(RestNamespace.NAME, null);
		try {		
			ContainerTypeDescription desc = new ContainerTypeDescription(RestContainer.NAME, new RestContainerInstantiatior(), null);
			ID id = new RestID(namespace, new URL("http://test.de"));
			container = (RestContainer) ContainerFactory.getDefault().createContainer(desc, id);
			Namespace remoteServiceNamespace = container.getRemoteServiceNamespace();
			assertEquals(namespace, remoteServiceNamespace);
		} catch (ContainerCreateException e) {
			fail();
		} catch (MalformedURLException e) {
			fail();
		}
	}
	
	public void testGetContainer() {
		IContainer container2 = container.getContainer();
		assertEquals(container, container2);
	}
	
	public void testGetContainerAdapter() {
		IRemoteServiceContainerAdapter containerAdapter = container.getContainerAdapter();
		assertEquals(container, containerAdapter);
	}
	
	public void testDispose() {
		Namespace namespace = new RestNamespace(RestNamespace.NAME, null);
		try {
			RestID id = new RestID(namespace, new URL("http://test.de"));
			container.connect(id, null);
			assertEquals(id, container.getConnectedID());
			container.addListener(new IContainerListener() {				
				public void handleEvent(IContainerEvent event) {
					assertTrue(event instanceof IContainerDisposeEvent
							|| event instanceof IContainerDisconnectingEvent
							|| event instanceof IContainerDisconnectedEvent);
				}
			});
			container.dispose();
			assertNull(container.getConnectedID());
		} catch (ContainerConnectException e) {
			fail();
		} catch (MalformedURLException e) {
			fail();
		}
	}
	


	private RestService createRestService() {
		return new RestService();
	}

	static IContainer createRestContainer() {
		IContainer container = null;
		try {
			Namespace namespace = new RestNamespace(RestNamespace.NAME, null);
			ID id = namespace.createInstance(new Object[]{new URL("http://www.test.de")});;
			container = ContainerFactory.getDefault().createContainer(RestContainer.NAME, id);
		} catch (ContainerCreateException e) {
			e.printStackTrace();
		} catch (IDCreateException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return container;
	}

}
