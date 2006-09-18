package org.eclipse.ecf.provider.remoteservice.generic;

import java.util.Dictionary;

import org.eclipse.ecf.core.ISharedObjectContainerConfig;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.provider.generic.TCPClientSOContainer;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;

public class RemoteServiceContainer extends TCPClientSOContainer
		implements IRemoteServiceContainer {

	protected RemoteServiceContainerDelegate delegate;
	protected IRemoteServiceContainer registry;
	
	protected void createRegistry() {
		delegate = new RemoteServiceContainerDelegate(getSharedObjectManager());
		registry = (IRemoteServiceContainer) delegate.getDelegate();
	}

	public RemoteServiceContainer(ISharedObjectContainerConfig config) {
		super(config);
		createRegistry();
	}

	public RemoteServiceContainer(ISharedObjectContainerConfig config,
			int ka) {
		super(config, ka);
		createRegistry();
	}

	public void dispose() {
		super.dispose();
		delegate.dispose();
		delegate = null;
		registry = null;
	}
	public void addRemoteServiceListener(IRemoteServiceListener listener) {
		registry.addRemoteServiceListener(listener);
	}

	public IRemoteService getRemoteService(IRemoteServiceReference ref) {
		return registry.getRemoteService(ref);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter,
			String clazz, String filter) {
		return registry.getRemoteServiceReferences(idFilter,clazz,filter);
	}

	public IRemoteServiceRegistration registerRemoteService(String[] clazzes,
			Object service, Dictionary properties) {
		return registry.registerRemoteService(clazzes,service,properties);
	}

	public void removeRemoteServiceListener(IRemoteServiceListener listener) {
		registry.removeRemoteServiceListener(listener);
	}

	public boolean ungetRemoteService(IRemoteServiceReference ref) {
		return registry.ungetRemoteService(ref);
	}
}
