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

import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainerInstantiator;
import org.osgi.service.http.HttpService;

public class TimeServiceServerContainerInstantiator extends
		ServletServerContainerInstantiator {

	@Override
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		// Get first http services from HttpServiceComponent
		HttpService httpService = TimeServiceHttpServiceComponent.getDefault()
				.getHttpServices().iterator().next();
		@SuppressWarnings("unchecked")
		// First parameter should be Map
		Map<String, Object> map = (Map<String, Object>) parameters[0];
		// Get the ID parameter from Map
		return new TimeServiceServerContainer((String) map.get("id"),
					httpService);
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] { TimeServiceServerContainer.TIMESERVICE_HOST_CONFIG_NAME };
	}

}
