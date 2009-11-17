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

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceRegisteredEvent;
import org.eclipse.ecf.remoteservice.rest.IRestCallable;
import org.eclipse.ecf.remoteservice.rest.RestCallable;
import org.eclipse.ecf.remoteservice.rest.client.IRestClientContainerAdapter;
import org.osgi.framework.InvalidSyntaxException;

public class RestRemoteServiceAdapterTest extends AbstractRestTestCase {
	
	IContainer container;
	
	protected void setUp() throws Exception {
		super.setUp();
		container = createRestContainer(RestConstants.TEST_TWITTER_TARGET);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		container.disconnect();
		container.dispose();
		getContainerManager().removeAllContainers();
	}
	
	IRestClientContainerAdapter getRestClientContainerAdapter() {
		return super.getRestClientContainerAdapter(container);
	}
	
	IRemoteServiceRegistration createRestRegistration(String resourcePath) {
		IRestCallable callable = new RestCallable(resourcePath,resourcePath,null,IRestCallable.RequestType.GET);
		return registerCallable(container, callable, null);
	}
	
	public void testGetRemoteService() {
		IRemoteServiceRegistration registration = createRestRegistration("resourcePath");
		IRemoteServiceReference reference = registration.getReference();
		assertNotNull(reference);
		IRemoteService remoteService = getRestClientContainerAdapter().getRemoteService(reference);
		assertNotNull(remoteService);
	}
	
	public void testGetRemoteServiceReference() {
		IRemoteServiceRegistration registration = createRestRegistration("resourcePath");
		IRemoteServiceReference remoteServiceReference = getRestClientContainerAdapter().getRemoteServiceReference(registration.getID());
		assertEquals(registration.getReference(), remoteServiceReference);
	}
	
	public void testUngetRemoteService() {
		IRemoteServiceRegistration registration = createRestRegistration("resourcePath");
		IRemoteServiceReference reference = registration.getReference();
		getRestClientContainerAdapter().getRemoteService(reference);
		assertTrue(getRestClientContainerAdapter().ungetRemoteService(reference));
	}

	public void testRemoteServiceRegisteredEvent() {
		IRestClientContainerAdapter adapter = getRestClientContainerAdapter();
		adapter.addRemoteServiceListener(new IRemoteServiceListener() {
			public void handleServiceEvent(IRemoteServiceEvent event) {
				assertTrue(event instanceof IRemoteServiceRegisteredEvent);
			}
		});
		createRestRegistration("resourcePath");
	}
	
	public void testCreateRemoteFilter() {
		String filter = "(" + Constants.OBJECTCLASS + "=" + IRemoteService.class.getName() + ")";
		try {
			IRemoteFilter remoteFilter = getRestClientContainerAdapter().createRemoteFilter(filter);
			assertNotNull(remoteFilter);
		} catch (InvalidSyntaxException e) {
			fail();
		}		
	}
	
	public void testGetRemoteServiceID() {
		IRemoteServiceRegistration registration = createRestRegistration("resourcePath");
		long containerRelativeID = registration.getID().getContainerRelativeID();
		IRemoteServiceID remoteServiceID = getRestClientContainerAdapter().getRemoteServiceID(container.getID(), containerRelativeID);
		assertEquals(registration.getID(), remoteServiceID);
	}
	
}
