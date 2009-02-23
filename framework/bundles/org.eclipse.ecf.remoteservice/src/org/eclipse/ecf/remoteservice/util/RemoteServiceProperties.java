/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.remoteservice.util;

import java.util.Properties;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.Constants;

/**
 *
 */
public class RemoteServiceProperties extends Properties {

	private static final long serialVersionUID = -7308465594888236414L;

	public RemoteServiceProperties(String containerFactory, IContainer container) {
		this(containerFactory, container.getID());
	}

	public RemoteServiceProperties(String containerFactory, ID containerID) {
		super();
		put(Constants.SERVICE_CONTAINER_ID_FACTORY, containerFactory);
		put(Constants.SERVICE_CONTAINER_ID, containerID.getName());
	}
}
