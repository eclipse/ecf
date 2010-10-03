/*******************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util.reflection;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @since 2.2
 *
 */
public class ClassUtil {

	/**
	 * @param aClass The Class providing method under question (Must not be null)
	 * @param aMethodName The method name to search for (Must not be null)
	 * @param someParameterTypes Method arguments (May be null or parameters)
	 * @return A match. If more than one method matched (due to overloading) an arbitrary match is taken
	 * @throws NoSuchMethodException If a match cannot be found
	 */
	public static Method getMethod(final Class aClass, String aMethodName, final Class[] someParameterTypes) throws NoSuchMethodException {
		// no args makes matching simple
		if (someParameterTypes == null || someParameterTypes.length == 0) {
			return aClass.getMethod(aMethodName, (Class[]) null);
		}
		return getMethod(aClass.getMethods(), aMethodName, someParameterTypes);
	}

	/**
	 * @param aClass The Class providing method under question (Must not be null)
	 * @param aMethodName The method name to search for (Must not be null)
	 * @param someParameterTypes Method arguments (May be null or parameters)
	 * @return A match. If more than one method matched (due to overloading) an arbitrary match is taken
	 * @throws NoSuchMethodException If a match cannot be found
	 */
	public static Method getDeclaredMethod(final Class aClass, String aMethodName, final Class[] someParameterTypes) throws NoSuchMethodException {
		// no args makes matching simple
		if (someParameterTypes == null || someParameterTypes.length == 0) {
			return aClass.getDeclaredMethod(aMethodName, (Class[]) null);
		}
		return getMethod(aClass.getDeclaredMethods(), aMethodName, someParameterTypes);
	}

	private static Method getMethod(final Method[] candidates, String aMethodName, final Class[] someParameterTypes) throws NoSuchMethodException {
		// match parameters to determine callee
		final int parameterCount = someParameterTypes.length;
		aMethodName = aMethodName.intern();

		OUTER: for (int i = 0; i < candidates.length; i++) {
			Method candidate = candidates[i];
			String candidateMethodName = candidate.getName().intern();
			Class[] candidateParameterTypes = candidate.getParameterTypes();
			int candidateParameterCount = candidateParameterTypes.length;
			if (candidateParameterCount == parameterCount && aMethodName == candidateMethodName) {
				for (int j = 0; j < candidateParameterCount; j++) {
					Class clazzA = candidateParameterTypes[j];
					Class clazzB = someParameterTypes[j];
					// clazzA must be non-null, but clazzB could be null (null given as parameter value)
					// so in that case we consider it a match and continue
					if (!(clazzB == null || clazzA.isAssignableFrom(clazzB))) {
						continue OUTER;
					}
				}
				return candidate;
			}
		}
		// if no match has been found, fail with NSME
		throw new NoSuchMethodException("No such method: " + aMethodName + "(" + Arrays.asList(someParameterTypes) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
