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
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ui.DiscoveryPropertyTesterUtil;
import org.eclipse.ecf.internal.remoteservices.ui.RemoteServiceHandlerUtil;
import org.eclipse.ecf.remoteservice.Constants;

public class ConnectedTester extends PropertyTester {
	
	public ConnectedTester() {
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

		boolean hasContainer = hasContainer(receiver);

		if(expected && hasContainer) {
			return true;
		} else if(expected && !hasContainer) {
			return false;
		} else if(!expected && hasContainer) {
			return false;
		} else {
			return true;
		}
	}

	private boolean hasContainer(Object receiver) {
		// get the container instance
		IServiceInfo serviceInfo = DiscoveryPropertyTesterUtil.getIServiceInfoReceiver(receiver);
		final String connectNamespace = getConnectNamespace(serviceInfo);
		final String connectId = getConnectID(serviceInfo);
		try {
			ID createConnectId = IDFactory.getDefault().createID(connectNamespace, connectId);
			return (RemoteServiceHandlerUtil.getContainerWithConnectId(createConnectId) != null);
		} catch (IDCreateException e) {
			//Trace.trace(...);
			return false;
		}
	}

	private String getConnectNamespace(IServiceInfo serviceInfo) {
		return serviceInfo.getServiceProperties().getPropertyString(Constants.SERVICE_CONNECT_ID_NAMESPACE);
	}

	private String getConnectID(IServiceInfo serviceInfo) {
		return serviceInfo.getServiceProperties().getPropertyString(Constants.SERVICE_CONNECT_ID);
	}
}
