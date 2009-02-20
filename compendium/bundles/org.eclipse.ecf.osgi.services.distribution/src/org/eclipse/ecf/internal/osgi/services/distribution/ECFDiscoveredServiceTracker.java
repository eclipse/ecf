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

import org.eclipse.ecf.core.util.Trace;
import org.osgi.service.discovery.DiscoveredServiceNotification;
import org.osgi.service.discovery.DiscoveredServiceTracker;

public class ECFDiscoveredServiceTracker implements DiscoveredServiceTracker {

	public void serviceChanged(DiscoveredServiceNotification notification) {
		// XXX TODO
		Trace.entering(Activator.PLUGIN_ID, DebugOptions.FINDHOOKDEBUG, this
				.getClass(), "serviceChanged", notification);
	}

}
