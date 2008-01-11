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

package org.eclipse.ecf.tests.remoteservice;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.IAsyncResult;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;
import org.osgi.framework.InvalidSyntaxException;

/**
 * 
 */
public abstract class AbstractRemoteServiceTest extends ContainerAbstractTestCase {

	protected IRemoteServiceContainerAdapter[] adapters = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.ContainerAbstractTestCase#getClientContainerName()
	 */
	protected abstract String getClientContainerName();

	protected void setClientCount(int count) {
		super.setClientCount(count);
		adapters = new IRemoteServiceContainerAdapter[count];
	}

	protected void setupRemoteServiceAdapters() throws Exception {
		final int clientCount = getClientCount();
		for (int i = 0; i < clientCount; i++) {
			adapters[i] = (IRemoteServiceContainerAdapter) getClients()[i].getAdapter(IRemoteServiceContainerAdapter.class);
		}
	}

	protected IRemoteServiceContainerAdapter[] getRemoteServiceAdapters() {
		return adapters;
	}

	protected IRemoteServiceListener createRemoteServiceListener() {
		return new IRemoteServiceListener() {
			public void handleServiceEvent(IRemoteServiceEvent event) {
				System.out.println("handleServiceEvent(" + event + ")");
			}
		};
	}

	protected void addRemoteServiceListeners() {
		for (int i = 0; i < adapters.length; i++) {
			adapters[i].addRemoteServiceListener(createRemoteServiceListener());
		}
	}

	protected IRemoteServiceRegistration registerService(IRemoteServiceContainerAdapter adapter, String serviceInterface, Object service, int sleepTime) {
		final IRemoteServiceRegistration result = adapter.registerRemoteService(new String[] {serviceInterface}, service, null);
		sleep(sleepTime);
		return result;
	}

	protected IRemoteServiceReference[] getRemoteServiceReferences(IRemoteServiceContainerAdapter adapter, String clazz) {
		try {
			return adapter.getRemoteServiceReferences(null, clazz, null);
		} catch (final InvalidSyntaxException e) {
			fail("should not happen");
		}
		return null;
	}

	protected IRemoteService getRemoteService(IRemoteServiceContainerAdapter adapter, String clazz) {
		final IRemoteServiceReference[] refs = getRemoteServiceReferences(adapter, clazz);
		if (refs.length == 0)
			return null;
		return adapter.getRemoteService(refs[0]);
	}

	protected IRemoteService registerAndGetRemoteService(IRemoteServiceContainerAdapter server, IRemoteServiceContainerAdapter client, String serviceName, int sleepTime) {
		registerService(server, serviceName, createService(), sleepTime);
		return getRemoteService(client, serviceName);
	}

	protected IRemoteCall createRemoteCall(final String method, final Object[] params) {
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

	protected Object createService() {
		return new IConcatService() {
			public String concat(String string1, String string2) {
				final String result = string1.concat(string2);
				System.out.println("SERVICE.concat(" + string1 + "," + string2 + ") returning " + result);
				return string1.concat(string2);
			}
		};
	}

	public void testRemoteServiceAdapters() throws Exception {
		final IRemoteServiceContainerAdapter[] adapters = getRemoteServiceAdapters();
		assertNotNull(adapters);
		for (int i = 0; i < adapters.length; i++)
			assertNotNull(adapters[i]);
	}

	public void testRegisterService() throws Exception {
		final IRemoteServiceContainerAdapter[] adapters = getRemoteServiceAdapters();
		// adapter [0] is the service 'server'
		final IRemoteServiceRegistration reg = registerService(adapters[0], IConcatService.class.getName(), createService(), 1500);
		assertNotNull(reg);
		assertNotNull(reg.getContainerID());
	}

	public void testUnregisterService() throws Exception {
		final IRemoteServiceContainerAdapter[] adapters = getRemoteServiceAdapters();
		// adapter [0] is the service 'server'
		final IRemoteServiceRegistration reg = registerService(adapters[0], IConcatService.class.getName(), createService(), 1500);
		assertNotNull(reg);
		assertNotNull(reg.getContainerID());

		reg.unregister();

	}

	public void testGetServiceReferences() throws Exception {
		final IRemoteServiceContainerAdapter[] adapters = getRemoteServiceAdapters();
		registerService(adapters[0], IConcatService.class.getName(), createService(), 3000);

		final IRemoteServiceReference[] refs = getRemoteServiceReferences(adapters[1], IConcatService.class.getName());

		assertNotNull(refs);
		assertTrue(refs.length > 0);
	}

	public void testGetService() throws Exception {
		final IRemoteService service = registerAndGetRemoteService();

		assertNotNull(service);
	}

	protected IRemoteCall createRemoteConcat(String first, String second) {
		return createRemoteCall("concat", new Object[] {first, second});
	}

	protected IRemoteService registerAndGetRemoteService() {
		final IRemoteServiceContainerAdapter[] adapters = getRemoteServiceAdapters();
		return registerAndGetRemoteService(adapters[0], adapters[1], IConcatService.class.getName(), 1500);

	}

	protected IRemoteCallListener createRemoteCallListener() {
		return new IRemoteCallListener() {
			public void handleEvent(IRemoteCallEvent event) {
				System.out.println("CLIENT.handleEvent(" + event + ")");
			}
		};
	}

	public void testCallSynch() throws Exception {
		final IRemoteService service = registerAndGetRemoteService();

		traceCallStart("callSynch");
		final Object result = service.callSynch(createRemoteConcat("Eclipse ", "is cool"));
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

	public void testBadCallSynch() throws Exception {
		final IRemoteService service = registerAndGetRemoteService();

		// Following should throw exception because "concat1" method does not exist
		try {
			service.callSynch(createRemoteCall("concat1", new Object[] {"first", "second"}));
			fail();
		} catch (final ECFException e) {
			// Exception should occur
		}

		// Following should throw exception because wrong number of params for concat	
		try {
			service.callSynch(createRemoteCall("concat", new Object[] {"first"}));
			fail();
		} catch (final ECFException e) {
			// Exception should occur
		}

	}

	public void testCallAsynch() throws Exception {
		final IRemoteService service = registerAndGetRemoteService();

		traceCallStart("callAsynch");
		service.callAsynch(createRemoteConcat("ECF ", "is cool"), createRemoteCallListener());
		traceCallEnd("callAsynch");
		sleep(1500);
	}

	public void testFireAsynch() throws Exception {
		final IRemoteService service = registerAndGetRemoteService();

		traceCallStart("fireAsynch");
		service.fireAsynch(createRemoteConcat("Eclipse ", "sucks"));
		traceCallEnd("fireAsynch");

		sleep(1500);
	}

	public void testProxy() throws Exception {
		final IRemoteService service = registerAndGetRemoteService();

		final IConcatService proxy = (IConcatService) service.getProxy();
		assertNotNull(proxy);
		traceCallStart("getProxy");
		final String result = proxy.concat("ECF ", "sucks");
		traceCallEnd("getProxy", result);
		sleep(1500);
	}

	public void testAsyncResult() throws Exception {
		final IRemoteService service = registerAndGetRemoteService();
		traceCallStart("callAsynchResult");
		final IAsyncResult result = service.callAsynch(createRemoteConcat("ECF AsynchResults ", "are cool"));
		traceCallEnd("callAsynchResult", result);
		assertNotNull(result);
		sleep(4000);
	}

}
