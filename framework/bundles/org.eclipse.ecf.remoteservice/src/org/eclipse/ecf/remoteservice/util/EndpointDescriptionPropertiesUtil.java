/****************************************************************************
 * Copyright (c) 2016 Composent, Inc. and others.
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
package org.eclipse.ecf.remoteservice.util;

import java.util.*;
import org.eclipse.ecf.core.identity.*;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * @since 8.9
 */
public class EndpointDescriptionPropertiesUtil {

	public static String verifyStringProperty(Map<String, Object> properties, String propName) {
		if (properties == null)
			return null;
		Object r = properties.get(propName);
		try {
			return (String) r;
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException("property value is not a String: " + propName); //$NON-NLS-1$
			iae.initCause(e);
			throw iae;
		}
	}

	public static Long verifyLongProperty(Map<String, Object> properties, String propName) {
		if (properties == null)
			return null;
		Object r = properties.get(propName);
		try {
			return (Long) r;
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException("property value is not a Long: " + propName); //$NON-NLS-1$
			iae.initCause(e);
			throw iae;
		}
	}

	public static ID verifyIDProperty(String idNamespace, String idName) {
		if (idName == null)
			return null;
		try {
			return IDUtil.createID(idNamespace, idName);
		} catch (IDCreateException e) {
			return IDFactory.getDefault().createStringID(idName);
		}
	}

	public static List<String> getStringPlusProperty(Map<String, ?> properties, String key) {
		if (properties == null)
			return Collections.EMPTY_LIST;

		Object value = properties.get(key);

		if (value == null)
			return Collections.EMPTY_LIST;

		if (value instanceof String)
			return Collections.singletonList((String) value);

		if (value instanceof String[]) {
			String[] values = (String[]) value;
			List result = new ArrayList(values.length);
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					result.add(values[i]);
				}
			}
			return Collections.unmodifiableList(result);
		}

		if (value instanceof Collection) {
			Collection values = (Collection) value;
			List result = new ArrayList(values.size());
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				Object v = iter.next();
				if (v instanceof String) {
					result.add(v);
				}
			}
			return Collections.unmodifiableList(result);
		}

		return Collections.EMPTY_LIST;
	}

	public static ID[] verifyIDArray(Map<String, Object> properties, String propName, String idNamespace) {
		List<String> idNames = getStringPlusProperty(properties, propName);
		if (idNames.size() == 0)
			return null;
		List<ID> results = new ArrayList();
		for (String idName : idNames) {
			try {
				results.add(IDUtil.createID(idNamespace, idName));
			} catch (IDCreateException e) {
				IllegalArgumentException iae = new IllegalArgumentException("cannot create ID[]: idNamespace=" + idNamespace //$NON-NLS-1$
						+ " idName=" + idName); //$NON-NLS-1$
				iae.initCause(e);
				throw iae;
			}
		}
		return results.toArray(new ID[results.size()]);
	}

	public static Version getPackageVersion(Map<String, Object> endpointDescriptionProperties, String packageName) {
		String key = "endpoint.package.version." + packageName; //$NON-NLS-1$
		Object value = endpointDescriptionProperties.get(key);
		String version;
		try {
			version = (String) value;
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException(key + " property value not a String"); //$NON-NLS-1$
			iae.initCause(e);
			throw iae;
		}
		return Version.parseVersion(version);
	}

	public static List<String> verifyObjectClassProperty(Map<String, Object> endpointDescriptionProperties) {
		Object o = endpointDescriptionProperties.get(Constants.OBJECTCLASS);
		if (!(o instanceof String[]))
			throw new IllegalArgumentException("objectClass value must be of type String[]"); //$NON-NLS-1$
		String[] obClass = (String[]) o;
		if (obClass.length < 1)
			throw new IllegalArgumentException("objectClass is empty"); //$NON-NLS-1$

		for (String intf : obClass) {
			int index = intf.lastIndexOf('.');
			if (index == -1)
				continue;
			String packageName = intf.substring(0, index);
			try {
				getPackageVersion(endpointDescriptionProperties, packageName);
			} catch (IllegalArgumentException e) {
				IllegalArgumentException iae = new IllegalArgumentException("Bad version for package " + packageName); //$NON-NLS-1$
				iae.initCause(e);
				throw iae;
			}
		}
		return Collections.unmodifiableList(Arrays.asList(obClass));
	}

	public static Dictionary createDictionaryFromMap(Map propMap) {
		if (propMap == null)
			return null;
		Dictionary result = new Properties();
		for (Iterator i = propMap.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Object val = propMap.get(key);
			if (key != null && val != null)
				result.put(key, val);
		}
		return result;
	}

}
