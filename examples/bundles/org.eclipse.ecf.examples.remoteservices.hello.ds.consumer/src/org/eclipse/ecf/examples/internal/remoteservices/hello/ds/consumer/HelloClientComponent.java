/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.examples.internal.remoteservices.hello.ds.consumer;

import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceProxy;
import org.eclipse.ecf.remoteservice.RemoteServiceHelper;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;

public class HelloClientComponent {

	private static final String CONSUMER_NAME = "helloclientcomponent";
	
	private IContainerFactory containerFactory;
	
	void setContainerFactory(IContainerFactory cf) {
		this.containerFactory = cf;
	}
	
	void activate() throws Exception {
		// Create client of appropriate type
		containerFactory.createContainer("ecf.generic.client");
	}
	
	public void bindRemoteService(IHello proxy) {
		// First print out on console that we got something
		System.out.println("Got proxy IHello="+proxy);
		//Call proxy
		proxy.hello(CONSUMER_NAME+" via proxy");
		
		// Get IRemoteService from proxy.  This is possible, because for all ECF providers
		// the proxy also implements org.eclipse.ecf.remoteservice.IRemoteServiceProxy
		IRemoteService remoteService = ((IRemoteServiceProxy) proxy).getRemoteService();
		// Create listener for callback in asynchronous call
		IRemoteCallListener listener = new IRemoteCallListener() {
			public void handleEvent(IRemoteCallEvent event) {
				if (event instanceof IRemoteCallCompleteEvent) {
					System.out.println("Completed hello remote service invocation using async");
				}
			}};
		// Call asynchronously with listener
		RemoteServiceHelper.asyncExec(remoteService, "hello", new Object[] { CONSUMER_NAME + " via async" }, listener);
	}
	
}
