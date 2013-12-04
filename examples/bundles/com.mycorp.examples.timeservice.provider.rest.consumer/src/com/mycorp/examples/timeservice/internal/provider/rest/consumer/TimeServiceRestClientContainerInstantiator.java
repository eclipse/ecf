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

import java.util.Arrays;
import java.util.List;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.rest.client.RestClientContainerInstantiator;

public class TimeServiceRestClientContainerInstantiator extends
		RestClientContainerInstantiator {

	private static final String TIMESERVICE_HOST_CONFIG_NAME = "com.mycorp.examples.timeservice.rest.host";

	@Override
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		// Create new container instance
		return new TimeServiceRestClientContainer();
	}

	public String[] getImportedConfigs(ContainerTypeDescription description,
			String[] exporterSupportedConfigs) {
		@SuppressWarnings("rawtypes")
		List supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		// If the supportedConfigs contains the timeservice host config,
		// then we are the client to handle it!
		if (supportedConfigs.contains(TIMESERVICE_HOST_CONFIG_NAME))
			return new String[] { TimeServiceRestClientContainer.TIMESERVICE_CONSUMER_CONFIG_NAME };
		else return null;
	}

}
