/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

public interface DebugOptions {

	public static final String DEBUG = Activator.PLUGIN_ID + "/debug"; //$NON-NLS-1$

	public static final String EXCEPTIONS_CATCHING = DEBUG
			+ "/exceptions/catching"; //$NON-NLS-1$

	public static final String EXCEPTIONS_THROWING = DEBUG
			+ "/exceptions/throwing"; //$NON-NLS-1$

	public static final String METHODS_ENTERING = DEBUG + "/methods/entering"; //$NON-NLS-1$

	public static final String METHODS_EXITING = DEBUG + "/methods/exiting"; //$NON-NLS-1$

	public static final String REMOTE_SERVICE_ADMIN = DEBUG
			+ "/remoteserviceadmin"; //$NON-NLS-1$

	public static final String TOPOLOGY_MANAGER = DEBUG + "/topologymanager"; //$NON-NLS-1$

	public static final String CONTAINER_SELECTOR = DEBUG
			+ "/containerselector"; //$NON-NLS-1$

	public static final String METADATA_FACTORY = DEBUG + "/metadatafactory"; //$NON-NLS-1$

	public static final String ENDPOINT_DESCRIPTION_ADVERTISER = DEBUG
			+ "/endpointdescriptionadvertiser"; //$NON-NLS-1$

	public static final String ENDPOINT_DESCRIPTION_LOCATOR = DEBUG
			+ "/endpointdescriptionlocator"; //$NON-NLS-1$

	public static final String ENDPOINT_DESCRIPTION_READER = DEBUG
			+ "/endpointdescriptionreader"; //$NON-NLS-1$

	public static final String PACKAGE_VERSION_COMPARATOR = DEBUG
			+ "/packageversioncomparator"; //$NON-NLS-1$

}
