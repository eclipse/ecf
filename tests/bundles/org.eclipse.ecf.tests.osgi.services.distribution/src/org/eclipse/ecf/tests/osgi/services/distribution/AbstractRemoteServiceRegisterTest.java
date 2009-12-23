/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution;

import java.util.Properties;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.osgi.framework.ServiceRegistration;

public abstract class AbstractRemoteServiceRegisterTest extends
		AbstractDistributionTest {

	protected static final int REGISTER_WAIT = 2000;

	protected abstract String getServerContainerTypeName();
	
	protected void tearDown() throws Exception {
		super.tearDown();
		IContainer [] containers = getContainerManager().getAllContainers();
		for(int i=0; i < containers.length; i++) {
			containers[i].dispose();
		}
		getContainerManager().removeAllContainers();
		
	}
	
	protected void registerWaitAndUnregister(Properties props) throws Exception {
		// Actually register with default service (IConcatService)
		ServiceRegistration registration = registerDefaultService(props);
		// Wait a while
		Thread.sleep(REGISTER_WAIT);
		// Then unregister
		registration.unregister();
		Thread.sleep(REGISTER_WAIT);
	}

	public void testRegisterOnCreatedServer() throws Exception {
		Properties props = new Properties();
		props.put(SERVICE_EXPORTED_CONFIGS, getServerContainerTypeName());
		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
		registerWaitAndUnregister(props);
	}

	public void testRegisterOnCreatedServerWithIdentity() throws Exception {
		Properties props = new Properties();
		// Set config to the server container name/provider config name (e.g. ecf.generic.server)
		props.put(SERVICE_EXPORTED_CONFIGS, getServerContainerTypeName());
		// set the container factory arguments to the server identity (e.g. ecftcp://localhost:3282/server)
		props.put(SERVICE_EXPORTED_CONTAINER_FACTORY_ARGUMENTS, new String[] { getServerIdentity() } );
		// Set the service exported interfaces to all
		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
		
		registerWaitAndUnregister(props);
	}

	public void testRegisterOnExistingServer() throws Exception {
		// Create server container
		this.server = ContainerFactory.getDefault().createContainer(getServerContainerTypeName(),new Object[] {getServerCreateID()});
		
		Properties props = new Properties();
		props.put(SERVICE_EXPORTED_CONFIGS, getServerContainerTypeName());
		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
		
		registerWaitAndUnregister(props);
	}

	public void testRegisterOnExistingServerWithContainerID() throws Exception {
		// Create server container
		this.server = ContainerFactory.getDefault().createContainer(getServerContainerTypeName(),new Object[] {getServerCreateID()});
		
		Properties props = new Properties();
		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
		props.put(SERVICE_EXPORTED_CONTAINER_ID, getServerCreateID());
		registerWaitAndUnregister(props);
	}

	public void testRegisterOnExistingServerWithIdentity() throws Exception {
		// Create server container
		this.server = ContainerFactory.getDefault().createContainer(getServerContainerTypeName(),getServerIdentity());
		
		Properties props = new Properties();
		// Set config to the server container name/provider config name (e.g. ecf.generic.server)
		props.put(SERVICE_EXPORTED_CONFIGS, getServerContainerTypeName());
		// Set the service exported interfaces to all
		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
		
		registerWaitAndUnregister(props);
	}

}
