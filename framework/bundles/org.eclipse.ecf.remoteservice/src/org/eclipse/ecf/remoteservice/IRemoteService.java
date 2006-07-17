package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.core.util.AsynchResult;
import org.eclipse.ecf.core.util.ECFException;

public interface IRemoteService {
	public Object callSynch(IRemoteCall call) throws ECFException;
	public AsynchResult callAsynch(IRemoteCall call) throws ECFException;
	public void callAsynch(IRemoteCall call, IRemoteCallListener listener);
	public void fireAsynch(IRemoteCall call) throws ECFException;
}
