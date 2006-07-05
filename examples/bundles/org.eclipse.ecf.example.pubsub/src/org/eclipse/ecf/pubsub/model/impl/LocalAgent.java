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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ReplicaSharedObjectDescription;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.pubsub.model.IMasterModel;

public class LocalAgent extends AgentBase implements IMasterModel {
	
	public synchronized void update(Object data) throws IOException {
		apply(data);
		config.getContext().sendMessage(null, data);
	}
	
	public Map getProperties() {
		return Collections.EMPTY_MAP;
	}
	
	public void subscribe(ID containerID) {
		ISharedObjectContext ctx = config.getContext();
		try {
			ctx.sendCreate(containerID, createRemoteAgentDescription());
		} catch (IOException e) {
			// TODO Log me!
			e.printStackTrace();
		}
	}
	
	public void unsubscribe(ID containerID) {
		// TODO Auto-generated method stub
		
	}

	protected void deactivated() {
		if (isConnected())
			try {
				config.getContext().sendDispose(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	protected ReplicaSharedObjectDescription createRemoteAgentDescription() {
		Map props = new HashMap(2);
		props.put(INITIAL_DATA_KEY, data);
		props.put(MODEL_UPDATER_KEY, updaterID);
		return new ReplicaSharedObjectDescription(RemoteAgent.class, config.getSharedObjectID(), config.getHomeContainerID(), props);
	}
}
