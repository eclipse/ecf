/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery.impl;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;

public class ServiceContainerEvent implements IServiceEvent {

	private static final long serialVersionUID = 1L;

	protected IServiceInfo info;
	protected ID containerID;
	
	public ServiceContainerEvent(IServiceInfo info, ID containerID) {
		this.info = info;
		this.containerID = containerID;
	}
	public IServiceInfo getServiceInfo() {
		return info;
	}

	public ID getLocalContainerID() {
		return containerID;
	}

}
