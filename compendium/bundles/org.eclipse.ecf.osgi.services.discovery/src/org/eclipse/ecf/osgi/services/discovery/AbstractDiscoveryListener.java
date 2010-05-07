/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.discovery;

import java.util.Enumeration;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.internal.osgi.services.discovery.Activator;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public abstract class AbstractDiscoveryListener {

	protected int logLevel = LogService.LOG_INFO;

	public AbstractDiscoveryListener(int logLevel) {
		this.logLevel = logLevel;
	}

	public AbstractDiscoveryListener() {
	}

	protected int getLogLevel() {
		return logLevel;
	}

	protected void log(ServiceReference serviceReference, String message,
			Throwable t) {
		Activator a = Activator.getDefault();
		if (a == null)
			return;
		a.log(serviceReference, getLogLevel(), message, t);
	}

	protected String printServiceInfo(int tabLevel, IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return "null"; //$NON-NLS-1$
		StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
		sb.append(createTabs(tabLevel));
		sb.append("serviceInfo").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		printServicePropertyString(tabLevel + 1, "location", serviceInfo //$NON-NLS-1$
				.getLocation().toString(), sb, true);
		printServicePropertyString(tabLevel + 1, "serviceID", serviceInfo //$NON-NLS-1$
				.getServiceID().toExternalForm(), sb, true);
		printServicePropertyString(tabLevel + 1, "serviceName", //$NON-NLS-1$
				serviceInfo.getServiceName(), sb, true);
		printServicePropertyString(tabLevel + 1, "priority", //$NON-NLS-1$
				serviceInfo.getPriority() + "", sb, true); //$NON-NLS-1$
		printServicePropertyString(tabLevel + 1, "weight", //$NON-NLS-1$
				serviceInfo.getWeight() + "", sb, true); //$NON-NLS-1$
		sb.append(createTabs(tabLevel + 1))
				.append("discoveryServiceProperties") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(printServiceProperties(tabLevel + 2,
						serviceInfo.getServiceProperties()));
		IServiceProperties serviceProperties = serviceInfo
				.getServiceProperties();
		if (serviceProperties != null) {
			sb.append(createTabs(tabLevel + 1));
			sb.append("osgiServiceProperties"); //$NON-NLS-1$
			sb.append("\n"); //$NON-NLS-1$
			sb.append(printServicePropertyString(tabLevel + 2,
					serviceProperties,
					ServicePublication.SERVICE_INTERFACE_NAME,
					"osgiServiceInterfaces")); //$NON-NLS-1$
			sb.append(printServicePropertyString(tabLevel + 2,
					serviceProperties,
					RemoteServicePublication.ENDPOINT_SUPPORTED_CONFIGS,
					"endpointSupportedConfigs")); //$NON-NLS-1$
			sb.append(printServicePropertyString(tabLevel + 2,
					serviceProperties,
					RemoteServicePublication.ENDPOINT_SERVICE_INTENTS,
					"endpointServiceIntents")); //$NON-NLS-1$
			sb.append(printServicePropertyBytes(tabLevel + 2,
					serviceProperties,
					RemoteServicePublication.ENDPOINT_CONTAINERID,
					"endpointContainerID")); //$NON-NLS-1$
			sb.append(printServicePropertyString(tabLevel + 2,
					serviceProperties,
					RemoteServicePublication.ENDPOINT_CONTAINERID_NAMESPACE,
					"endpointContainerIDNamespace")); //$NON-NLS-1$
			sb.append(printServicePropertyBytes(tabLevel + 2,
					serviceProperties, "ecf.rsvc.id", "remoteServiceID")); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(printServicePropertyString(tabLevel + 2,
					serviceProperties, "ecf.rsvc.ns", //$NON-NLS-1$
					"remoteServiceIDNamespace")); //$NON-NLS-1$
		}

		return sb.toString();
	}

	private void printServicePropertyString(int tabLevel, String propertyName,
			String propertyValue, StringBuffer sb, boolean newline) {
		if (propertyValue != null) {
			sb.append(createTabs(tabLevel)).append(propertyName);
			sb.append("="); //$NON-NLS-1$
			sb.append(propertyValue);
			if (newline)
				sb.append("\n"); //$NON-NLS-1$
		}
	}

	protected String printServicePropertyString(int tabLevel,
			IServiceProperties serviceProperties, String propertyName,
			String outputName) {
		StringBuffer sb = new StringBuffer();
		String propertyValue = serviceProperties
				.getPropertyString(propertyName);
		if (propertyValue != null)
			printServicePropertyString(tabLevel, outputName, propertyValue, sb,
					true);
		return sb.toString();
	}

	protected String printServicePropertyBytes(int tabLevel,
			IServiceProperties serviceProperties, String propertyName,
			String outputName) {
		byte[] propertyValue = serviceProperties.getPropertyBytes(propertyName);
		StringBuffer sb = new StringBuffer();
		if (propertyValue != null)
			printServicePropertyString(tabLevel, outputName, new String(
					propertyValue), sb, true);
		return sb.toString();
	}

	protected String printServiceID(IServiceID serviceID) {
		if (serviceID == null)
			return "null"; //$NON-NLS-1$
		return serviceID.toExternalForm();
	}

	protected String createTabs(int tabLevel) {
		StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
		for (int i = 0; i < tabLevel; i++) {
			sb.append("\t"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	protected String printServiceProperties(int tabLevel,
			IServiceProperties serviceProperties) {
		if (serviceProperties == null)
			return "null"; //$NON-NLS-1$
		Enumeration enumeration = serviceProperties.getPropertyNames();
		StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
		if (enumeration != null) {
			for (Enumeration e = enumeration; e.hasMoreElements();) {
				String key = (String) e.nextElement();
				sb.append(createTabs(tabLevel));
				sb.append("name=").append(key).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
				String stringValue = serviceProperties.getPropertyString(key);
				byte[] bytesValue = serviceProperties.getPropertyBytes(key);
				Object objectValue = serviceProperties.getProperty(key);
				if (stringValue != null)
					sb.append("value[String]=").append(stringValue); //$NON-NLS-1$
				if (bytesValue != null)
					sb.append("value[bytes]=").append(printBytes(bytesValue)); //$NON-NLS-1$
				if (objectValue != null && stringValue == null) {
					if (bytesValue != null)
						sb.append(","); //$NON-NLS-1$
					sb.append("value[Object]=").append(objectValue); //$NON-NLS-1$
				}
				sb.append("\n"); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}

	protected String printBytes(byte[] bytesValue) {
		if (bytesValue == null)
			return "null"; //$NON-NLS-1$
		StringBuffer sb = new StringBuffer("["); //$NON-NLS-1$
		for (int i = 0; i < bytesValue.length; i++) {
			if (i > 0)
				sb.append(","); //$NON-NLS-1$
			sb.append(bytesValue[i]);
		}
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

}
