package org.eclipse.ecf.remoteservice;

import java.util.Dictionary;

public interface IRemoteServiceRegistration {
	public IRemoteServiceReference getReference();
	public void setProperties(Dictionary properties);
	public void unregister();
}
