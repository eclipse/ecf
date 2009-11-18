/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import java.util.*;
import org.eclipse.core.runtime.Assert;

/**
 * Factory for creating {@link IRestParameter} instances.
 */
public class RestParameterFactory {

	public static IRestParameter[] createParameters(String[] names, String[] values) {
		Assert.isNotNull(names);
		Assert.isNotNull(values);
		Assert.isTrue(names.length == values.length);
		List result = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			result.add(new RestParameter(names[i], values[i]));
		}
		return (IRestParameter[]) result.toArray(new IRestParameter[] {});
	}

	public static IRestParameter[] createParameters(String name, String value) {
		return createParameters(new String[] {name}, new String[] {value});
	}

	public static IRestParameter[] createParameters(String name1, String value1, String name2, String value2) {
		return createParameters(new String[] {name1, name2}, new String[] {value1, value2});
	}

	public static IRestParameter[] createParameters(String name1, String value1, String name2, String value2, String name3, String value3) {
		return createParameters(new String[] {name1, name2, name3}, new String[] {value1, value2, value3});
	}

	public static IRestParameter[] createParameters(Map nameValues) {
		List names = new ArrayList();
		List values = new ArrayList();
		for (Iterator i = nameValues.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Object val = nameValues.get(key);
			if (key instanceof String && val instanceof String) {
				names.add(key);
				values.add(val);
			}
		}
		return createParameters((String[]) names.toArray(new String[] {}), (String[]) values.toArray(new String[] {}));
	}

}
