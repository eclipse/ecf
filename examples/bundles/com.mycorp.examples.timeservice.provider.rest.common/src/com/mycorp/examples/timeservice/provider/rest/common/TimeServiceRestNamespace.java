/*******************************************************************************
* Copyright (c) 2013 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package com.mycorp.examples.timeservice.provider.rest.common;

import java.net.URI;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;

public class TimeServiceRestNamespace extends RestNamespace {

	public static final String NAME = "com.mycorp.examples.timeservice.provider.rest.namespace";
	
	private static final long serialVersionUID = -3632048418135041788L;

	@Override
	public ID createInstance(Object[] parameters) throws IDCreateException {
		try {
			String uriString = getInitStringFromExternalForm(parameters);
			if (uriString == null)
				uriString = (String) parameters[0];
			return new TimeServiceRestID(this, URI.create(uriString));
		} catch (Exception e) {
			throw new IDCreateException("Could not create TimeServiceRestID", e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { { String.class } };
	}
}
