/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.remoteservice.util;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.RemoteCall;

/**
 * Implementation of IRemoteCall based upon Method.
 * 
 */
public class RemoteCallMethod extends RemoteCall implements IRemoteCall {

	protected static final Object[] EMPTY_PARAMETERS = {};

	public static void checkSerializable(Object[] parameters) throws NotSerializableException {
		if (parameters == null)
			return;
		for (int i = 0; i < parameters.length; i++) {
			if (!(parameters instanceof Serializable))
				throw new NotSerializableException("Remote method call parameter-" + i + " not serializable"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static void checkForTypeMatch(Method method, Object[] parameters) {
		if (parameters == null)
			return;
		final Class[] parameterTypes = method.getParameterTypes();
		// Check for same length
		if (parameterTypes.length != parameters.length)
			throw new IllegalArgumentException("parameter types length=" + parameterTypes.length + " must be equal to parameters length=" + parameters.length); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < parameterTypes.length; i++) {
			if (!parameterTypes[i].isInstance(parameters[i]))
				throw new IllegalArgumentException("parameter-" + i + " is of improper type"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @param method method
	 * @param parameters parameters
	 * @throws NotSerializableException if given parameters cannot be serialized
	 * @since 4.0
	 */
	public void setParameters(Method method, Object[] parameters) throws NotSerializableException {
		checkSerializable(parameters);
		checkForTypeMatch(method, parameters);
		this.parameters = parameters;
	}

	public RemoteCallMethod(Method method, Object[] parameters, long timeout) throws NotSerializableException {
		super(method.getName(), null, timeout);
		setParameters(method, parameters);
	}

	public RemoteCallMethod(Method method, Object[] parameters) throws NotSerializableException {
		this(method, parameters, DEFAULT_TIMEOUT);
	}

	public RemoteCallMethod(Method method, long timeout) {
		super(method.getName(), null, timeout);
	}

	public RemoteCallMethod(Method method) {
		this(method, DEFAULT_TIMEOUT);
	}

}