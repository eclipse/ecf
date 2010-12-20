/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;

public interface IServiceInfoFactory {

	public IServiceInfo createServiceInfoForDiscovery(
			IDiscoveryAdvertiser advertiser,
			EndpointDescription endpointDescription);

	public IServiceInfo removeServiceInfoForUndiscovery(
			IDiscoveryAdvertiser advertiser,
			EndpointDescription endpointDescription);

	public boolean removeServiceInfo(IDiscoveryAdvertiser advertiser,
			EndpointDescription endpointDescription);

	public void removeAllServiceInfos();

}
