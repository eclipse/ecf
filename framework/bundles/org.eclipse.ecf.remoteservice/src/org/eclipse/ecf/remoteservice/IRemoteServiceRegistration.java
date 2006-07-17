package org.eclipse.ecf.remoteservice;

import java.util.Dictionary;

import org.eclipse.ecf.core.identity.ID;

public interface IRemoteServiceRegistration {
	public ID getLocalContainerID();
	public IRemoteServiceReference getReference();
	public void setProperties(Dictionary properties);
	public void unregister();
}
