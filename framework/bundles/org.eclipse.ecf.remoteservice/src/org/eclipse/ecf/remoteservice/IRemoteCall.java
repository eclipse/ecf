package org.eclipse.ecf.remoteservice;

public interface IRemoteCall {
	public String getMethod();
	public Object [] getParameters();
	public long getTimeout();
}
