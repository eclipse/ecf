package org.eclipse.ecf.remoteservice;

import java.util.Dictionary;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

public interface IRemoteServiceContainer {

	public void addRemoteServiceListener(IRemoteServiceListener listener);

	public void removeRemoteServiceListener(IRemoteServiceListener listener);

	public IRemoteServiceRegistration registerRemoteService(String[] clazzes,
			Object service, Dictionary properties) throws ECFException;

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter,
			String clazz, String filter) throws ECFException;

	public IRemoteService getRemoteService(IRemoteServiceReference ref)
			throws ECFException;

	public boolean ungetRemoteService(IRemoteServiceReference ref);

}
