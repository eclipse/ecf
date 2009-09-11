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
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;
import org.eclipse.ecf.remoteservice.rest.IRestCall;
import org.eclipse.ecf.remoteservice.rest.RestService;
import org.eclipse.ecf.remoteservice.rest.util.RestCallFactory;
import org.eclipse.ecf.tests.remoteservice.rest.service.SimpleRestService;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.w3c.dom.Document;

public class RestRemoteServiceTest extends TestCase {

	private SimpleRestService service;

	protected void setUp() throws Exception {
		if (service == null) {
			service = new SimpleRestService();
		}
	}

	protected void tearDown() throws Exception {
		service.shutdown();
	}

	public void testServiceCreation() {
		RestService restService = new RestService();
		assertNotNull(restService);
	}

	public void testSyncCall() {
		RestService restService = new RestService();
		try {
			Object result = restService.callSync(getRestXMLCall());
			assertNotNull(result);
		} catch (ECFException e) {
			fail("Could not contact the service");
		}
	}

	public void testAsynCall() {
		RestService restService = new RestService();
		IFuture future = restService.callAsync(getRestXMLCall());
		try {
			Object response = future.get();
			assertTrue(response instanceof Document);
		} catch (OperationCanceledException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}

	public void testAsyncCallWithListener() {
		RestService restService = new RestService();
		restService.callAsync(getRestXMLCall(), new IRemoteCallListener() {
			public void handleEvent(IRemoteCallEvent event) {
				if (event instanceof IRemoteCallCompleteEvent) {
					// TODO: test async
					// assertEquals(SimpleRestService.XML_RESPONSE,
					// completeEvent.getResponse());
				}
			}
		});

	}

	private IRestCall getRestXMLCall() {
		try {
			return RestCallFactory.createRestCall(IRestCall.HTTP_GET, new URI(
					service.getServerUrl() + "/test.xml"),
					"ecf.rest.resource.xml", null, 10000);
		} catch (URISyntaxException e) {
			fail();
		}
		return null;
	}

}
