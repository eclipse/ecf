/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import java.io.NotSerializableException;
import org.eclipse.ecf.remoteservice.IRemoteCall;

/**
 * Parameter serializer for String parameters.
 * 
 * @since 4.0
 *
 */
public class StringParameterSerializer extends AbstractParameterSerializer implements IRemoteCallParameterSerializer {

	/**
	 * @param uri uri
	 * @param call call
	 * @param callable callable
	 * @param paramDefault parameter default
	 * @param paramToSerialize the parameter to serialize
	 * @return IRemoteCallParameter created as result of serialization
	 * @throws NotSerializableException if input parameters cannot be serialized
	 */
	public IRemoteCallParameter serializeParameter(String uri, IRemoteCall call, IRemoteCallable callable, IRemoteCallParameter paramDefault, Object paramToSerialize) throws NotSerializableException {
		if (paramToSerialize == null) {
			Object defaultValue = paramDefault.getValue();
			if (defaultValue instanceof String)
				return new RemoteCallParameter(paramDefault.getName(), defaultValue);
		}
		return new RemoteCallParameter(paramDefault.getName(), paramToSerialize.toString());
	}

}
