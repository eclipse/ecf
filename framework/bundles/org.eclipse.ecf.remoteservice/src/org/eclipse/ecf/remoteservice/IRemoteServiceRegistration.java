package org.eclipse.ecf.remoteservice;

import java.util.Dictionary;

import org.eclipse.ecf.core.identity.ID;

public interface IRemoteServiceRegistration {
	public ID getContainerID();
	public IRemoteServiceReference getReference();
	public void setProperties(Dictionary properties);
	public Object getProperty(String key);
	public String [] getPropertyKeys();
	public void unregister();
}
