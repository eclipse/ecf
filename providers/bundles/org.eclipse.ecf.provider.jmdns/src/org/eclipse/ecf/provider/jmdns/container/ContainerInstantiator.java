/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.jmdns.container;

import java.io.IOException;
import java.net.InetAddress;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.internal.provider.jmdns.Messages;

public class ContainerInstantiator implements IContainerInstantiator {

	public ContainerInstantiator() {
		super();
	}

	public IContainer createInstance(ContainerTypeDescription description, Object[] args) throws ContainerCreateException {
		try {
			AbstractContainer container = new JMDNSDiscoveryContainer(InetAddress.getLocalHost());
			return container;
		} catch (IDCreateException e) {
			ContainerCreateException excep = new ContainerCreateException(Messages.ContainerInstantiator_EXCEPTION_CONTAINER_CREATE);
			excep.setStackTrace(e.getStackTrace());
			throw excep;
		} catch (IOException e) {
			ContainerCreateException excep = new ContainerCreateException(Messages.ContainerInstantiator_EXCEPTION_GETTING_INETADDRESS);
			excep.setStackTrace(e.getStackTrace());
			throw excep;
		}
	}

	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		return new String[] {IDiscoveryContainerAdapter.class.getName()};
	}

	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		return new Class[0][0];
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}

}
