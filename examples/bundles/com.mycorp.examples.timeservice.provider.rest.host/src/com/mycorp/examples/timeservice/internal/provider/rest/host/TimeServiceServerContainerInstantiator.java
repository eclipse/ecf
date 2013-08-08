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

import java.util.Collection;
import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainerInstantiator;
import org.osgi.service.http.HttpService;

import com.mycorp.examples.timeservice.provider.rest.common.TimeServiceRestNamespace;

public class TimeServiceServerContainerInstantiator extends
		ServletServerContainerInstantiator {

	@Override
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		try {
			// Get HttpServices
			Collection<HttpService> httpServices = TimeServiceHttpServiceComponent.getDefault().getHttpServices();
			if (httpServices == null || httpServices.size() == 0) throw new NullPointerException("Cannot get HttpService for TimeServiceServerContainer creation");
			// If we've got more than one, then we'll just use the first one
			HttpService httpService = httpServices.iterator().next();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) parameters[0];
			String id = (String) map.get("id");
			return new TimeServiceServerContainer(IDFactory.getDefault()
					.createID(TimeServiceRestNamespace.NAME, id), httpService);
		} catch (Exception e) {
			throw new ContainerCreateException(
					"Could not create TimeServiceServerContainer", e);
		}
	}

	@Override
	public String[] getSupportedAdapterTypes(
			ContainerTypeDescription description) {
		return getInterfacesAndAdaptersForClass(TimeServiceServerContainer.class);
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] { TimeServiceServerContainer.NAME };
	}
}
