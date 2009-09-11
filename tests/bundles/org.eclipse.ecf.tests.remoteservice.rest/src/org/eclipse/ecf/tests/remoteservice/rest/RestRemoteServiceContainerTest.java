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

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.rest.RestService;
import org.eclipse.ecf.remoteservice.rest.RestServiceRegistration;

public class RestRemoteServiceContainerTest extends TestCase {
	
	private IRemoteServiceContainer container;
	
	protected void setUp() throws Exception {
		if(container == null) {
			createRemoteServiceContainer();
		}
	}
	
	public void testCreation(){
		assertNotNull(container);
	}
	
	public void testGetRemoteServiceContainerAdapter(){
		IRemoteServiceContainerAdapter containerAdapter = container.getContainerAdapter();
		assertEquals(container, containerAdapter);
	}
	
	public void testGetContainer(){
		IContainer iContainer = container.getContainer();
		assertEquals(container, iContainer);
	}
	
	public void testGetRemoteService(){
		IRemoteService remoteService = container.getRemoteService(IRemoteService.class.getName());
		assertNull(remoteService);
		
		IRemoteServiceContainerAdapter adapter = container.getContainerAdapter();
		String[] clazzes = new String[] {IRemoteService.class.getName()};
		RestService service = new RestService();
		Dictionary properties = new Hashtable();
		properties.put("user", "null");
		IRemoteServiceRegistration registration = adapter.registerRemoteService(clazzes, service, properties);	
		assertNotNull(registration);
		
		remoteService = container.getRemoteService(IRemoteService.class.getName());
		assertNotNull(remoteService);
		assertTrue(registration instanceof RestServiceRegistration);
		RestServiceRegistration restRegistration = (RestServiceRegistration)registration;
		assertEquals(restRegistration.getService(), remoteService);
	}
	
	public void testGetRemoteServiceWithIDFilter() {
		IRemoteService remoteService = container.getRemoteService(IRemoteService.class.getName());
		assertNull(remoteService);
		
		IRemoteServiceContainerAdapter adapter = container.getContainerAdapter();
		String[] clazzes = new String[] {IRemoteService.class.getName()};
		RestService service = new RestService();
		Dictionary properties = new Hashtable();
		properties.put("user", "null");
		IRemoteServiceRegistration registration = adapter.registerRemoteService(clazzes, service, properties);	
		assertNotNull(registration);
		
		try {
			remoteService = container.getRemoteService(null, IRemoteService.class.getName());
		} catch (ContainerConnectException e) {
			e.printStackTrace();
		}
		assertNotNull(remoteService);
		assertTrue(registration instanceof RestServiceRegistration);
		RestServiceRegistration restRegistration = (RestServiceRegistration)registration;
		assertEquals(restRegistration.getService(), remoteService);

	}

	private void createRemoteServiceContainer() {
		IContainer restContainer = RestContainerTest.createRestContainer();
		if(restContainer instanceof IRemoteServiceContainer)
			container = (IRemoteServiceContainer)restContainer;
	}
	
	

}
