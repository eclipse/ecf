/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.remoteservice.service;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

/**
 * OSGI remote service interface.  This interface should be registered
 * by providers when they wish to expose remote service services to OSGI
 * service clients.
 */
public interface IRemoteServiceService extends IRemoteServiceContainerAdapter {
	// no methods for interface

}
