/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.remoteservice;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.util.*;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

/**
 *
 */
public abstract class AbstractRemoteServiceDescription {

	private ID discoveryID;
	private IServiceInfo serviceInfo;

	private IContainerFilter remoteServiceAdapterFilter = new AdapterContainerFilter(IRemoteServiceContainerAdapter.class);

	private IContainerFilter connectedFilter = new ConnectedContainerFilter();

	public AbstractRemoteServiceDescription(IServiceInfo serviceInfo, ID discoveryID) {
		Assert.isNotNull(serviceInfo);
		this.serviceInfo = serviceInfo;
		this.discoveryID = null;
	}

	public ID getDiscoveryID() {
		return this.discoveryID;
	}

	public IServiceInfo getServiceInfo() {
		return this.serviceInfo;
	}

	public abstract Object createProxy() throws Exception;

	protected String getProperty(String key) {
		return serviceInfo.getServiceProperties().getPropertyString(key);
	}

	protected String[] getInterfaces() {
		String val = serviceInfo.getServiceProperties().getPropertyString(org.eclipse.ecf.remoteservice.Constants.OBJECTCLASS);
		return org.eclipse.ecf.core.util.StringUtils.split(val, ";"); //$NON-NLS-1$
	}

	public String getContainerFactoryName() {
		return getProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_FACTORY_NAME);
	}

	public IContainer createContainer() throws ContainerCreateException {
		String containerFactoryName = getContainerFactoryName();
		if (containerFactoryName == null)
			return ContainerFactory.getDefault().createContainer();
		return ContainerFactory.getDefault().createContainer(containerFactoryName);
	}

	protected IContainer[] getMatchingContainers(IContainerFilter filter, IContainer[] sourceContainers) {
		List results = new ArrayList();
		for (int i = 0; i < sourceContainers.length; i++)
			if (filter.match(sourceContainers[i]))
				results.add(sourceContainers[i]);
		return (IContainer[]) results.toArray(new IContainer[] {});
	}

	public IContainer[] getRemoteServicesContainers(IContainer[] sourceContainers) {
		return getMatchingContainers(remoteServiceAdapterFilter, sourceContainers);
	}

	public IContainer[] getConnectedContainers(IContainer[] sourceContainers) {
		return getMatchingContainers(connectedFilter, sourceContainers);
	}

	public ID createTargetID(Namespace namespace) throws IDCreateException {
		return IDFactory.getDefault().createID(namespace, getProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_CONTAINER_TARGET));
	}
}
