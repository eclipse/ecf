/*******************************************************************************
* Copyright (c) 2016 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice;

import java.util.Map;
import org.eclipse.ecf.remoteservice.RSARemoteServiceContainerAdapter.RSARemoteServiceRegistration;

/**
 * @since 8.9
 */
public interface IRSAHostContainerAdapter {

	Map<String, Object> registerEndpoint(RSARemoteServiceRegistration registration);

	void unregisterEndpoint(RSARemoteServiceRegistration registration);

}
