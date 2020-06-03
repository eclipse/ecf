/****************************************************************************
 * Copyright (c) 2008 Remy Chi Jian Suen and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservices.ui;

import java.lang.reflect.Method;

class RemoteMethod {

	private final Method method;
	private final Parameter[] parameters;
	private final Class[] parameterTypes;

	RemoteMethod(Method method) {
		this.method = method;
		parameterTypes = method.getParameterTypes();
		parameters = new Parameter[parameterTypes.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = new Parameter(parameterTypes[i]);
		}
	}

	public String[] getParameterTypes() {
		final String[] types = new String[parameterTypes.length];
		for (int i = 0; i < types.length; i++) {
			final String name = parameterTypes[i].getName();
			if (name.charAt(0) == 'j') {
				types[i] = "String"; //$NON-NLS-1$
			} else {
				types[i] = name;
			}
		}
		return types;
	}

	Method getMethod() {
		return method;
	}

	Parameter[] getParameters() {
		return parameters;
	}

	public String getReturnType() {
		String name = method.getReturnType().getName();
		// Fix array types
		if (name.startsWith("[L")) { //$NON-NLS-1$
			name = name.substring(2, name.length() - 1).concat("[]"); //$NON-NLS-1$
		}
		final int index = name.lastIndexOf('.');
		if (index != -1) {
			name = name.substring(index + 1);
		}
		name = name.replace('$', '.');
		return name;
	}

	public String getSignature() {
		final StringBuffer buffer = new StringBuffer(method.getName());
		synchronized (buffer) {
			buffer.append('(');
			final String[] types = getParameterTypes();
			if (types.length != 0) {
				for (int i = 0; i < types.length; i++) {
					buffer.append(types[i]).append(", "); //$NON-NLS-1$
				}
				buffer.delete(buffer.length() - 2, buffer.length());
			}
			buffer.append(')');
			return buffer.toString();
		}
	}

	class Parameter {

		private final Class parameter;
		private String argument = ""; //$NON-NLS-1$

		Parameter(Class parameter) {
			this.parameter = parameter;
		}

		void setArgument(String argument) {
			this.argument = argument;
		}

		String getArgument() {
			return argument;
		}

		Class getParameter() {
			return parameter;
		}

	}

}
