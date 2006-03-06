package org.eclipse.ecf.tutorial.basic;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;

public class Client1 {
	
	protected static final String CONTAINER_TYPE = "ecf.generic.client";
	protected static final String TARGET_SERVER = "ecftcp://localhost:3282/server";
	
	IContainer container = null;
	
	public void createAndConnect() throws ECFException {
		// create container instance from ECF ContainerFactory
		container = ContainerFactory.getDefault().createContainer(CONTAINER_TYPE);
		// create target ID
		ID targetID = IDFactory.getDefault().createID(container.getConnectNamespace(),TARGET_SERVER);
		// connect container to target
		container.connect(targetID, null);
	}
}
