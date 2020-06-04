/****************************************************************************
 * Copyright (c) 2008 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.jslp;

import ch.ethz.iks.slp.*;
import java.util.List;
import java.util.Map;

public interface LocatorDecorator extends Locator {
	/**
	 * @param aServiceType
	 * @param scopes
	 * @return A Map whos keys are {@link ServiceURL} and Entries are {@link List} describing service attributes
	 * @throws ServiceLocationException 
	 */
	public Map getServiceURLs(ServiceType aServiceType, List scopes) throws ServiceLocationException;

	/**
	 * @return A Map whos keys are {@link ServiceURL} and Entries are {@link List} describing service attributes
	 * @throws ServiceLocationException 
	 */
	public Map getServiceURLs() throws ServiceLocationException;

	/**
	 * @param namingAuthority
	 * @param scopes
	 * @return A List of a ServiceURLs for the given namingAuthority and scopes
	 * @throws ServiceLocationException
	 */
	public List getServiceURLs(final String namingAuthority, final List scopes) throws ServiceLocationException;
}
