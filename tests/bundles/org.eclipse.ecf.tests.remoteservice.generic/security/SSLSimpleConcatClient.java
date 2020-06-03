/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.remoteservice.generic;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;
import org.eclipse.ecf.tests.remoteservice.IConcatService;
import org.eclipse.osgi.util.NLS;

public class SSLSimpleConcatClient {

	public static final String CLIENT_TYPE = "ecf.generic.ssl.client";
	
	private IRemoteServiceContainer rsContainer;
	
	protected String SERVER_ID = "ecfssl://localhost:{0}"+SSLSimpleConcatServer.PATH;
	
	private IRemoteServiceReference rsReference;
	private IRemoteService remoteService;
	
	public synchronized IRemoteService getRemoteService() {
		if (remoteService == null) {
			remoteService = rsContainer.getContainerAdapter().getRemoteService(rsReference);
		}
		return remoteService;
	}
	
	public void start(int port) throws Exception {
		IContainer client = ContainerFactory.getDefault().createContainer(
				CLIENT_TYPE);
		// Get adapter for accessing remote services
		IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) client
				.getAdapter(IRemoteServiceContainerAdapter.class);

		rsContainer = new RemoteServiceContainer(client,adapter);
		
		System.out.println("Client created with ID=" + client.getID());

		ID connectTargetID = IDFactory.getDefault().createStringID(
				NLS.bind(SERVER_ID, new Integer(port)));
		
		System.out.println("Attempting connect to id="+connectTargetID);
		client.connect(connectTargetID, null);
		System.out.println("Client connected to connectTargetID="
				+ connectTargetID);

		Thread.sleep(1000);
		

		// Get remote service reference
		IRemoteServiceReference[] refs = adapter.getRemoteServiceReferences(
				(ID[]) null, IConcatService.class.getName(), null);
		
		rsReference = refs[0];
		
		System.out.println("Remote service with ref=" + refs[0]);
	}
	
	public void stop() {
		if (rsContainer != null) {
			rsContainer.getContainerAdapter().ungetRemoteService(rsReference);
			remoteService = null;
			rsReference = null;
			rsContainer.getContainer().disconnect();
			rsContainer = null;
		}
		
	}

}
