/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.util;

public class AbstractFactory {
	@SuppressWarnings("rawtypes")
	public static Class[] getClassesForTypes(String[] argTypes, Object[] args, ClassLoader cl)
			throws ClassNotFoundException {
		Class clazzes[] = null;
		if (args == null || args.length == 0)
			clazzes = new Class[0];
		else if (argTypes != null) {
			clazzes = new Class[argTypes.length];
			for (int i = 0; i < argTypes.length; i++) {
				clazzes[i] = Class.forName(argTypes[i], true, cl);
			}
		} else {
			clazzes = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null)
					clazzes[i] = null;
				else
					clazzes[i] = args[i].getClass();
			}
		}
		return clazzes;
	}
}