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
package org.eclipse.ecf.tests.provider.jslp;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.provider.jslp.identity.JSLPServiceID;

/**
 * Used for testing equality
 */
public class JSLPTestComparator implements Comparator {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		if(arg0 instanceof IServiceInfo && arg1 instanceof IServiceInfo)  {
			
			IServiceInfo first = (IServiceInfo) arg0;
			IServiceInfo second = (IServiceInfo) arg1;
			
			//TODO-mkuppe No prio, weight, scope and protocol atm in the JSLP testcase
			JSLPServiceID firstID = (JSLPServiceID) first.getServiceID();
			JSLPServiceID secondID = (JSLPServiceID) second.getServiceID();
			IServiceTypeID firstTypeID = firstID.getServiceTypeID();
			IServiceTypeID secondTypeID = secondID.getServiceTypeID();
			
			boolean protocolsSame = Arrays.equals(firstTypeID.getProtocols(), secondTypeID.getProtocols());
			boolean scopesSame = Arrays.equals(firstTypeID.getScopes(), secondTypeID.getScopes());
			boolean weightSame = first.getWeight() == second.getWeight();
			boolean prioSame = first.getPriority() == second.getPriority();
			
			boolean namesSame = firstID.getAddress().equals(secondID.getAddress());
			boolean servicesSame = Arrays.equals(firstTypeID.getServices(), secondTypeID.getServices());
			boolean namespacesSame = firstID.getNamespace().equals(secondID.getNamespace());
			boolean naSame = firstTypeID.getNamingAuthority().equals(secondTypeID.getNamingAuthority());
			boolean addressesSame = first.getLocation().equals(second.getLocation());
			boolean propertiesSame = first.getServiceProperties().asProperties().equals(second.getServiceProperties().asProperties());
			boolean result = namesSame && namespacesSame && servicesSame && naSame && addressesSame && propertiesSame;
			if(result == true) {
				return 0;
			}
		}
		return -1;
	}

}
