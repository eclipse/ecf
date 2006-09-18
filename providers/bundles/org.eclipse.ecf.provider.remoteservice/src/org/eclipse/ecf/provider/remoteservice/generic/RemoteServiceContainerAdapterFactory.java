package org.eclipse.ecf.provider.remoteservice.generic;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;

public class RemoteServiceContainerAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (ISharedObjectContainer.class.isInstance(adaptableObject)) {
			RemoteServiceContainerAdapter rsca = new RemoteServiceContainerAdapter(((ISharedObjectContainer) adaptableObject).getSharedObjectManager());
			return rsca.getDelegate();
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IRemoteServiceContainer.class };
	}

}
