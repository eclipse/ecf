/****************************************************************************
 * Copyright (c) 2021 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.ecf.core.util;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.10
 */
public class SerDTO implements Serializable {

	private static final long serialVersionUID = -1739849704193630352L;
	private String className;
	private Map<String, Object> fields;

	public SerDTO(Object obj) {
		Class<?> clazz = obj.getClass();
		this.className = clazz.getName();
		this.fields = new HashMap<String, Object>();
		for (Field f : clazz.getFields()) {
			final int mod = f.getModifiers();
			if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
				continue;
			Object value = null;
			try {
				value = f.get(obj);
			} catch (Exception e) {
				//
			}
			if (value != null) {
				fields.put(f.getName(), value);
			}
		}
	}

	public String getClassname() {
		return this.className;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public Object readObject(Class<?> clazz) throws IOException {
		Object result = null;
		try {
			result = clazz.getConstructor(new Class[] {}).newInstance();
			for (Field f : clazz.getFields()) {
				Object v = fields.get(f.getName());
				if (v != null) {
					f.setAccessible(true);
					try {
						f.set(result, v);
					} catch (Exception e) {
						// ignore
					}
				}
			}
		} catch (Throwable e) {
			throw new IOException("Unexpected exception reading DTO in SerDTO.readObject: " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
		return result;

	}
}
