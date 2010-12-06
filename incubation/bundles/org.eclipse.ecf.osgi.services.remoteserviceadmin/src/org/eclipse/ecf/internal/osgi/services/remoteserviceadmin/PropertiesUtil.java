/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;

public class PropertiesUtil {

	protected static final List osgiProperties = Arrays
			.asList(new String[] {
					// OSGi properties
					org.osgi.framework.Constants.OBJECTCLASS,
					org.osgi.framework.Constants.SERVICE_ID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
					org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS });

	protected static final List ecfProperties = Arrays.asList(new String[] {
			// ECF properties
			RemoteConstants.DISCOVERY_DEFAULT_SERVICE_NAME_PREFIX,
			RemoteConstants.DISCOVERY_NAMING_AUTHORITY,
			RemoteConstants.DISCOVERY_PROTOCOLS,
			RemoteConstants.DISCOVERY_SCOPE,
			RemoteConstants.DISCOVERY_SERVICE_NAME,
			RemoteConstants.ENDPOINT_CONNECTTARGET_ID,
			RemoteConstants.ENDPOINT_CONNECTTARGET_ID_NAMESPACE,
			RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE,
			RemoteConstants.ENDPOINT_IDFILTER_IDS,
			RemoteConstants.ENDPOINT_IDFILTER_IDARRAY_COUNT,
			RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER,
			RemoteConstants.ENDPOINT_REMOTESERVICE_ID,
			RemoteConstants.SERVICE_EXPORTED_CONTAINER_CONNECT_CONTEXT,
			RemoteConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS,
			RemoteConstants.SERVICE_EXPORTED_CONTAINER_ID,
			RemoteConstants.SERVICE_TYPE });

	public static String verifyStringProperty(Map properties, String propName) {
		Object r = properties.get(propName);
		try {
			return (String) r;
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"property value is not a String: " + propName);
			iae.initCause(e);
			throw iae;
		}
	}

	public static List getStringPlusProperty(Map properties, String key) {
		Object value = properties.get(key);
		if (value == null) {
			return Collections.EMPTY_LIST;
		}

		if (value instanceof String) {
			return Collections.singletonList((String) value);
		}

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
					result.add((String) v);
				}
			}
			return Collections.unmodifiableList(result);
		}

		return Collections.EMPTY_LIST;
	}

	public static boolean isOSGiProperty(String key) {
		if (key == null)
			return false;
		return osgiProperties.contains(key) && !key.startsWith(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_PACKAGE_VERSION_);
	}

	public static boolean isECFProperty(String key) {
		if (key == null)
			return false;
		return ecfProperties.contains(key)
				&& !key.startsWith(RemoteConstants.ENDPOINT_IDFILTER_IDARRAY_NAME_)
				&& !key.startsWith(RemoteConstants.ENDPOINT_IDFILTER_IDARRAY_NAMESPACE_);
	}

	public static boolean isStandardProperty(String key) {
		if (key == null)
			return false;
		return isOSGiProperty(key) || isECFProperty(key);
	}

	public static Map createMapFromDictionary(Dictionary input) {
		if (input == null)
			return null;
		Map result = new HashMap();
		for (Enumeration e = input.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object val = input.get(key);
			result.put(key, val);
		}
		return result;
	}

	public static Dictionary createDictionaryFromMap(Map propMap) {
		if (propMap == null)
			return null;
		Dictionary result = new Properties();
		for (Iterator i = propMap.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Object val = propMap.get(key);
			result.put(key, val);
		}
		return result;
	}

	public static Long getLongWithDefault(Map props, String key, Long def) {
		if (props == null)
			return def;
		Object o = props.get(key);
		if (o instanceof Long)
			return (Long) o;
		if (o instanceof String)
			return Long.valueOf((String) o);
		return def;
	}

	public static String[] getStringArrayWithDefault(
			Map<String, Object> properties, String key, String[] def) {
		if (properties == null)
			return def;
		Object o = properties.get(key);
		if (o instanceof String) {
			return new String[] { (String) o };
		} else if (o instanceof String[]) {
			return (String[]) o;
		} else if (o instanceof List) {
			List l = (List) o;
			return (String[]) l.toArray(new String[l.size()]);
		}
		return def;
	}

	public static String getStringWithDefault(Map props, String key, String def) {
		if (props == null)
			return def;
		Object o = props.get(key);
		if (o == null || (!(o instanceof String)))
			return def;
		return (String) o;
	}

	public static Map<String, Object> getNonECFProperties(
			Map<String, Object> parsedProperties) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (String key : parsedProperties.keySet())
			if (!isECFProperty(key))
				result.put(key, parsedProperties.get(key));
		return result;
	}


}
