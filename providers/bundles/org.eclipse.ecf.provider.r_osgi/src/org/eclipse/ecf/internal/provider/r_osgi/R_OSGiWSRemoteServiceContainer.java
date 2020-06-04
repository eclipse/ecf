/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.r_osgi;

import ch.ethz.iks.r_osgi.RemoteOSGiService;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.provider.r_osgi.identity.R_OSGiWSNamespace;

class R_OSGiWSRemoteServiceContainer extends R_OSGiRemoteServiceContainer {

	public R_OSGiWSRemoteServiceContainer(RemoteOSGiService service, ID containerID) throws IDCreateException {
		super(service, containerID);
	}

	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(R_OSGiWSNamespace.NAME_WS);
	}
}
