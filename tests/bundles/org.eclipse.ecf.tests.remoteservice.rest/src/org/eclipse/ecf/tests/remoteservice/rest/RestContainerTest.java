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

import java.net.URI;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.rest.IRestCallable;
import org.eclipse.ecf.remoteservice.rest.RestCallable;
import org.eclipse.ecf.remoteservice.rest.client.IRestClientContainerAdapter;
import org.eclipse.ecf.remoteservice.rest.client.RestClientContainer;

public class RestContainerTest extends AbstractRestTestCase {

	protected void tearDown() throws Exception {
		getContainerManager().removeAllContainers();
	}

	public void testCreateContainer() throws Exception {
		IContainer container = createRestContainer(RestConstants.TEST_DE_TARGET);
		assertNotNull(container);
		assertTrue(container instanceof RestClientContainer);
	}

	public void testCreateContainer1() throws Exception {
		IContainer container = createRestContainer(RestConstants.TEST_DE_TARGET);
		assertNotNull(container);
		assertTrue(container instanceof RestClientContainer);
	}

	public void testCreateContainer2() throws Exception {
		ContainerTypeDescription description = getContainerFactory()
				.getDescriptionByName(RestConstants.REST_CONTAINER_TYPE);
		IContainer container = getContainerFactory().createContainer(description,
				new Object[] { new URL(RestConstants.TEST_DE_TARGET) });
		assertNotNull(container);
		assertTrue(container instanceof RestClientContainer);
	}

	public void testCreateContainer3() throws Exception {
		ContainerTypeDescription description = getContainerFactory()
				.getDescriptionByName(RestConstants.REST_CONTAINER_TYPE);
		IContainer container = getContainerFactory().createContainer(description,
				new Object[] { new URI(RestConstants.TEST_DE_TARGET) });
		assertNotNull(container);
		assertTrue(container instanceof RestClientContainer);
	}

	public void testCreateContainer4() throws Exception {
		ID restID = createRestID(RestConstants.TEST_TWITTER_TARGET);
		IContainer container = createRestContainer(restID);
		assertNotNull(container);
	}

	protected IRestClientContainerAdapter getRestClientContainerAdapter(IContainer container) {
		return (IRestClientContainerAdapter) container.getAdapter(IRestClientContainerAdapter.class);
	}
	
	public void testRegisterRestService() throws Exception {
		IContainer container = createRestContainer(RestConstants.TEST_DE_TARGET);
		IRestClientContainerAdapter adapter = getRestClientContainerAdapter(container);
		assertNotNull(adapter);
		Dictionary properties = new Hashtable();
		properties.put("user", "null");
		IRestCallable callable = new RestCallable("methodName","resourcePath",null,IRestCallable.RequestType.GET);
		IRemoteServiceRegistration registration = adapter
				.registerCallable(callable, properties);
		assertNotNull(registration);
	}

	public void testConnectedID() throws Exception {
		IContainer container = createRestContainer(RestConstants.TEST_DE_TARGET);
		ID connectedID = container.getConnectedID();
		assertNull(connectedID);
	}
	public void testConnect() throws Exception {
		IContainer container = createRestContainer(RestConstants.TEST_DE_TARGET);
		ID connectedID = container.getConnectedID();
		ID targetID = createRestID(new URL(
					RestConstants.TEST_TWITTER_TARGET));
		assertNotNull(targetID);
		container.connect(targetID, null);
		connectedID = container.getConnectedID();
		assertEquals(targetID, connectedID);
	}

	public void testGetId() throws Exception {
		ID id = createRestContainer(RestConstants.TEST_DE_TARGET).getID();
		assertNotNull(id);
	}

    public void testGetId1() throws Exception {
		ID id = createRestContainer(RestConstants.TEST_DE_TARGET).getID();
		assertNotNull(id);
		ID id2 = createRestID(RestConstants.TEST_DE_TARGET);
		assertEquals(id, id2);
    }
    
	public void testGetRemoteServiceNamespace() throws Exception {
		IContainer container = createRestContainer(RestConstants.TEST_DE_TARGET);
		IRestClientContainerAdapter adapter = getRestClientContainerAdapter(container);
		assertNotNull(adapter);
		Namespace namespace = adapter.getRemoteServiceNamespace();
		assertNotNull(namespace);
	}

	public void testGetContainerFromManager() throws Exception {
		IContainer container = createRestContainer(RestConstants.TEST_DE_TARGET);
		ID id = container.getID();
		IContainer container2 = getContainerManager().getContainer(id);
		assertNotNull(container2);
	}

	public void testDispose() throws Exception {
		IContainer container = createRestContainer(RestConstants.TEST_DE_TARGET);
		container.dispose();
		assertNull(container.getConnectedID());
	}

}
