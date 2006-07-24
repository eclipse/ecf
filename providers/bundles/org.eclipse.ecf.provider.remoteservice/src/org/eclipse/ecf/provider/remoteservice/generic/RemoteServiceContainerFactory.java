package org.eclipse.ecf.provider.remoteservice.generic;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerInstantiationException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.ISharedObjectContainerConfig;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.IContainerInstantiator;

public class RemoteServiceContainerFactory implements
		IContainerInstantiator {

	public IContainer createInstance(ContainerTypeDescription description,
			Class[] argTypes, Object[] args)
			throws ContainerInstantiationException {
		try {
			final ID newID = IDFactory.getDefault().createGUID();
			return new RemoteServiceContainer(new ISharedObjectContainerConfig() {
				public Object getAdapter(Class clazz) {
					return null;
				}
				public Map getProperties() {
					return new HashMap();
				}
				public ID getID() {
					return newID;
				}});
		} catch (Exception e) {
			throw new ContainerInstantiationException("Exception creating GenericRemoteServiceContainer",e);
		}				
		
	}

}
