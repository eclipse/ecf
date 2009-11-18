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

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.rest.IRestParameter;

/**
 * Default parameter serializer.
 *
 */
public class StringParameterSerializer implements IRestParameterSerializer {

	/**
	 * @throws ECFException  
	 */
	public String serializeParameter(IRestParameter parameter, Object callValue) throws ECFException {
		if (callValue == null)
			return parameter.getValue();
		if (callValue instanceof String)
			return (String) callValue;
		return callValue.toString();
	}

}
