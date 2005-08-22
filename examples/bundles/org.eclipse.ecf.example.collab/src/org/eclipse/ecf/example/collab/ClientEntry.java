/**
 * 
 */
package org.eclipse.ecf.example.collab;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject;

public class ClientEntry {
	IContainer client;
	EclipseCollabSharedObject obj;
	String containerType;
	boolean isDisposed = false;
	
	public ClientEntry(String type, IContainer cont) {
		this.containerType = type;
		this.client = cont;
	}

	public IContainer getContainer() {
		return client;
	}

	public String getContainerType() {
		return containerType;
	}

	public ID getConnectedID() {
		return client.getConnectedID();
	}

	public void setObject(EclipseCollabSharedObject obj) {
		this.obj = obj;
	}

	public EclipseCollabSharedObject getObject() {
		return obj;
	}
	public boolean isDisposed() {
		return isDisposed;
	}
	public void dispose() {
		isDisposed = true;
		if (obj != null)
			obj.destroySelf();
		client.dispose();
	}
}