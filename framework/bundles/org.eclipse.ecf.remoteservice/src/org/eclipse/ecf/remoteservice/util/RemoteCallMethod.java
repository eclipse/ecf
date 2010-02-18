/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.remoteservice.util;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import org.eclipse.ecf.internal.remoteservice.Messages;
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
				throw new NotSerializableException(Messages.RemoteCallMethod_EXCEPTION_PARAMETER_NOT_SERIALIZABLE);
		}
	}

	public static void checkForTypeMatch(Method method, Object[] parameters) {
		if (parameters == null)
			return;
		final Class[] parameterTypes = method.getParameterTypes();
		// Check for same length
		if (parameterTypes.length != parameters.length)
			throw new IllegalArgumentException(Messages.RemoteCallMethod_EXCEPTION_ARGS_NOT_RIGHT_LENGTH);
		for (int i = 0; i < parameterTypes.length; i++) {
			if (!parameterTypes[i].isInstance(parameters[i]))
				throw new IllegalArgumentException(Messages.RemoteCallMethod_EXCEPTION_ARG_WRONG_TYPE);
		}
	}

	/**
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