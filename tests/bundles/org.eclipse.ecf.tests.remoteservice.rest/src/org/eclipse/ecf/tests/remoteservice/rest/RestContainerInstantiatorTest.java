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

import junit.framework.TestCase;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.rest.RestContainer;
import org.eclipse.ecf.remoteservice.rest.RestContainerInstantiatior;

public class RestContainerInstantiatorTest extends TestCase {
	private RestContainerInstantiatior instantiator;
	private ContainerTypeDescription description;

	protected void setUp() throws Exception {
		instantiator = new RestContainerInstantiatior();
		description = new ContainerTypeDescription(RestContainer.NAME, instantiator, null);
	}
	
	public void testSupportedParameterTypes() {						
		Class[][] types = instantiator.getSupportedParameterTypes(description);
		assertEquals(types.length, 1);
		assertEquals(types[0].length, 1);
		Class supportedType = types[0][0];
		assertEquals(URL.class, supportedType);
	}
	
	public void testCreateInstance() {
		try {
			IContainer container = instantiator.createInstance(description, new Object[]{new URL("http://test.de")});
			assertTrue(container instanceof RestContainer);
			ID connectedID = container.getConnectedID();
			assertNull(connectedID);
			URL baseUrl = new URL("http://www.twitter.com");
			container = instantiator.createInstance(description, new Object[]{baseUrl});
			assertTrue(container instanceof RestContainer);
		} catch (ContainerCreateException e) {
			fail();
		} catch (MalformedURLException e) {
			fail();
		}
	}

}
