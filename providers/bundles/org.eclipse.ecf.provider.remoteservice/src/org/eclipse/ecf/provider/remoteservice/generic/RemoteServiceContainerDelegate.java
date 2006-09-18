package org.eclipse.ecf.provider.remoteservice.generic;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectContainerDelegate;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;

public class RemoteServiceContainerDelegate extends
		SharedObjectContainerDelegate {

	private static final String REGISTRY_ID = RemoteServiceContainer.class.getName() + ".registry";

	public RemoteServiceContainerDelegate(
			ISharedObjectManager sharedObjectManager) {
		super(sharedObjectManager);
	}

	protected ISharedObject createSharedObjectInstance() {
		return new RegistrySharedObject();
	}

	protected ID createSharedObjectID()	{
		try {
			return IDFactory.getDefault().createStringID(REGISTRY_ID);
		} catch (IDInstantiationException e) {
			throw new RuntimeException("Exception creating new ID", e);
		}
	}

}
