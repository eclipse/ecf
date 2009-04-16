/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.net.URI;

public class DiscoveredServiceID {

	private URI serviceLocation;
	private long serviceId;
	private long hashCode = 7;

	public DiscoveredServiceID(URI location, long serviceId) {
		this.serviceLocation = location;
		this.serviceId = serviceId;
		hashCode = 31 * hashCode + (int) (serviceId ^ (serviceId >>> 32));
		hashCode = 31 * hashCode + serviceLocation.hashCode();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DiscoveredServiceID))
			return false;
		DiscoveredServiceID other = (DiscoveredServiceID) o;
		return serviceLocation.equals(other.serviceLocation)
				&& this.serviceId == other.serviceId;
	}
}
