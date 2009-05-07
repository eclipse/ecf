/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.remoteservice.r_osgi;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;
import org.eclipse.ecf.tests.remoteservice.IConcatService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class ROsgiConcatServerApplication implements IApplication {

	IRemoteServiceContainer rsContainer;
	boolean done = false;
	
	public Object start(IApplicationContext context) throws Exception {

		IContainer container = ContainerFactory.getDefault().createContainer(
				R_OSGi.CLIENT_CONTAINER_NAME);
		rsContainer = new RemoteServiceContainer(container,
				(IRemoteServiceContainerAdapter) container
						.getAdapter(IRemoteServiceContainerAdapter.class));

		rsContainer.getContainerAdapter().registerRemoteService(
				new String[] { IConcatService.class.getName() },
				createService(), null);
		
		synchronized (this) {
			while (!done) wait();
		}
		return new Integer(0);
	}

	protected Object createService() {
		return new IConcatService() {
			public String concat(String string1, String string2) {
				final String result = string1.concat(string2);
				System.out.println("SERVICE.concat(" + string1 + "," + string2
						+ ") returning " + result);
				return string1.concat(string2);
			}
		};
	}

	public void stop() {
		rsContainer.getContainer().disconnect();
		rsContainer.getContainer().dispose();
		((IContainerManager) ContainerFactory.getDefault()).removeAllContainers();
		done = true;
		notifyAll();
	}

}
