/****************************************************************************
 * Copyright (c) 2014 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.internal.remoteservice.java8;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.generic.SSLServerSOContainer;

public class J8SSLServerSOContainer extends SSLServerSOContainer {

	public J8SSLServerSOContainer(ISharedObjectContainerConfig config,
			InetAddress bindAddress, int keepAlive) throws IOException,
			URISyntaxException {
		super(config, bindAddress, keepAlive);
	}
}