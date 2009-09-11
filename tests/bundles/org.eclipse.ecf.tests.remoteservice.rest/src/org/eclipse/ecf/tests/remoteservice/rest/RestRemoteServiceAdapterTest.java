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

import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceRegisteredEvent;
import org.eclipse.ecf.remoteservice.rest.RestContainer;
import org.eclipse.ecf.remoteservice.rest.RestService;
import org.eclipse.ecf.remoteservice.rest.RestServiceRegistration;
import org.osgi.framework.InvalidSyntaxException;

public class RestRemoteServiceAdapterTest extends TestCase {
	
	private IRemoteServiceRegistration registration;
	private RestContainer container;

	protected void setUp() throws Exception {
		registration = getServiceRegistration();
	}
	
	protected void tearDown() throws Exception {
		registration = null;
	}
	
	public void testGetRemoteService() {
		IRemoteServiceReference reference = registration.getReference();
		IRemoteService remoteService = ((IRemoteServiceContainerAdapter)container).getRemoteService(reference);
		RestServiceRegistration reg = (RestServiceRegistration)registration;
		assertEquals(reg.getService(), remoteService);
	}
	
	public void testGetRemoteServiceReference() {
		IRemoteServiceReference remoteServiceReference = container.getRemoteServiceReference(registration.getID());
		assertEquals(registration.getReference(), remoteServiceReference);
	}
	
	public void testUngetRemoteService() {
		container.getRemoteService(registration.getReference());
		assertTrue(container.ungetRemoteService(registration.getReference()));
	}
	
	public void testRemoteServiceRegisteredEvent() {
		container.addRemoteServiceListener(new IRemoteServiceListener() {
			
			public void handleServiceEvent(IRemoteServiceEvent event) {
				assertTrue(event instanceof IRemoteServiceRegisteredEvent);
			}
		});
		String[] clazzes = new String[] {IRemoteService.class.getName()};
		RestService service = new RestService();
		Dictionary properties = new Hashtable();
		properties.put("user", "null");
		container.registerRemoteService(clazzes, service, properties);	
		
	}
	
	public void testCreateRemoteFilter() {
		String filter = "(" + Constants.OBJECTCLASS + "=" + IRemoteService.class.getName() + ")";
		try {
			IRemoteFilter remoteFilter = container.createRemoteFilter(filter);
			assertNotNull(remoteFilter);
		} catch (InvalidSyntaxException e) {
			fail();
		}		
	}
	
	public void testGetRemoteServiceID() {
		long containerRelativeID = registration.getID().getContainerRelativeID();
		IRemoteServiceID remoteServiceID = container.getRemoteServiceID(container.getID(), containerRelativeID);
		assertEquals(registration.getID(), remoteServiceID);
	}
	
	
	

	private IRemoteServiceRegistration getServiceRegistration() {
		container = (RestContainer)RestContainerTest.createRestContainer();
		IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) container;
		String[] clazzes = new String[] {IRemoteService.class.getName()};
		RestService service = new RestService();
		Dictionary properties = new Hashtable();
		properties.put("user", "null");
		return adapter.registerRemoteService(clazzes, service, properties);		
	}

}
