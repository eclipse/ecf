/****************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others.
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
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
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
import java.util.TreeMap;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class PropertiesUtil {

	protected static final List osgiProperties = Arrays.asList(new String[] {
			// OSGi properties
			org.osgi.framework.Constants.OBJECTCLASS, org.osgi.framework.Constants.SERVICE_ID,
			// Adding these for bug
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433799
			// Adding them as strings because old version of Constants class
			// (pre R6)
			// Don't have them
			"service.bundleid", //$NON-NLS-1$
			"service.scope", //$NON-NLS-1$
			"component.id", //$NON-NLS-1$
			"component.name", //$NON-NLS-1$
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
			org.eclipse.ecf.remoteservice.Constants.OBJECTCLASS, org.eclipse.ecf.remoteservice.Constants.SERVICE_ID,
			RemoteConstants.ENDPOINT_CONNECTTARGET_ID, RemoteConstants.ENDPOINT_ID,
			RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE, RemoteConstants.ENDPOINT_TIMESTAMP,
			RemoteConstants.ENDPOINT_IDFILTER_IDS, RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER,
			RemoteConstants.SERVICE_IMPORTED_VALUETYPE });

	public static void testSerializable(Object value) throws Exception {
		new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(value);
	}

	public static String verifyStringProperty(Map properties, String propName) {
		Object r = properties.get(propName);
		try {
			return (String) r;
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException("property value is not a String: " + propName); //$NON-NLS-1$
			iae.initCause(e);
			throw iae;
		}
	}

	public static Object convertToStringPlusValue(List<String> values) {
		if (values == null)
			return null;
		int valuesSize = values.size();
		switch (valuesSize) {
		case 0:
			return null;
		case 1:
			return values.get(0);
		default:
			return values.toArray(new String[valuesSize]);
		}
	}

	public static String[] getStringArrayFromPropertyValue(Object value) {
		if (value == null)
			return null;
		else if (value instanceof String)
			return new String[] { (String) value };
		else if (value instanceof String[])
			return (String[]) value;
		else if (value instanceof Collection)
			return (String[]) ((Collection) value).toArray(new String[] {});
		else
			return null;
	}

	public static String[] getExportedInterfaces(ServiceReference serviceReference,
			Map<String, ?> overridingProperties) {
		Object overridingPropValue = overridingProperties
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES);
		if (overridingPropValue != null)
			return getExportedInterfaces(serviceReference, overridingPropValue);
		return getExportedInterfaces(serviceReference);
	}

	public static String[] getMatchingInterfaces(String[] origin, Object propValue) {
		if (propValue == null || origin == null)
			return null;
		boolean wildcard = propValue.equals("*"); //$NON-NLS-1$
		if (wildcard)
			return origin;
		else {
			final String[] stringArrayValue = getStringArrayFromPropertyValue(propValue);
			if (stringArrayValue == null)
				return null;
			else if (stringArrayValue.length == 1 && stringArrayValue[0].equals("*")) { //$NON-NLS-1$
				// this will support the idiom: new String[] { "*" }
				return origin;
			} else {
				List<String> originList = Arrays.asList(origin);
				for(int i=0; i < origin.length-1; i++) {
					if (!originList.contains(stringArrayValue[i])) {
						throw new IllegalArgumentException("ObjectClass="+originList+" does not contain interface="+stringArrayValue[i]); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			return stringArrayValue;
		}
	}

	private static String[] getExportedInterfaces(ServiceReference serviceReference, Object propValue) {
		if (propValue == null)
			return null;
		String[] objectClass = (String[]) serviceReference.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
		return getMatchingInterfaces(objectClass, propValue);
	}

	public static String[] getExportedInterfaces(ServiceReference serviceReference) {
		return getExportedInterfaces(serviceReference, serviceReference
				.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES));
	}

	public static String[] getServiceIntents(ServiceReference serviceReference, Map overridingProperties) {
		List results = new ArrayList();

		String[] intents = getStringArrayFromPropertyValue(
				overridingProperties.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS));
		if (intents == null) {
			intents = getStringArrayFromPropertyValue(
					serviceReference.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS));
		}
		if (intents != null)
			results.addAll(Arrays.asList(intents));

		String[] exportedIntents = getStringArrayFromPropertyValue(
				overridingProperties.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS));
		if (exportedIntents == null) {
			exportedIntents = getStringArrayFromPropertyValue(serviceReference
					.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS));
		}
		if (exportedIntents != null)
			results.addAll(Arrays.asList(exportedIntents));

		String[] extraIntents = getStringArrayFromPropertyValue(overridingProperties
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
		if (extraIntents == null) {
			extraIntents = getStringArrayFromPropertyValue(serviceReference
					.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
		}
		if (extraIntents != null)
			results.addAll(Arrays.asList(extraIntents));

		if (results.size() == 0)
			return null;
		return (String[]) results.toArray(new String[results.size()]);
	}

	public static List getStringPlusProperty(Map properties, String key) {
		return org.eclipse.ecf.remoteservice.util.EndpointDescriptionPropertiesUtil.getStringPlusProperty(properties,
				key);
	}

	public static Object getPropertyValue(ServiceReference serviceReference, String key) {
		return (serviceReference == null) ? null : serviceReference.getProperty(key);
	}

	public static Object getPropertyValue(ServiceReference serviceReference, Map<String, Object> overridingProperties,
			String key) {
		Object result = null;
		if (overridingProperties != null)
			result = overridingProperties.get(key);
		return (result != null) ? result : getPropertyValue(serviceReference, key);
	}

	public static boolean isOSGiProperty(String key) {
		return osgiProperties.contains(key)
				|| key.startsWith(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_PACKAGE_VERSION_);
	}

	public static boolean isECFProperty(String key) {
		return ecfProperties.contains(key);
	}

	// skip dotted (private) properties (R4.2 enterprise spec. table 122.1)
	public static boolean isPrivateProperty(String key) {
		return (key.startsWith(".")); //$NON-NLS-1$
	}

	public static boolean isReservedProperty(String key) {
		return isOSGiProperty(key) || isECFProperty(key) || isPrivateProperty(key);
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
			if (key != null && val != null)
				result.put(key, val);
		}
		return result;
	}

	public static Dictionary createSerializableDictionaryFromMap(Map propMap) {
		if (propMap == null)
			return null;
		Dictionary result = new Properties();
		for (Iterator i = propMap.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Object val = propMap.get(key);
			if (key != null && val != null) {
				try {
					testSerializable(val);
					result.put(key, val);
				} catch (Exception e) {
					LogUtility.logWarning("createSerializableDictionaryFromMap", DebugOptions.EXCEPTIONS_CATCHING, //$NON-NLS-1$
							PropertiesUtil.class, "Cannot serialize value for " + key + ".  Removing from properties", //$NON-NLS-1$ //$NON-NLS-2$
							e);
				}
			}
		}
		return result;
	}

	public static Long getLongWithDefault(Map props, String key, Long def) {
		Object o = props.get(key);
		if (o instanceof Long)
			return (Long) o;
		if (o instanceof String)
			return Long.valueOf((String) o);
		return def;
	}

	public static Integer getIntWithDefault(Map props, String key, Integer def) {
		Object o = props.get(key);
		if (o instanceof Integer)
			return (Integer) o;
		if (o instanceof String)
			return Integer.valueOf((String) o);
		return def;
	}

	public static String[] getStringArrayWithDefault(Map<String, Object> properties, String key, String[] def) {
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
		Object o = props.get(key);
		if (o == null || (!(o instanceof String)))
			return def;
		return (String) o;
	}

	public static Map<String, Object> copyProperties(IRemoteServiceRegistration rsRegistration,
			Map<String, Object> target) {
		String[] keys = rsRegistration.getPropertyKeys();
		for (int i = 0; i < keys.length; i++)
			target.put(keys[i], rsRegistration.getProperty(keys[i]));
		return target;
	}

	public static Map<String, Object> copyProperties(Map<String, Object> source, Map<String, Object> target) {
		for (String key : source.keySet())
			target.put(key, source.get(key));
		return target;
	}

	public static Map<String, Object> copySerializableProperties(Map<String, ?> source, Map<String, Object> target) {
		if (source == null)
			return target;
		for (String key : source.keySet()) {
			Object value = source.get(key);
			try {
				testSerializable(value);
				target.put(key, value);
			} catch (Exception e) {
				LogUtility.logWarning("copySerializableProperties", DebugOptions.EXCEPTIONS_CATCHING, //$NON-NLS-1$
						PropertiesUtil.class,
						"Cannot serialize value for property=" + key + ". Removing from properties", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return target;
	}

	public static Map<String, Object> copyProperties(final ServiceReference serviceReference,
			final Map<String, Object> target) {
		final String[] keys = serviceReference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++) {
			target.put(keys[i], serviceReference.getProperty(keys[i]));
		}
		return target;
	}

	public static Map<String, Object> copyNonECFProperties(Map<String, Object> source, Map<String, Object> target) {
		for (String key : source.keySet())
			if (!isECFProperty(key))
				target.put(key, source.get(key));
		return target;
	}

	public static Map<String, Object> copyNonReservedProperties(Map<String, Object> source,
			Map<String, Object> target) {
		for (String key : source.keySet())
			if (!isReservedProperty(key))
				target.put(key, source.get(key));
		return target;
	}

	public static Map<String, Object> copyNonECFProperties(ServiceReference serviceReference,
			Map<String, Object> target) {
		String[] keys = serviceReference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++)
			if (!isECFProperty(keys[i]))
				target.put(keys[i], serviceReference.getProperty(keys[i]));
		return target;
	}

	public static Map<String, Object> copyNonReservedProperties(ServiceReference serviceReference,
			Map<String, Object> target) {
		String[] keys = serviceReference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++)
			if (!isReservedProperty(keys[i]))
				target.put(keys[i], serviceReference.getProperty(keys[i]));
		return target;
	}

	public static Map<String, Object> copyNonReservedProperties(IRemoteServiceReference rsReference,
			Map<String, Object> target) {
		String[] keys = rsReference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++)
			if (!isReservedProperty(keys[i]))
				target.put(keys[i], rsReference.getProperty(keys[i]));
		return target;
	}

	public static Map mergeProperties(final ServiceReference serviceReference, final Map<String, Object> overrides) {
		return mergeProperties(copyProperties(serviceReference, new HashMap()), overrides);
	}

	public static Map mergePropertiesRaw(final Map<String,Object> source, final Map<String,Object> overrides) {
		return mergeProperties0(source, overrides, true);
	}
	
	public static Map mergeProperties(final Map<String, Object> source, final Map<String, Object> overrides) {
		return mergeProperties0(source, overrides, false);
	}
	
	public static Map mergeProperties0(final Map<String, Object> source, final Map<String, Object> overrides, boolean raw) {

		// copy to target from service reference
		final Map target = copyProperties(source, new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER));

		// now do actual merge
		for (final String key : overrides.keySet()) {
			// skip keys not allowed
			if (!raw && (Constants.SERVICE_ID.equals(key) || Constants.OBJECTCLASS.equals(key))) {
				continue;
			}
			// remove from target if exists
			target.remove(key);
			// get value associated with key
			Object value = overrides.get(key);
			// if value not null then put new key->value into target
			if (value != null) {
				target.put(key, value);
			}
		}
		return target;
	}

	public static Long getOSGiEndpointModifiedValue(Map<String, Object> properties) {
		Object modifiedValue = properties.get(RemoteConstants.OSGI_ENDPOINT_MODIFIED);
		if (modifiedValue != null && modifiedValue instanceof String)
			return Long.valueOf((String) modifiedValue);
		return null;
	}

	public static boolean isConfigProperty(String config, String prop) {
		return prop.startsWith(config + "."); //$NON-NLS-1$
	}

	public static Map<String, Object> removePrivateConfigProperties(String[] configs, Map<String, Object> source) {
		Map<String, Object> results = copyProperties(source,
				new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER));
		for (Iterator<String> it = results.keySet().iterator(); it.hasNext();) {
			for (int i = 0; i < configs.length; i++) {
				String prop = it.next();
				if (isConfigProperty(configs[i], prop) && prop.substring(configs[i].length() + 1).startsWith(".")) //$NON-NLS-1$
					it.remove();
			}
		}
		return results;
	}

	public static Map<String, Object> getConfigProperties(String config, Map<String, Object> source) {
		Map<String, Object> results = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		for (String origKey : source.keySet()) {
			if (isConfigProperty(config, origKey)) {
				String key = origKey.substring(config.length() + 1);
				if (key != null)
					results.put(key, source.get(origKey));
			}
		}
		return results;
	}

	public static Map<String, Object> copyIntentProperties(Map<String, Object> source, String[] intents, Map<String, Object> target) {
		// Copy everything from existing target
		Map<String, Object> results = copyProperties(target,
				new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER));
		for (String intent : intents) {
			// put intent with empty string into results
			results.put(intent, "intent"); //$NON-NLS-1$
			// for each property, if intent or intent config in source then copy to results
			for (String sourceKey : source.keySet())
				if (sourceKey.equals(intent) || isConfigProperty(intent, sourceKey))
					results.put(sourceKey, source.get(sourceKey));
		}
		return results;
	}

	public static Map<String, Object> copyNonIntentsProperties(IRemoteServiceReference rsReference, String[] intents,
			Map<String, Object> target) {
		List intentsList = (intents == null) ? Collections.EMPTY_LIST : Arrays.asList(intents);
		String[] keys = rsReference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++)
			if (!isReservedProperty(keys[i]) && !intentsList.contains(keys[i]))
				target.put(keys[i], rsReference.getProperty(keys[i]));
		return target;
	}

}
