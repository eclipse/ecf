/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.discovery;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.osgi.framework.ServiceReference;

public class ServicePropertyUtils {

	private static final String COLLECTION_SEPARATOR = ",";

	public static Collection getCollectionProperty(ServiceReference sr,
			String propName) {
		if (sr == null)
			return null;
		Object val = sr.getProperty(propName);
		if (val == null || !(val instanceof Collection))
			return null;
		return (Collection) val;
	}

	public static String getStringProperty(ServiceReference reference,
			String propKey) {
		Object val = reference.getProperty(propKey);
		if (val == null || !(val instanceof String))
			return null;
		return (String) val;
	}

	public static URL getURLProperty(ServiceReference reference, String propKey) {
		Object val = reference.getProperty(propKey);
		if (val == null || !(val instanceof URL))
			return null;
		return (URL) val;
	}

	public static Map getMapProperty(ServiceReference reference,
			String propKeyServiceProperties) {
		Object val = reference.getProperty(propKeyServiceProperties);
		if (val == null || !(val instanceof Map))
			return null;
		return (Map) val;
	}

	public static String createStringFromCollection(Collection svcInterfaces) {
		StringBuffer result = new StringBuffer();
		for (Iterator i = svcInterfaces.iterator(); i.hasNext();) {
			String item = (String) i.next();
			result.append(item);
			if (i.hasNext())
				result.append(COLLECTION_SEPARATOR);
		}
		return result.toString();
	}

	public static Collection createCollectionFromString(String value) {
		StringTokenizer t = new StringTokenizer(value, COLLECTION_SEPARATOR);
		List result = new ArrayList();
		while (t.hasMoreTokens()) {
			result.add(t.nextToken());
		}
		return result;
	}

}
