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

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.servlet.HttpServiceComponent;
import org.eclipse.ecf.remoteservice.servlet.RemoteServiceHttpServlet;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainer;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.http.HttpService;

import com.mycorp.examples.timeservice.ITimeService;
import com.mycorp.examples.timeservice.provider.rest.common.TimeServiceRestNamespace;

public class TimeServiceServerContainer extends ServletServerContainer {

	public static final String TIMESERVICE_HOST_CONFIG_NAME = "com.mycorp.examples.timeservice.rest.host";
	public static final String TIMESERVICE_SERVLET_NAME = "/" + ITimeService.class.getName();

	private final HttpService httpService;
	
	TimeServiceServerContainer(String id, HttpService httpService) throws ContainerCreateException {
		super(IDFactory.getDefault()
				.createID(TimeServiceRestNamespace.NAME, id));
		this.httpService = httpService;
		// Register our servlet with the given httpService with the TIMESERVICE_SERVLET_NAME
		// which is "/com.mycorp.examples.timeservice.ITimeService"
		try {
			this.httpService.registerServlet(TIMESERVICE_SERVLET_NAME,
					new TimeRemoteServiceHttpServlet(), null, null);
		} catch (Exception e) {
			throw new ContainerCreateException("Could not create Time Service Server Container",e);
		}
	}

	@Override
	public void dispose() {
		httpService.unregister(TIMESERVICE_SERVLET_NAME);
		super.dispose();
	}

	@Override
	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(
				TimeServiceRestNamespace.NAME);
	}

	class TimeRemoteServiceHttpServlet extends RemoteServiceHttpServlet {

		private static final long serialVersionUID = 3906126401901826462L;

		// Handle get call right here.
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			
			// No arguments to getCurrentTime() method, so
			// nothing to deserialize from request
			
			// Get local OSGi ITimeService
			ITimeService timeService = HttpServiceComponent.getDefault()
					.getService(ITimeService.class);
			
			// Call local service to get the time
			Long currentTime = timeService.getCurrentTime();
			
			// Serialize response
		    try {
				resp.getOutputStream().print(new JSONObject().put("time", currentTime).toString());
			} catch (JSONException e) {
				throw new ServletException("json response object could not be created for time service", e);
			}
		}
	}

}
