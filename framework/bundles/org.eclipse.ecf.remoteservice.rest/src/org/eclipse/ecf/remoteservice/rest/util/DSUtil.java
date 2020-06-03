/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Andre Dietisheim - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.rest.util;

import org.osgi.framework.*;

public class DSUtil {
	/**
	 * Checks whether the declarative services daemon is running.
	 * 
	 * @param context
	 *            the context
	 * 
	 * @return <tt>true</tt>, if is declarative services are running
	 */
	public static boolean isRunning(BundleContext context) {
		ServiceReference[] serviceReferences = null;
		try {
			serviceReferences = context.getServiceReferences(IDSPresent.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			// ignore
		}
		return serviceReferences != null && serviceReferences.length > 0;
	}

}
