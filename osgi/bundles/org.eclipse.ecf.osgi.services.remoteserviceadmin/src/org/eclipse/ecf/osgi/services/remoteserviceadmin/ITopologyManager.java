/****************************************************************************
 * Copyright (c) 2017 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

/**
 * @since 4.6
 */
public interface ITopologyManager {

	public static final String ENDPOINT_ALLOWLOCALHOST_PROP = "allowLocalhost"; //$NON-NLS-1$
	public static final boolean ENDPOINT_ALLOWLOCALHOST = Boolean
			.getBoolean(ITopologyManager.class.getName() + "." + ENDPOINT_ALLOWLOCALHOST_PROP); //$NON-NLS-1$
	public static final String ENDPOINT_EXTRA_FILTERS_PROP = "extraFilters"; //$NON-NLS-1$
	public static final String ENDPOINT_EXTRA_FILTERS = System
			.getProperty(ITopologyManager.class.getName() + "." + ENDPOINT_EXTRA_FILTERS_PROP); //$NON-NLS-1$

	public static final String ONLY_ECF_SCOPE = "(" + RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE + "=*)"; //$NON-NLS-1$ //$NON-NLS-2$

	public static final String EXPORT_REGISTERED_SERVICES_FILTER_PROP = "exportRegisteredServicesFilter"; //$NON-NLS-1$
	public static final String EXPORT_REGISTERED_SERVICES_FILTER = System.getProperty(
			ITopologyManager.class.getName() + "." + EXPORT_REGISTERED_SERVICES_FILTER_PROP, //$NON-NLS-1$
			"(service.exported.interfaces=*)"); //$NON-NLS-1$
	
	public String[] getEndpointFilters();
	public String[] setEndpointFilters(String[] newFilters);

}
