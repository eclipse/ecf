/*******************************************************************************
* Copyright (c) 2013 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package com.mycorp.examples.timeservice.internal.provider.rest.host;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.servlet.RemoteServiceHttpServlet;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainer;
import org.eclipse.ecf.remoteservice.servlet.HttpServiceComponent;
import org.eclipse.ecf.remoteservice.servlet.ObjectSerializationResponseSerializer;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.mycorp.examples.timeservice.ITimeService;
import com.mycorp.examples.timeservice.provider.rest.common.TimeServiceRestNamespace;

public class TimeServiceServerContainer extends ServletServerContainer {

	public static final String NAME = "com.mycorp.examples.timeservice.rest.host";

	private final String timeServiceServletName = "/" + ITimeService.class.getName();

	private HttpService httpService;
	
	public TimeServiceServerContainer(ID id, HttpService httpService) throws ServletException,
			NamespaceException {
		super(id);
		this.httpService = httpService;
		// Register our servlet with 
		this.httpService.registerServlet(timeServiceServletName,
				new TimeRemoteServiceHttpServlet(), null, null);
	}

	@Override
	public void dispose() {
		if (httpService != null) {
			httpService.unregister(timeServiceServletName);
			httpService = null;
		}
		super.dispose();
	}

	@Override
	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(
				TimeServiceRestNamespace.NAME);
	}

	class TimeRemoteServiceHttpServlet extends RemoteServiceHttpServlet {

		private static final long serialVersionUID = 3906126401901826462L;

		public TimeRemoteServiceHttpServlet() {
			// Set response serializer to serialized time
			// service call
			setRemoteCallResponseSerializer(new ObjectSerializationResponseSerializer());
		}

		// Handle post call right here.
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			
			// No arguments to getCurrentTime() method, so
			// nothing to deserialize
			
			// Get local ITimeService
			ITimeService timeService = HttpServiceComponent.getDefault()
					.getService(ITimeService.class);
			
			// Call local service
			Long currentTime = timeService.getCurrentTime();
			
			// Serialize response
			getRemoteCallResponseSerializer().serializeResponse(resp,
					currentTime);
		}
	}

}
