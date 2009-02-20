/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jslp.container;

import ch.ethz.iks.slp.ServiceLocationException;
import ch.ethz.iks.slp.ServiceURL;
import java.net.URI;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.ecf.internal.provider.jslp.ServicePropertiesAdapter;
import org.eclipse.ecf.internal.provider.jslp.ServiceURLAdapter;
import org.eclipse.ecf.provider.jslp.identity.JSLPNamespace;

public class JSLPServiceInfo extends ServiceInfo implements IServiceInfo {

	private static final long serialVersionUID = 6828789192986625259L;

	public JSLPServiceInfo(final URI anURI, final IServiceID serviceID, final int priority, final int weight, final IServiceProperties props) {
		super(anURI, serviceID, priority, weight, props);
	}

	/**
	 * @param serviceID
	 * @deprecated
	 */
	public JSLPServiceInfo(final IServiceID serviceID) {
		super(null, serviceID, DEFAULT_PRIORITY, DEFAULT_WEIGHT, new ServiceProperties());
	}

	public JSLPServiceInfo(final IServiceInfo aSI) throws IDCreateException {
		this(aSI.getLocation(), ServiceIDFactory.getDefault().createServiceID(IDFactory.getDefault().getNamespaceByName(JSLPNamespace.NAME), aSI.getServiceID().getServiceTypeID(), aSI.getServiceID().getServiceName()), aSI.getPriority(), aSI.getWeight(), aSI.getServiceProperties());
	}

	public JSLPServiceInfo(final ServiceURLAdapter anAdapter, final int priority, final int weight, final ServicePropertiesAdapter aServicePropertiesAdapter) {
		this(anAdapter.getURI(), anAdapter.getIServiceID(), priority, weight, aServicePropertiesAdapter.toServiceProperties());
	}

	public ServiceURL getServiceURL() throws ServiceLocationException {
		final IServiceTypeID stid = getServiceID().getServiceTypeID();
		final URI location = getLocation();
		final String scheme = location.getScheme();
		final String authority = location.getAuthority();
		final String path = location.getPath() == null ? "" : location.getPath(); //$NON-NLS-1$
		return new ServiceURL(stid.getInternal() + "://" + scheme + "://" + authority + path, ServiceURL.LIFETIME_PERMANENT); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
