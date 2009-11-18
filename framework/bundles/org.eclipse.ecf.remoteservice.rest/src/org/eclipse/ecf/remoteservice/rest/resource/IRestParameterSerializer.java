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
 * Instances of this object are responsible for serializing rest parameters.
 *
 */
public interface IRestParameterSerializer {

	/**
	 * Serialize the callValue for the given parameter.
	 * 
	 * @param parameter the rest parameter associated with the callValue.  Will no be <code>null</code>.
	 * @param callValue the rest parameter callValue to serialize.  May be <code>null</code>.
	 * @return String result of parameter serialization.
	 * @throws ECFException if callValue cannot be serialized.
	 */
	public String serializeParameter(IRestParameter parameter, Object callValue) throws ECFException;

}
