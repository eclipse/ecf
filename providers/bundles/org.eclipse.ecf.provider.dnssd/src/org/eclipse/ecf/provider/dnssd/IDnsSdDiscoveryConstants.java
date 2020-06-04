/****************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.dnssd;

public interface IDnsSdDiscoveryConstants {

	/**
	 * Config admin key to define the default search path
	 */
	public final String CA_SEARCH_PATH = "searchPath"; //$NON-NLS-1$
	/**
	 * Config admin key to define the default resolver
	 */
	public final String CA_RESOLVER = "resolver"; //$NON-NLS-1$
	
	/**
	 * Config admin key to define the TSIG key to be used to sign requests 
	 */
	public final String CA_TSIG_KEY = "tsig-key"; //$NON-NLS-1$
	public final Object CA_TSIG_KEY_NAME = "tsig-key-name"; //$NON-NLS-1$

}
