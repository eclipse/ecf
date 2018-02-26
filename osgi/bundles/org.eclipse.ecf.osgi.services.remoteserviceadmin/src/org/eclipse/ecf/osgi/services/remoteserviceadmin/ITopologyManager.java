/*******************************************************************************
 * Copyright (c) 2017 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;

/**
 * @since 4.6
 */
public interface ITopologyManager {

	public static final String ENDPOINT_ALLOWLOCALHOST_PROP = "allowLocalhost"; //$NON-NLS-1$
	public static final boolean ENDPOINT_ALLOWLOCALHOST = Boolean
			.getBoolean(ITopologyManager.class.getName() + "." + ENDPOINT_ALLOWLOCALHOST_PROP); //$NON-NLS-1$
	public static final String ENDPOINT_CONDITIONAL_OP_PROP = "conditionalOp"; //$NON-NLS-1$
	public static final String ENDPOINT_CONDITIONAL_OP = System
			.getProperty(ITopologyManager.class.getName() + "." + ENDPOINT_CONDITIONAL_OP_PROP, "&"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String ENDPOINT_EXTRA_CONDITIONAL_PROP = "conditional"; //$NON-NLS-1$
	public static final String ENDPOINT_EXTRA_CONDITIONAL = System
			.getProperty(ITopologyManager.class.getName() + "." + ENDPOINT_EXTRA_CONDITIONAL_PROP); //$NON-NLS-1$

	public static final String ENDPOINT_EXTRA_FILTERS_PROP = "extraFilters"; //$NON-NLS-1$
	public static final String ENDPOINT_EXTRA_FILTERS = System
			.getProperty(ITopologyManager.class.getName() + "." + ENDPOINT_EXTRA_FILTERS_PROP); //$NON-NLS-1$

	public static final String ONLY_ECF_SCOPE = "(" + RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE + "=*)"; //$NON-NLS-1$ //$NON-NLS-2$

	public String[] getEndpointFilters();
	public String[] setEndpointFilters(String[] newFilters);

}
