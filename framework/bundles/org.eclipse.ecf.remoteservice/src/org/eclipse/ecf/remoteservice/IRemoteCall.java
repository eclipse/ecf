package org.eclipse.ecf.remoteservice;

import java.util.Dictionary;

public interface IRemoteCall {
	public String getMethod();
	public Object [] getParameters();
	public Dictionary getProperties();
	public long getTimeout();
}
