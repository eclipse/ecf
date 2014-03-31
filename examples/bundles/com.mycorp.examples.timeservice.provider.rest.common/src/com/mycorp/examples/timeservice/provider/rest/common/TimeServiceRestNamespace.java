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
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;

public class TimeServiceRestNamespace extends RestNamespace {

	public static final String NAME = "com.mycorp.examples.timeservice.provider.rest.namespace";
	
	private static final long serialVersionUID = -3632048418135041788L;

	public TimeServiceRestNamespace() {
		super(NAME,"Time Service REST Namespace");
	}
	
	@Override
	public ID createInstance(Object[] parameters) throws IDCreateException {
		return new TimeServiceRestID(this, URI.create((String) parameters[0]));
	}

	/**
	 * @since 2.0
	 */
	public static class TimeServiceRestID extends RestID {

		private static final long serialVersionUID = 688293496962799572L;

		public TimeServiceRestID(Namespace namespace, URI uri) {
			super(namespace, uri);
		}
	}

}
