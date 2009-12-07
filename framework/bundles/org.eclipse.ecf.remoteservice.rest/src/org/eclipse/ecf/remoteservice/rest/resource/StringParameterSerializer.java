/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.resource;

import org.eclipse.ecf.remoteservice.rest.IRestParameter;
import org.eclipse.ecf.remoteservice.rest.RestException;

/**
 * Default parameter serializer.
 *
 */
public class StringParameterSerializer implements IRestParameterSerializer {

	/**
	 * @throws RestException  
	 */
	public String serializeParameter(IRestParameter parameter, Object callValue) throws RestException {
		if (callValue == null)
			return parameter.getValue();
		if (callValue instanceof String)
			return (String) callValue;
		return callValue.toString();
	}

}
