package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.core.identity.ID;

public interface IRemoteServiceReference {
	public ID getRemoteContainerID();
	public Object getProperty(String key);
	public String [] getPropertyKeys();
}
