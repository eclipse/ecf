/*******************************************************************************
 * Copyright (c) 2008 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.jslp;

import ch.ethz.iks.slp.*;
import java.util.*;
import org.eclipse.core.runtime.Assert;

/**
 * This decorator add additional methods which will eventually be moved to jSLP itself
 */
public class LocatorDecoratorImpl implements LocatorDecorator {

	private Locator locator;

	public LocatorDecoratorImpl(Locator aLocator) {
		Assert.isNotNull(aLocator);
		locator = aLocator;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Locator#findAttributes(ch.ethz.iks.slp.ServiceType, java.util.List, java.util.List)
	 */
	public ServiceLocationEnumeration findAttributes(ServiceType type, List scopes, List attributeIds) throws ServiceLocationException {
		return locator.findAttributes(type, scopes, attributeIds);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Locator#findAttributes(ch.ethz.iks.slp.ServiceURL, java.util.List, java.util.List)
	 */
	public ServiceLocationEnumeration findAttributes(ServiceURL url, List scopes, List attributeIds) throws ServiceLocationException {
		return locator.findAttributes(url, scopes, attributeIds);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Locator#findServices(ch.ethz.iks.slp.ServiceType, java.util.List, java.lang.String)
	 */
	public ServiceLocationEnumeration findServices(ServiceType type, List scopes, String searchFilter) throws ServiceLocationException, IllegalArgumentException {
		return locator.findServices(type, scopes, searchFilter);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Locator#findServiceTypes(java.lang.String, java.util.List)
	 */
	public ServiceLocationEnumeration findServiceTypes(String namingAuthority, List scopes) throws ServiceLocationException {
		return locator.findServiceTypes(namingAuthority, scopes);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Locator#getLocale()
	 */
	public Locale getLocale() {
		return locator.getLocale();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.slp.Locator#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		locator.setLocale(locale);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.provider.jslp.LocatorDecorator#getServiceURLs(ch.ethz.iks.slp.ServiceType, java.util.List)
	 */
	public Map getServiceURLs(ServiceType aServiceType, List scopes) throws ServiceLocationException {
		Map result = new HashMap();
		ServiceLocationEnumeration services = findServices(aServiceType, scopes, null);
		while (services.hasMoreElements()) {
			ServiceURL url = (ServiceURL) services.next();
			result.put(url, Collections.list(findAttributes(url, scopes, null)));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.provider.jslp.LocatorDecorator#getServiceURLs()
	 */
	public Map getServiceURLs() throws ServiceLocationException {
		Enumeration stEnum = findServiceTypes(null, null);
		Set aSet = new HashSet(Collections.list(stEnum));
		Map result = new HashMap();
		for (Iterator itr = aSet.iterator(); itr.hasNext();) {
			String type = (String) itr.next();
			ServiceLocationEnumeration services = findServices(new ServiceType(type), null, null);
			while (services.hasMoreElements()) {
				ServiceURL url = (ServiceURL) services.next();
				result.put(url, Collections.list(findAttributes(url, null, null)));
			}
		}
		return result;
	}
}
