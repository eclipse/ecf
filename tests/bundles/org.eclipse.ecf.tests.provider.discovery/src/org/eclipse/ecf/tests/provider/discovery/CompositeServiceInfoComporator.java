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
package org.eclipse.ecf.tests.provider.discovery;

import java.net.URI;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.tests.discovery.ServiceInfoComparator;

public class CompositeServiceInfoComporator extends ServiceInfoComparator {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.ServiceInfoComparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		if (arg0 instanceof IServiceInfo && arg1 instanceof IServiceInfo) {
			final IServiceInfo first = (IServiceInfo) arg0;
			final IServiceInfo second = (IServiceInfo) arg1;
			final URI uri1 = first.getLocation();
			final URI uri2 = second.getLocation();
			IServiceID firstID = first.getServiceID();
			IServiceID secondID = second.getServiceID();
			//TODO-mkuppe No prio, weight and protocol atm in the JSLP testcase
			boolean serviceIDs = firstID.getName().equals(secondID.getName()) && firstID.getServiceName().equals(secondID.getServiceName()) && firstID.getServiceTypeID().equals(secondID.getServiceTypeID());
			final boolean result = (serviceIDs && uri1.getHost().equals(uri2.getHost()) && uri1.getPort() == uri2.getPort()/* && first.getPriority() == second.getPriority() && first.getWeight() == second.getWeight() */&& compareServiceProperties(first.getServiceProperties(), second.getServiceProperties()));
			if (result == true) {
				return 0;
			}
		}
		return -1;
	}

}
