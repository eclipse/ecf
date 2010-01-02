/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import java.io.NotSerializableException;
import org.eclipse.ecf.remoteservice.IRemoteCall;

/**
 * Default parameter serializer.
 * @since 3.3
 *
 */
public class StringParameterSerializer implements IRemoteCallParameterSerializer {

	/**
	 * @throws NotSerializableException  
	 */
	public String serializeParameter(String uri, IRemoteCall call, IRemoteCallable callable, Object param, IRemoteCallParameter defaultParam) throws NotSerializableException {
		if (param == null) {
			Object defaultValue = defaultParam.getValue();
			if (defaultValue == null)
				return null;
			if (defaultValue instanceof String)
				return (String) defaultValue;
			return defaultValue.toString();
		}
		if (param instanceof String)
			return (String) param;
		return param.toString();
	}

}
