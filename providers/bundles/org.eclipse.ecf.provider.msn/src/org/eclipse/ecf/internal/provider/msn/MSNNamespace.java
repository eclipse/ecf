/****************************************************************************
 * Copyright (c) 2006, 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.msn;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;

public class MSNNamespace extends Namespace {

	private static final String SCHEME_IDENTIFIER = "msn";

	public ID createInstance(Object[] parameters) throws IDCreateException {
		Assert.isNotNull(parameters);
		switch (parameters.length) {
		case 1:
			return new MSNID(this, (String) parameters[0]);
		default:
			throw new IDCreateException();
		}
	}

	public String getScheme() {
		return SCHEME_IDENTIFIER;
	}

	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { { String.class }, { String.class, String.class } };
	}

}
