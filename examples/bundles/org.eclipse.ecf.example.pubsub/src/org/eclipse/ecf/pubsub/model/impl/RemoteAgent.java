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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.example.pubsub.SerializationUtil;
import org.eclipse.ecf.pubsub.model.IModelUpdater;
import org.eclipse.ecf.pubsub.model.IReplicaModel;

public class RemoteAgent extends AgentBase implements IReplicaModel {
	
	protected void initializeData(Object data) throws SharedObjectInitException {
		try {
			this.data = SerializationUtil.deserialize((byte[]) data);
		} catch (IOException e) {
			throw new SharedObjectInitException(e);
		} catch (ClassNotFoundException e) {
			throw new SharedObjectInitException(e);
		}
	}
	
	protected void initializeUpdater() throws SharedObjectInitException {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		if (registry == null)
			throw new SharedObjectInitException("No Platform Extension Registry.");
		
		IConfigurationElement[] elements = registry.getConfigurationElementsFor("org.eclipse.ecf.example.pubsub.modelUpdater");
		for (int i = 0; i < elements.length; ++i) {
			if (updaterID.equals(elements[i].getAttribute("id"))) {
				try {
					updater = (IModelUpdater) elements[i].createExecutableExtension("class");
				} catch (CoreException e) {
					throw new SharedObjectInitException(e);
				} catch (ClassCastException e) {
					throw new SharedObjectInitException(e);
				}

				break;
			}
		}

		if (updater == null)
			throw new SharedObjectInitException("Could not find specified Model Updater.");
	}
	
	protected void disconnected() {
		config.getContext().getSharedObjectManager().removeSharedObject(config.getSharedObjectID());
	}
	
	protected void disconnected(ID containerID) {
		if (containerID.equals(config.getHomeContainerID()))
			disconnected();
	}
	
	protected void received(ID containerID, Object data) {
		try {
			updater.update(this.data, SerializationUtil.deserialize((byte[]) data));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
