/*******************************************************************************
 * Copyright (c) 2008 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.jslp;

import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.util.StringUtils;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.ServiceProperties;

/**
 * Adapts SLP's service properties to ECF's ServiceProperties and vice versa
 * @see "http://www.ietf.org/rfc/rfc2608.txt page. 10ff"
 */
public class ServicePropertiesAdapter {

	private static final String SLP_BYTE_PREFIX = "\\FF"; //$NON-NLS-1$
	private IServiceProperties serviceProperties;

	public ServicePropertiesAdapter(List aList) {
		Assert.isNotNull(aList);
		serviceProperties = new ServiceProperties();
		for (Iterator itr = aList.iterator(); itr.hasNext();) {
			String[] str = StringUtils.split((String) itr.next(), "="); //$NON-NLS-1$
			if (str.length != 2) {
				Trace.trace(Activator.PLUGIN_ID, "/debug/methods/tracing", this.getClass(), "ServicePropertiesAdapter(List)", "Skipped broken service attribute " + str); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				continue;
			}
			// remove the brackets "( )" from the string value which are added by jSLP for the LDAP style string representation
			String key = str[0].substring(1);
			String value = str[1].substring(0, str[1].length() - 1);
			if (value.startsWith(SLP_BYTE_PREFIX)) {
				String[] strs = StringUtils.split(value.substring(4), "\\"); //$NON-NLS-1$
				byte[] b = new byte[strs.length];
				for (int i = 0; i < strs.length; i++) {
					b[i] = new Byte(strs[i]).byteValue();
				}
				serviceProperties.setPropertyBytes(key, b);
			} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) { //$NON-NLS-1$ //$NON-NLS-2$
				serviceProperties.setProperty(key, new Boolean(value));
			} else if (isInteger(value)) {
				serviceProperties.setProperty(key, new Integer(value));
			} else {
				serviceProperties.setProperty(key, value);
			}
		}
	}

	public ServicePropertiesAdapter(IServiceProperties aServiceProperties) {
		Assert.isNotNull(aServiceProperties);
		serviceProperties = aServiceProperties;
	}

	private boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public IServiceProperties toServiceProperties() {
		return serviceProperties;
	}

	public Dictionary toProperties() {
		Dictionary dict = new Properties();
		Enumeration propertyNames = serviceProperties.getPropertyNames();
		while (propertyNames.hasMoreElements()) {
			String key = (String) propertyNames.nextElement();
			byte[] propertyBytes = serviceProperties.getPropertyBytes(key);
			if (propertyBytes != null) {
				StringBuffer buf = new StringBuffer();
				buf.append(SLP_BYTE_PREFIX);
				for (int i = 0; i < propertyBytes.length; i++) {
					buf.append("\\"); //$NON-NLS-1$
					buf.append(propertyBytes[i]);
				}
				dict.put(key, buf.toString());
			} else {
				dict.put(key, serviceProperties.getProperty(key).toString());
			}
		}
		return dict;
	}
}