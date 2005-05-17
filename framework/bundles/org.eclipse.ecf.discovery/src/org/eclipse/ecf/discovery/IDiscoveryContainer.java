/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery;

import java.io.IOException;

import org.eclipse.ecf.core.identity.ServiceID;

public interface IDiscoveryContainer {
	public void addServiceListener(ServiceID type, IServiceListener listener);
	public void removeServiceListener(ServiceID type, IServiceListener listener);
	public void addServiceTypeListener(IServiceTypeListener listener);
	public void removeServiceTypeListener(IServiceTypeListener listener);
	public void registerServiceType(ServiceID serviceType);
	public void registerService(IServiceInfo serviceInfo) throws IOException;
	public IServiceInfo getServiceInfo(ServiceID service, int timeout);
	public void requestServiceInfo(ServiceID service, int timeout);
	public void unregisterService(IServiceInfo serviceInfo);
	public void unregisterAllServices();
	public IServiceInfo [] getServices(ServiceID type);
}
