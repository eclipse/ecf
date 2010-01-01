/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.util;

import java.io.NotSerializableException;
import org.eclipse.ecf.remoteservice.IRemoteCallParameter;

/**
 * Default parameter serializer.
 * @since 3.3
 *
 */
public class RemoteCallParameterStringSerializer implements IRemoteCallParameterSerializer {

	/**
	 * @throws NotSerializableException  
	 */
	public String serializeRemoteCallParameter(IRemoteCallParameter parameter, Object callValue) throws NotSerializableException {
		if (callValue == null) {
			Object defaultValue = parameter.getValue();
			if (defaultValue == null)
				return null;
			if (defaultValue instanceof String)
				return (String) defaultValue;
			return defaultValue.toString();
		}
		if (callValue instanceof String)
			return (String) callValue;
		return callValue.toString();
	}

}
