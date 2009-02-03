/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.discovery;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.osgi.service.discovery.ServiceEndpointDescription;

public class ServiceEndpointDescriptionImpl implements
		ServiceEndpointDescription {

	private final IServiceInfo serviceInfo;

	public ServiceEndpointDescriptionImpl(IServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	public String getEndpointID() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEndpointInterfaceName(String interfaceName) {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getPropertyKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getProvidedInterfaces() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVersion(String interfaceName) {
		// TODO Auto-generated method stub
		return null;
	}

}
