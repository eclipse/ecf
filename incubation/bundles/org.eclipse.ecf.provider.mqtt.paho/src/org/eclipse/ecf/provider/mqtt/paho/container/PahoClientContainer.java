package org.eclipse.ecf.provider.mqtt.paho.container;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.generic.ClientSOContainer;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoNamespace;

public class PahoClientContainer extends ClientSOContainer {

	public PahoClientContainer(ISharedObjectContainerConfig config) {
		super(config);
	}

	@Override
	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(PahoNamespace.NAME);
	}

	@Override
	protected ISynchAsynchConnection createConnection(ID targetID, Object data)
			throws ConnectionCreateException {
		// XXX todo
		return null;
	}

}
