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

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;

public class ContainerInstantiator implements
		IContainerInstantiator {

	public ContainerInstantiator() {
		super();
	}

	public IContainer createInstance(
			ContainerTypeDescription description, Object[] args) throws ContainerCreateException {
			try {
				JMDNSDiscoveryContainer container = new JMDNSDiscoveryContainer();
				return container;
			} catch (IDCreateException e) {
				ContainerCreateException excep = new ContainerCreateException("Exception making JMDNS container");
				excep.setStackTrace(e.getStackTrace());
				throw excep;
			} catch (IOException e) {
				ContainerCreateException excep = new ContainerCreateException("Exception getting InetAddress for JMDNS container");
				excep.setStackTrace(e.getStackTrace());
				throw excep;
			}
	}

}
