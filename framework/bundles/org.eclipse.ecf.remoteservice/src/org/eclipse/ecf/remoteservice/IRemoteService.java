package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.core.util.AsynchResult;
import org.eclipse.ecf.core.util.ECFException;

public interface IRemoteService {
	public Object callSynch(IRemoteCallable call) throws ECFException;
	public AsynchResult callAsynch(IRemoteCallable call) throws ECFException;
	public void fire(IRemoteCallable call) throws ECFException;
}
