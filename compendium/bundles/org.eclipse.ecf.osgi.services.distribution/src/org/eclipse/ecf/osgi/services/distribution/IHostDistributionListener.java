/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.distribution;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceReference;

public interface IHostDistributionListener {

	public void registered(ServiceReference serviceReference,
			IRemoteServiceContainer remoteServiceContainer,
			IRemoteServiceRegistration remoteRegistration);

	public void unregistered(ServiceReference serviceReference,
			IRemoteServiceRegistration remoteReference);

}
