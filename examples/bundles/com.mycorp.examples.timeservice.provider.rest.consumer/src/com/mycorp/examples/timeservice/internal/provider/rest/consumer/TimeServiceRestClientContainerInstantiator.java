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
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.remoteservice.rest.client.RestClientContainerInstantiator;

import com.mycorp.examples.timeservice.provider.rest.common.TimeServiceRestID;
import com.mycorp.examples.timeservice.provider.rest.common.TimeServiceRestNamespace;

public class TimeServiceRestClientContainerInstantiator extends
		RestClientContainerInstantiator {

	private static final String TIMESERVICE_SERVER_NAME = "com.mycorp.examples.timeservice.rest.host";

	@Override
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		try {
			String containerId = "uuid:"
					+ java.util.UUID.randomUUID().toString();
			return new TimeServiceRestClientContainer(
					(TimeServiceRestID) IDFactory.getDefault().createID(
							TimeServiceRestNamespace.NAME, containerId));
		} catch (Exception e) {
			throw new ContainerCreateException(
					"Could not create TimeServiceRestClientContainer", e);
		}
	}

	@Override
	public String[] getSupportedAdapterTypes(
			ContainerTypeDescription description) {
		return getInterfacesAndAdaptersForClass(TimeServiceRestClientContainer.class);
	}

	public String[] getImportedConfigs(ContainerTypeDescription description,
			String[] exporterSupportedConfigs) {
		@SuppressWarnings("rawtypes")
		List supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		if (supportedConfigs.contains(TIMESERVICE_SERVER_NAME))
			return new String[] { TimeServiceRestClientContainer.NAME };
		return null;
	}

}
