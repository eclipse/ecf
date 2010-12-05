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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;

public class PropertiesUtil {

	protected static final List osgiProperties = Arrays
			.asList(new String[] {
					// OSGi properties
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
					org.osgi.framework.Constants.OBJECTCLASS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED });

	protected static final List ecfProperties = Arrays.asList(new String[] {
			// ECF properties
			RemoteConstants.ENDPOINT_CONNECTTARGET_ID,
			RemoteConstants.ENDPOINT_CONNECTTARGET_ID_NAMESPACE,
			RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE,
			RemoteConstants.ENDPOINT_REMOTESERVICE_ID,
			RemoteConstants.ENDPOINT_IDFILTER_IDS,
			RemoteConstants.ENDPOINT_IDFILTER_IDARRAY_COUNT,
			RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER,
			RemoteConstants.DISCOVERY_DEFAULT_SERVICE_NAME_PREFIX,
			RemoteConstants.DISCOVERY_NAMING_AUTHORITY,
			RemoteConstants.DISCOVERY_PROTOCOLS,
			RemoteConstants.DISCOVERY_SCOPE,
			RemoteConstants.DISCOVERY_SERVICE_NAME,
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
		return osgiProperties.contains(key);
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


}
