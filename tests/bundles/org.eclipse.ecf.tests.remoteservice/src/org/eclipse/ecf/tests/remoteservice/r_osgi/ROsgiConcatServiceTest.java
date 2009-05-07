/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.remoteservice.r_osgi;

import junit.framework.TestCase;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;
import org.eclipse.ecf.tests.remoteservice.IConcatService;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.osgi.framework.InvalidSyntaxException;

public class ROsgiConcatServiceTest extends TestCase {

	IRemoteServiceContainer rsContainer;
	ID targetID;
	
	protected void setUp() throws Exception {
		super.setUp();
		IContainer container = ContainerFactory.getDefault().createContainer(R_OSGi.CLIENT_CONTAINER_NAME,IDFactory.getDefault().createStringID(
				"r-osgi://localhost:9279"));
		rsContainer = new RemoteServiceContainer(container,(IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class));
		targetID = IDFactory.getDefault().createID(container.getConnectNamespace(), R_OSGi.SERVER_IDENTITY);
	}

	protected void tearDown() throws Exception {
		rsContainer.getContainer().disconnect();
		rsContainer.getContainer().dispose();
		((IContainerManager) ContainerFactory.getDefault()).removeAllContainers();
		rsContainer = null;
		targetID = null;
	}
	
	protected String getClientContainerName() {
		return null;
	}
	
	protected IRemoteService getRemoteService(ID target, String clazz, String filter) {
		final IRemoteServiceReference[] refs = getRemoteServiceReferences(target, clazz, filter);
		if (refs == null || refs.length == 0)
			return null;
		return rsContainer.getContainerAdapter().getRemoteService(refs[0]);
	}

	protected IRemoteServiceReference[] getRemoteServiceReferences(ID target, String clazz, String filter) {
		try {
			return rsContainer.getContainerAdapter().getRemoteServiceReferences(target, clazz, filter);
		} catch (final InvalidSyntaxException e) {
			fail("should not happen");
		} catch (final ContainerConnectException e) {
			fail("connect problem");
		}
		return null;
	}


	public void testGetServiceReferences() throws Exception {
		final IRemoteServiceReference[] refs = getRemoteServiceReferences(targetID, IConcatService.class.getName(), null);

		assertTrue(refs != null);
		assertTrue(refs.length > 0);
	}

	public void testGetService() throws Exception {
		final IRemoteService service = getRemoteService(targetID, IConcatService.class.getName(), null);
		assertNotNull(service);
	}

	protected IRemoteCallListener createRemoteCallListener() {
		return new IRemoteCallListener() {
			public void handleEvent(IRemoteCallEvent event) {
				System.out.println("CLIENT.handleEvent(" + event + ")");
			}
		};
	}

	protected IRemoteCall createRemoteCall(final String method,
			final Object[] params) {
		return new IRemoteCall() {
			public String getMethod() {
				return method;
			}

			public Object[] getParameters() {
				return params;
			}

			public long getTimeout() {
				return 3000;
			}
		};
	}

	protected IRemoteCall createRemoteConcat(String first, String second) {
		return createRemoteCall("concat", new Object[] { first, second });
	}


	public void testCallSynch() throws Exception {
		final IRemoteService service = getRemoteService(targetID, IConcatService.class.getName(), null);
		assertNotNull(service);
		traceCallStart("callSynch");
		final Object result = service.callSync(createRemoteConcat("Eclipse ",
				"is cool"));
		traceCallEnd("callSynch", result);

		assertNotNull(result);
		assertTrue(result.equals("Eclipse ".concat("is cool")));
	}

	protected void traceCallStart(String callType) {
		System.out.println(callType + " start");
	}

	protected void traceCallEnd(String callType, Object result) {
		System.out.println(callType + " end");
		System.out.println("  result=" + result);
	}

	protected void traceCallEnd(String callType) {
		System.out.println(callType + " end.");
	}

	public void testCallAsynch() throws Exception {
		final IRemoteService service = getRemoteService(targetID, IConcatService.class.getName(), null);
		assertNotNull(service);
		traceCallStart("callAsynch");
		service.callAsync(createRemoteConcat("ECF ", "is cool"),
				createRemoteCallListener());
		traceCallEnd("callAsynch");
	}

	public void testFireAsynch() throws Exception {
		final IRemoteService service = getRemoteService(targetID, IConcatService.class.getName(), null);
		assertNotNull(service);
		traceCallStart("fireAsynch");
		service.fireAsync(createRemoteConcat("Eclipse ", "sucks"));
		traceCallEnd("fireAsynch");
	}

	public void testProxy() throws Exception {
		final IRemoteService service = getRemoteService(targetID, IConcatService.class.getName(), null);
		assertNotNull(service);
		final IConcatService proxy = (IConcatService) service.getProxy();
		assertNotNull(proxy);
		traceCallStart("getProxy");
		final String result = proxy.concat("ECF ", "sucks");
		traceCallEnd("getProxy", result);
	}

	public void testAsyncResult() throws Exception {
		final IRemoteService service = getRemoteService(targetID, IConcatService.class.getName(), null);
		assertNotNull(service);
		traceCallStart("callAsynchResult");
		final IFuture result = service.callAsync(createRemoteConcat(
				"ECF AsynchResults ", "are cool"));
		traceCallEnd("callAsynchResult", result);
		assertNotNull(result);
	}

}
