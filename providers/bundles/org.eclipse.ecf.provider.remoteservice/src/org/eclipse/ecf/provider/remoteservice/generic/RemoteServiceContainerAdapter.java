package org.eclipse.ecf.provider.remoteservice.generic;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.AbstractSharedObjectContainerAdapter;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;

public class RemoteServiceContainerAdapter extends
		AbstractSharedObjectContainerAdapter {

	private static final String REGISTRY_ID = RegistrySharedObject.class.getName() + ".registry";

	public RemoteServiceContainerAdapter(
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
