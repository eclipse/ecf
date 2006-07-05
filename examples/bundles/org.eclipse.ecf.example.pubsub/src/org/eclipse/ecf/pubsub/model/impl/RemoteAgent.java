/**
 * Copyright (c) 2006 Ecliptical Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ecliptical Software Inc. - initial API and implementation
 */
package org.eclipse.ecf.pubsub.model.impl;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.pubsub.model.IReplicaModel;

public class RemoteAgent extends AgentBase implements IReplicaModel {
	
	public Object getData() {
		return data;
	}
	
	protected void disconnected() {
		config.getContext().getSharedObjectManager().removeSharedObject(config.getSharedObjectID());
	}
	
	protected void disconnected(ID containerID) {
		if (containerID.equals(config.getHomeContainerID()))
			disconnected();
	}
	
	protected void received(ID containerID, Object data) {
		apply(data);
	}
}
