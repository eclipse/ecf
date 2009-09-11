/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.remoteservice.rest;

import java.net.URL;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;

/**
 * This class is omnly used for creating instances of {@link RestContainer}.
 */
public class RestContainerInstantiatior implements IContainerInstantiator {

	public IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException {
		if(!description.getName().equals(RestContainer.NAME))
			throw new ContainerCreateException();
		if(parameters == null)
			throw new ContainerCreateException();
		if(parameters.length > 0 && (parameters[0] instanceof URL)) {
			URL baseUrl = (URL) parameters[0];
			RestNamespace namespace = new RestNamespace(RestNamespace.NAME, description.getDescription());
			RestID id = new RestID(namespace, baseUrl);
			return new RestContainer(id);
		}
		if(parameters.length > 0 && (parameters[0] instanceof ID)) {
			return new RestContainer((ID) parameters[0]);
		}
		throw new ContainerCreateException();
		
	}

	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		
		return null;
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}

	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {		
		if(!description.getName().equals(RestContainer.NAME))
			throw new IllegalArgumentException("Description must be "+RestContainer.NAME);
		return new Class[][]{{ URL.class }};
	}

}
