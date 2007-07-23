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

package org.eclipse.ecf.tests.remoteservice.presence;

import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.tests.presence.PresenceAbstractTestCase;

/**
 * 
 */
public abstract class AbstractRemoteServiceTestCase extends PresenceAbstractTestCase {

	protected IRemoteServiceContainerAdapter[] adapters = null;

	protected void setClientCount(int count) {
		super.setClientCount(count);
		adapters = new IRemoteServiceContainerAdapter[count];
	}

	protected abstract Object createService();
	
	protected void setupRemoteServiceAdapters() throws Exception {
		int clientCount = getClientCount();
		for (int i = 0; i < clientCount; i++) {
			adapters[i] = (IRemoteServiceContainerAdapter) getClients()[i]
					.getAdapter(IRemoteServiceContainerAdapter.class);
		}
	}

	protected IRemoteServiceContainerAdapter [] getRemoteServiceAdapters() {
		return adapters;
	}

	protected IRemoteServiceListener createRemoteServiceListener() {
		return new IRemoteServiceListener() {
			public void handleServiceEvent(IRemoteServiceEvent event) {
				System.out.println("handleServiceEvent("+event+")");
			}
		};
	}

	protected void addRemoteServiceListeners() {
		for (int i =0; i < adapters.length; i++) {
			adapters[i].addRemoteServiceListener(createRemoteServiceListener());
		}
	}

	protected IRemoteCall createRemoteCall(final String method, final Object [] params) {
		return new IRemoteCall() {
			public String getMethod() {
				return method;
			}
	
			public Object[] getParameters() {
				return params;
			}
	
			public long getTimeout() {
				return 3000;
			}};
	}
}
