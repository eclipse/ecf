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

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;

public class TimeServiceRestID extends RestID {

	private static final long serialVersionUID = 6964783426775839086L;

	public TimeServiceRestID(Namespace namespace, URI uri) {
		super(namespace, uri);
	}

}
