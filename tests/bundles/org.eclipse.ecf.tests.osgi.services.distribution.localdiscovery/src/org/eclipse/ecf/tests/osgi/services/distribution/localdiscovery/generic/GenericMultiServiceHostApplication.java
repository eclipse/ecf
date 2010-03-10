/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution.localdiscovery.generic;

import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.tests.internal.osgi.services.distribution.localdiscovery.Activator;
import org.eclipse.ecf.tests.osgi.services.distribution.TestService1;
import org.eclipse.ecf.tests.osgi.services.distribution.TestServiceInterface1;
import org.eclipse.ecf.tests.remoteservice.AbstractConcatHostApplication;
import org.eclipse.ecf.tests.remoteservice.IConcatService;
import org.eclipse.equinox.app.IApplicationContext;

public class GenericMultiServiceHostApplication extends AbstractConcatHostApplication implements IDistributionConstants {

	private String containerId = "ecftcp://localhost:9655/server";
	private String containerType = "ecf.generic.server";
	
	public Object start(IApplicationContext context) throws Exception {
		// First, create container of appropriate type
		IContainer container = createContainer(containerId);
		// Then, from container create remote service container
		rsContainer = createRemoteServiceContainer(container);
		// Now register IConcatService remote service
		Activator.getDefault().getContext().registerService(IConcatService.class.getName(), createConcatService(), createRemoteServiceProperties(containerType,containerId));
		System.out.println("registered concat remote service");
		// Register ITestService
		Activator.getDefault().getContext().registerService(TestServiceInterface1.class.getName(), createTestService(), createRemoteServiceProperties(containerType,containerId));
		System.out.println("registered testserviceinterface1");
		
		printStarted();
		// And wait until we're explicitly stopped.
		synchronized (this) {
			while (!done)
				wait();
		}
		return new Integer(0);
	}

	protected String getContainerId() {
		return containerId;
	}
	
	protected Dictionary createRemoteServiceProperties(String containerType, String containerId) {
		Dictionary props = new Properties();
		// add OSGi service property indicated export of all interfaces exposed by service (wildcard)
		props.put(SERVICE_EXPORTED_INTERFACES, SERVICE_EXPORTED_INTERFACES_WILDCARD);
		// add OSGi service property specifying config
		props.put(SERVICE_EXPORTED_CONFIGS, containerType);
		// add ECF service property specifying container factory args
		props.put(SERVICE_EXPORTED_CONTAINER_FACTORY_ARGUMENTS, containerId);
		return props;
	}
	
	protected Object createTestService() {
		return new TestService1();
	}
	
	protected Object createConcatService() {
		return new IConcatService() {
			public String concat(String string1, String string2) {
				final String result = string1.concat(string2);
				System.out.println("SERVICE.concat(" + string1 + "," + string2
						+ ") returning " + result);
				return string1.concat(string2);
			}
		};
	}

	protected String getContainerType() {
		return containerType;
	}


}
