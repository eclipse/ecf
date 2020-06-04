/****************************************************************************
 * Copyright (c) 2014 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package com.mycorp.examples.timeservice.consumer.ds.generic.auth;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.security.IConnectInitiatorPolicy;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerClient;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.ConsumerContainerSelector;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.SelectContainerException;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;

public class GenericAuthConsumerContainerSelector extends
		ConsumerContainerSelector {

	public GenericAuthConsumerContainerSelector() {
		super(true);
	}

	@Override
	protected IRemoteServiceContainer createContainer(
			ContainerTypeDescription containerTypeDescription,
			String containerTypeDescriptionName,
			@SuppressWarnings("rawtypes") Map properties)
			throws SelectContainerException {
		IRemoteServiceContainer result = super.createContainer(
				containerTypeDescription, containerTypeDescriptionName,
				properties);

		ISharedObjectContainerClient client = (ISharedObjectContainerClient) result
				.getContainer().getAdapter(ISharedObjectContainerClient.class);
		if (client != null) {
			client.setConnectInitiatorPolicy(new IConnectInitiatorPolicy() {
				public void refresh() {
				}

				public Object createConnectData(IContainer container,
						ID targetID, IConnectContext context) {
					// This is called when the client.connect method
					// is called. Here is our chance to send credentials
					// to the server.
					return getConnectData();
				}

				public int getConnectTimeout() {
					return 30000;
				}
			});
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	Object getConnectData() {
		Map results = new HashMap();
		// Get system properties values, if present
		String username = System.getProperty("myUsername");
		if (username != null) {
			results.put("username",username);
			String password = System.getProperty("myPassword");
			if (password != null)
				results.put("password",password);
		}
		if (results.size() > 0)
			return results;
		return null;
	}
}
