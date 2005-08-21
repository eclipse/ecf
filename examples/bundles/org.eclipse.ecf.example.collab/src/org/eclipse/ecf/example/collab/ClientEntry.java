/**
 * 
 */
package org.eclipse.ecf.example.collab;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject;

public class ClientEntry {
	ISharedObjectContainer client;
	EclipseCollabSharedObject obj;
	String containerType;
	boolean isDisposed = false;
	
	public ClientEntry(String type, ISharedObjectContainer cont) {
		this.containerType = type;
		this.client = cont;
	}

	public ISharedObjectContainer getContainer() {
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