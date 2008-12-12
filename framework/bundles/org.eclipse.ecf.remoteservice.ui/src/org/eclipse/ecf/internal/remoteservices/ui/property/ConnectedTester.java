/*******************************************************************************
 * Copyright (c) 2008 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.remoteservices.ui.property;


import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ui.DiscoveryPropertyTesterUtil;
import org.eclipse.ecf.internal.remoteservices.ui.Activator;
import org.eclipse.ecf.remoteservice.Constants;

public class ConnectedTester extends PropertyTester {
	
	private IContainerManager containerManager;

	public ConnectedTester() {
		containerManager = Activator.getDefault().getContainerManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// consumers expect connected or disconnected
		if(!(expectedValue instanceof Boolean)) {
			return false;
		}
		boolean expected = ((Boolean) expectedValue).booleanValue();

		// get the container instance
		IServiceInfo serviceInfo = DiscoveryPropertyTesterUtil.getIServiceInfoReceiver(receiver);
		final String connectNamespace = getConnectNamespace(serviceInfo);
		final String connectId = getConnectID(serviceInfo);
		try {
			ID createConnectId = IDFactory.getDefault().createID(connectNamespace, connectId);
			IContainer container = containerManager.getContainer(createConnectId);
			if(container == null) {
				//Trace.trace(...);
				return expected == false;
			}
			ID connectedId = container.getConnectedID();
			boolean isConnected = connectedId == null ? false : true;
			return expected == isConnected;
		} catch (IDCreateException e) {
			//Trace.trace(...);
			return expected == false;
		}
	}

	private String getConnectNamespace(IServiceInfo serviceInfo) {
		return serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_CONNECT_ID_NAMESPACE_PROPERTY);
	}

	private String getConnectID(IServiceInfo serviceInfo) {
		return serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_CONNECT_ID_PROPERTY);
	}
}
