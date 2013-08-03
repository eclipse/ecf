/*******************************************************************************
* Copyright (c) 2013 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package com.mycorp.examples.timeservice.internal.provider.rest.consumer;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Map;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.client.IRemoteCallable;
import org.eclipse.ecf.remoteservice.client.IRemoteResponseDeserializer;
import org.eclipse.ecf.remoteservice.rest.RestCallableFactory;
import org.eclipse.ecf.remoteservice.rest.client.HttpPostRequestType;
import org.eclipse.ecf.remoteservice.rest.client.RestClientContainer;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.util.ObjectSerializationUtil;

import com.mycorp.examples.timeservice.ITimeService;
import com.mycorp.examples.timeservice.provider.rest.common.TimeServiceRestNamespace;

public class TimeServiceRestClientContainer extends RestClientContainer {

	public static final String NAME = "com.mycorp.examples.timeservice.rest.consumer";
	
	class TimeServiceRestResponseDeserializer implements
			IRemoteResponseDeserializer {
		public Object deserializeResponse(String endpoint, IRemoteCall call,
				IRemoteCallable callable,
				@SuppressWarnings("rawtypes") Map responseHeaders,
				byte[] responseBody) throws NotSerializableException {
			try {
				return new ObjectSerializationUtil()
						.deserializeFromBytes(responseBody);
			} catch (IOException e) {
				throw new NotSerializableException(
						"Could not deserialize server response");
			}
		}

	}

	private IRemoteServiceRegistration reg;

	public TimeServiceRestClientContainer(RestID id) {
		super(id);
		setResponseDeserializer(new TimeServiceRestResponseDeserializer());
	}

	@Override
	public void connect(ID targetID, IConnectContext connectContext1)
			throws ContainerConnectException {
		super.connect(targetID, connectContext1);
		// Now setup TimeService remote registration
		IRemoteCallable callable = RestCallableFactory.createCallable(
				"getCurrentTime", ITimeService.class.getName(), null,
				new HttpPostRequestType(),30000);
		reg = registerCallables(new String[] { ITimeService.class.getName() },
				new IRemoteCallable[][] { { callable } }, null);
	}

	@Override
	public void disconnect() {
		super.disconnect();
		if (reg != null) {
			reg.unregister();
			reg = null;
		}
	}

	@Override
	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(
				TimeServiceRestNamespace.NAME);
	}
}
