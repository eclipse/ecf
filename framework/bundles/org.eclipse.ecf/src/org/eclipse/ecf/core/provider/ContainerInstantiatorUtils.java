/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.core.provider;

import java.net.*;
import java.util.*;
import org.eclipse.ecf.core.identity.ID;
import org.osgi.framework.Constants;

/**
 * @since 3.9
 */
public class ContainerInstantiatorUtils {

	public static final String PRIVATE_INTENT = "osgi.private"; //$NON-NLS-1$

	/**
	 * @since 3.9
	 */
	public static String[] getContainerIntents(Map<String, ?> properties) {
		return getStringArrayProperty(properties, Constants.SERVICE_INTENTS);
	}

	/**
	 * @since 3.9
	 */
	public static boolean containsIntent(String[] intents, String intent) {
		if (intents == null)
			return false;
		return Arrays.asList(intents).contains(intent);
	}

	/**
	 * @since 3.9
	 */
	public static boolean containsPrivateIntent(String[] intents) {
		return containsIntent(intents, PRIVATE_INTENT);
	}

	/**
	 * @since 3.9
	 */
	public static boolean containsPrivateIntent(Map<String, ?> properties) {
		return containsPrivateIntent(getContainerIntents(properties));
	}

	/**
	 * @since 3.9
	 */
	public static String[] getStringArrayProperty(Map properties, String key) {
		if (properties == null)
			return null;
		Object value = properties.get(key);
		List<String> r = new ArrayList<String>();
		if (value == null)
			r = Collections.EMPTY_LIST;

		if (value instanceof String)
			r = Collections.singletonList((String) value);

		if (value instanceof String[]) {
			String[] values = (String[]) value;
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null)
					r.add(values[i]);
			}
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
		}
		return (r.size() == 0) ? null : r.toArray(new String[r.size()]);
	}

	/**
	 * @since 3.9
	 */
	public static void checkPrivate(InetAddress inetAddress) throws ContainerIntentException {
		if (!inetAddress.isSiteLocalAddress())
			throw new ContainerIntentException(PRIVATE_INTENT, "Address " + inetAddress + " is not private"); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	/**
	 * @since 3.9
	 */
	public static void checkPrivate(String hostname) throws ContainerIntentException {
		if (hostname == null)
			throw new ContainerIntentException(PRIVATE_INTENT, "Null hostname cannot be private"); //$NON-NLS-1$

		InetAddress ia = null;
		if (hostname.equals("localhost") || hostname.equals("127.0.0.1")) { //$NON-NLS-1$ //$NON-NLS-2$
			try {
				ia = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				throw new ContainerIntentException(PRIVATE_INTENT, "Could not get localhost inetaddress", e); //$NON-NLS-1$ 
			}
		} else {
			try {
				ia = InetAddress.getByName(hostname);
			} catch (UnknownHostException e) {
				throw new ContainerIntentException(PRIVATE_INTENT, "Could not get address for hostname: " + hostname); //$NON-NLS-1$ 
			}
		}
		checkPrivate(ia);
	}

	/**
	 * @since 3.9
	 */
	public static void checkPrivate(ID serverID) throws ContainerIntentException {
		String name = serverID.getName();
		try {
			checkPrivate(new URI(name).getHost());
		} catch (URISyntaxException e) {
			throw new ContainerIntentException(PRIVATE_INTENT, "Could not get hostname for serverID name: " + name); //$NON-NLS-1$ 
		}
	}
}
