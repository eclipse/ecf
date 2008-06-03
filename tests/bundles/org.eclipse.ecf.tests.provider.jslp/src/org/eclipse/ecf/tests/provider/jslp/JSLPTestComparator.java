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

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
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
			
			IServiceID firstID = (JSLPServiceID) first.getServiceID();
			IServiceID secondID = (JSLPServiceID) second.getServiceID();
			IServiceTypeID firstTypeID = firstID.getServiceTypeID();
			IServiceTypeID secondTypeID = secondID.getServiceTypeID();
			
			//TODO-mkuppe No prio, weight and protocol atm in the JSLP testcase
			boolean protocolsSame = Arrays.equals(firstTypeID.getProtocols(), secondTypeID.getProtocols());
			boolean weightSame = first.getWeight() == second.getWeight();
			boolean prioSame = first.getPriority() == second.getPriority();

			String firstName = firstID.getName();
			String secondName = secondID.getName();
			boolean nameSame = firstName.equals(secondName);
			String[] firstServices = firstTypeID.getServices();
			String[] secondServices = secondTypeID.getServices();
			boolean serviceSame = Arrays.equals(firstServices, secondServices);
			Namespace firstNamespace = firstID.getNamespace();
			Namespace secondNamespace = secondID.getNamespace();
			boolean namespaceSame = firstNamespace.equals(secondNamespace);
			String firstNA = firstTypeID.getNamingAuthority();
			String secondsSA = secondTypeID.getNamingAuthority();
			boolean naSame = firstNA.equals(secondsSA);
			URI firstLocation = first.getLocation();
			URI secondLocation = second.getLocation();
			boolean locationSame = firstLocation.equals(secondLocation);
			boolean scopesSame = Arrays.equals(firstTypeID.getScopes(), secondTypeID.getScopes());
			boolean propertySame = first.getServiceProperties().equals(second.getServiceProperties());
			boolean result = nameSame && namespaceSame && serviceSame && naSame && locationSame && scopesSame && propertySame;
			if(result == true) {
				return 0;
			}
		}
		return -1;
	}

}
