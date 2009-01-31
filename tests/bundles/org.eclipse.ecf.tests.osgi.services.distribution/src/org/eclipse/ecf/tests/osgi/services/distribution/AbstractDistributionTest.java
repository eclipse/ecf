/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution;

import java.util.Dictionary;
import java.util.Enumeration;

import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;
import org.eclipse.ecf.tests.remoteservice.IConcatService;
import org.osgi.framework.InvalidSyntaxException;

public abstract class AbstractDistributionTest extends
		ContainerAbstractTestCase {

	protected IRemoteServiceContainerAdapter[] adapters = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.tests.ContainerAbstractTestCase#getClientContainerName()
	 */
	protected abstract String getClientContainerName();

	protected void setClientCount(int count) {
		super.setClientCount(count);
		adapters = new IRemoteServiceContainerAdapter[count];
	}

	protected void setupRemoteServiceAdapters() throws Exception {
		final int clientCount = getClientCount();
		for (int i = 0; i < clientCount; i++) {
			adapters[i] = (IRemoteServiceContainerAdapter) getClients()[i]
					.getAdapter(IRemoteServiceContainerAdapter.class);
		}
	}

	protected IRemoteServiceContainerAdapter[] getRemoteServiceAdapters() {
		return adapters;
	}

	protected IRemoteServiceListener createRemoteServiceListener(final boolean server) {
		return new IRemoteServiceListener() {
			public void handleServiceEvent(IRemoteServiceEvent event) {
				System.out.println((server?"server":"client")+"handleServiceEvent(" + event + ")");
			}
		};
	}

	protected void addRemoteServiceListeners() {
		for (int i = 0; i < adapters.length; i++) {
			adapters[i].addRemoteServiceListener(createRemoteServiceListener(i==0));
		}
	}

	protected IRemoteServiceRegistration registerService(
			IRemoteServiceContainerAdapter adapter, String serviceInterface,
			Object service, Dictionary serviceProperties, int sleepTime) {
		final IRemoteServiceRegistration result = adapter
				.registerRemoteService(new String[] { serviceInterface },
						service, serviceProperties);
		sleep(sleepTime);
		return result;
	}

	protected IRemoteServiceReference[] getRemoteServiceReferences(
			IRemoteServiceContainerAdapter adapter, String clazz, String filter) {
		try {
			return adapter.getRemoteServiceReferences(null, clazz, filter);
		} catch (final InvalidSyntaxException e) {
			fail("should not happen");
		}
		return null;
	}

	protected IRemoteService getRemoteService(
			IRemoteServiceContainerAdapter adapter, String clazz, String filter) {
		final IRemoteServiceReference[] refs = getRemoteServiceReferences(
				adapter, clazz, filter);
		if (refs == null || refs.length == 0)
			return null;
		return adapter.getRemoteService(refs[0]);
	}

	protected String getFilterFromServiceProperties(Dictionary serviceProperties) {
		StringBuffer filter = null;
		if (serviceProperties != null && serviceProperties.size() > 0) {
			filter = new StringBuffer("(&");
			for (final Enumeration e = serviceProperties.keys(); e
					.hasMoreElements();) {
				final Object key = e.nextElement();
				final Object val = serviceProperties.get(key);
				if (key != null && val != null) {
					filter.append("(").append(key).append("=").append(val)
							.append(")");
				}
			}
			filter.append(")");
		}
		return (filter == null) ? null : filter.toString();
	}

	protected IRemoteService registerAndGetRemoteService(
			IRemoteServiceContainerAdapter server,
			IRemoteServiceContainerAdapter client, String serviceName,
			Dictionary serviceProperties, int sleepTime) {
		registerService(server, serviceName, createService(),
				serviceProperties, sleepTime);
		return getRemoteService(client, serviceName,
				getFilterFromServiceProperties(serviceProperties));
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

	protected Object createService() {
		return new IConcatService() {
			public String concat(String string1, String string2) {
				final String result = string1.concat(string2);
				System.out.println("SERVICE.concat(" + string1 + "," + string2
						+ ") returning " + result);
				return string1.concat(string2);
			}
		};
	}


}
