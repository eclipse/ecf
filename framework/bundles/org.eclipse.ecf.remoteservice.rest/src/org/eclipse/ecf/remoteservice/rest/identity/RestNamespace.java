/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.remoteservice.rest.identity;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.rest.RestContainer;

/**
 * This class represents a {@link Namespace} for {@link RestContainer}s.
 */
public class RestNamespace extends Namespace {
	
	private static final long serialVersionUID = -398861350452016954L;
	
	/**
	 * The name of this namespace.
	 */
	public static final String NAME = "ecf.rest.namespace";
	
	/**
	 * The scheme of this namespace.
	 */
	public static final String SCHEME = "rest";
	

	public RestNamespace() {
	}

	public RestNamespace(String name, String desc) {
		super(name, desc);
	}	

	/**
	 * Creates an instance of an {@link RestID}. The parameters must contain specific information.
	 * 
	 * First it should contain a String which represents the {@link RestID#baseUrl}.
	 * Additional it could contain a ServiceId and/or a ContainerId.
	 * 
	 * @param parameters a collection of attributes to call the right constructor on {@link RestID}.
	 * @return an instance of {@link RestID}. Will not be <code>null</code>.
	 */
	public ID createInstance(Object[] parameters) throws IDCreateException {
		URL url = null;
		if(parameters[0] instanceof String) {
			try {
				url = new URL((String) parameters[0]);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(e.getLocalizedMessage());
			}
		} else if((parameters[0] instanceof URL)) {
			url = (URL) parameters[0];
		} else 
			throw new IllegalArgumentException("the first parameter must be transformable to an URL");
		if(parameters.length == 2 && parameters[1] instanceof Long)
			return new RestID(this, url, (Long)parameters[1] );
		if( parameters.length == 3 && parameters[1] instanceof ID && parameters[2] instanceof Long)
			return new RestID(this, url, (ID)parameters[1], (Long)parameters[2] );
		return new RestID(this, url );
	}

	public String getScheme() {
		return SCHEME;
	}
	
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { { URL.class } };
	}

}
