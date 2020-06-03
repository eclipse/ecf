/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.remoteservice.generic;


import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.remoteservice.AbstractConcatHostApplication;
import org.eclipse.ecf.tests.remoteservice.Activator;

public class GenericConcatHostApplication extends
		AbstractConcatHostApplication {

	protected IContainer createContainer() throws ContainerCreateException {
		return Activator.getDefault().getContainerManager()
				.getContainerFactory().createContainer(getContainerType(),IDFactory.getDefault().createStringID(Generic.HOST_CONTAINER_ENDPOINT_ID));
	}

	public String getContainerType() {
		return Generic.HOST_CONTAINER_TYPE;
	}
	
	

}
