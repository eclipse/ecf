package org.eclipse.ecf.provider.remoteservice.generic;

import java.util.Map;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectAddException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;

public class RemoteServiceContainerDelegate {

	protected ISharedObjectManager sharedObjectManager;

	protected ID sharedObjectID;

	public RemoteServiceContainerDelegate(
			ISharedObjectManager sharedObjectManager) {
		this.sharedObjectManager = sharedObjectManager;
		this.sharedObjectID = createSharedObject();
	}

	protected ID createSharedObject() {
		try {
			return sharedObjectManager.addSharedObject(createSharedObjectID(),
					createSharedObjectInstance(),
					createSharedObjectProperties());
		} catch (SharedObjectAddException e) {
			throw new RuntimeException(
					"Exception adding shared object instance", e);
		}
	}

	protected ISharedObject createSharedObjectInstance() {
		return new RegistrySharedObject();
	}

	protected Map createSharedObjectProperties() {
		return null;
	}

	protected ID createSharedObjectID() {
		try {
			return IDFactory.getDefault().createStringID(
					RegistrySharedObject.class.getName() + ".registry");
		} catch (IDInstantiationException e) {
			throw new RuntimeException("Exception creating new ID", e);
		}
	}

	public Object getDelegate() {
		return sharedObjectManager.getSharedObject(sharedObjectID);
	}
}
