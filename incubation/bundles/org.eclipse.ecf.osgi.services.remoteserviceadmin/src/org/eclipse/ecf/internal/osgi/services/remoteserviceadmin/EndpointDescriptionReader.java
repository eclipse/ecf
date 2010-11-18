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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractMetadataFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionParseException;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;
import org.osgi.framework.Constants;

public class EndpointDescriptionReader extends AbstractMetadataFactory {

	public org.osgi.service.remoteserviceadmin.EndpointDescription[] readEndpointDescriptions(
			InputStream input) throws IOException {
		// First create parser
		EndpointDescriptionParser parser = new EndpointDescriptionParser();
		// Parse input stream
		parser.parse(input);
		// Get possible endpoint descriptions
		List<EndpointDescriptionParser.EndpointDescription> parsedDescriptions = parser
				.getEndpointDescriptions();
		List<org.osgi.service.remoteserviceadmin.EndpointDescription> results = new ArrayList();
		// For each one parsed, get properties and
		for (EndpointDescriptionParser.EndpointDescription ed : parsedDescriptions) {
			Map parsedProperties = ed.getProperties();
			org.osgi.service.remoteserviceadmin.EndpointDescription result = null;
			try {
				// OSGI required properties
				// objectClass/String+
				List<String> objectClasses = Activator.getStringPlusProperty(
						parsedProperties, Constants.OBJECTCLASS);
				// Must have at least one objectClass
				if (objectClasses == null || objectClasses.size() == 0)
					throw new EndpointDescriptionParseException(
							Constants.OBJECTCLASS
									+ " is not set in endpoint description.  It must be set to String+ value");
				parsedProperties.put(Constants.OBJECTCLASS,
						(String[]) objectClasses
								.toArray(new String[objectClasses.size()]));

				// endpoint.id
				String endpointId = getStringWithDefault(
						parsedProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
						null);
				// Must have endpoint id, so throw if it's not found
				if (endpointId == null)
					throw new EndpointDescriptionParseException(
							org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID
									+ " is not set in endpoint description.  It must be set to String value");
				parsedProperties
						.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
								endpointId);

				// endpoint.service.id. Default is set to Long(0), which means
				// not an OSGi endpoint description
				Long endpointServiceId = getLongWithDefault(
						parsedProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
						new Long(0));
				parsedProperties
						.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
								endpointServiceId);

				// service.imported.configs
				List<String> configurationTypes = Activator
						.getStringPlusProperty(
								parsedProperties,
								org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS);
				// Must have at least one service.imported.config
				if (configurationTypes == null
						|| configurationTypes.size() == 0)
					throw new EndpointDescriptionParseException(
							org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS
									+ " is not set in endpoint description.  It must be set to String+ value)");

				// Create OSGi endpoint description to verify that all OSGi
				// properties are correct/set
				result = new org.osgi.service.remoteserviceadmin.EndpointDescription(
						parsedProperties);

				// If ECF endpoint description, then an endpoint container ID
				// will be non-null
				ID endpointContainerID = getContainerID(parsedProperties);
				// if the endpointContainerID is not found, then this is not an
				// ECF endpoint description
				if (endpointContainerID != null) {
					result = createECFEndpointDescription(endpointContainerID,
							parsedProperties);
				}
				results.add(result);
			} catch (Exception e) {
				logError("Exception parsing endpoint description properties", e);
			}
		}
		return (org.osgi.service.remoteserviceadmin.EndpointDescription[]) results
				.toArray(new org.osgi.service.remoteserviceadmin.EndpointDescription[] {});
	}

	private org.osgi.service.remoteserviceadmin.EndpointDescription createECFEndpointDescription(
			ID endpointContainerID, Map parsedProperties)
			throws EndpointDescriptionParseException {
		// we get the remote service id...default 0 means that it's not an ECF
		// remote service
		Long remoteServiceId = getLongWithDefault(parsedProperties,
				RemoteConstants.ENDPOINT_REMOTESERVICE_ID, null);
		if (remoteServiceId == null)
			throw new EndpointDescriptionParseException(
					RemoteConstants.ENDPOINT_REMOTESERVICE_ID
							+ " is not set in endpoint description.  It must be set to Long value");
		// target ID
		ID targetID = null;
		String targetName = (String) parsedProperties
				.get(RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		String targetNamespace = (String) parsedProperties
				.get(RemoteConstants.ENDPOINT_CONNECTTARGET_ID_NAMESPACE);
		if (targetName != null) {
			if (targetNamespace == null)
				targetNamespace = endpointContainerID.getNamespace().getName();
			targetID = createID(targetNamespace, targetName);
		}

		// id filter
		ID[] idFilter = getIDFilter(endpointContainerID.getNamespace(),
				parsedProperties);
		// rs filter
		String rsFilter = (String) parsedProperties
				.get(RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);

		Map properties = getNonECFProperties(parsedProperties);

		return new EndpointDescription(properties, endpointContainerID,
				remoteServiceId.longValue(), targetID, idFilter, rsFilter);
	}

	private ID[] getIDFilter(Namespace namespace, Map<String, Object> properties) {
		List<ID> resultList = new ArrayList();
		Object o = properties.get(RemoteConstants.ENDPOINT_IDFILTER_IDS);
		if (o != null && o instanceof List<?>) {
			// Assumed to be list of strings
			for (String i : (List<String>) o) {
				ID id = createID(namespace, i);
				if (id != null)
					resultList.add(id);
			}
		} else {
			Number countInt = null;
			Object counto = properties
					.get(RemoteConstants.ENDPOINT_IDFILTER_IDARRAY_COUNT);
			if (counto instanceof Number)
				countInt = (Number) counto;
			if (countInt == null)
				return null;
			int count = countInt.intValue();
			if (count <= 0)
				return null;
			for (int i = 0; i < count; i++) {
				// decode string as name
				String name = (String) properties
						.get(RemoteConstants.ENDPOINT_IDFILTER_IDARRAY_NAME_
								+ i);
				if (name != null) {
					String ns = (String) properties
							.get(RemoteConstants.ENDPOINT_IDFILTER_IDARRAY_NAMESPACE_
									+ i);
					if (ns == null)
						ns = namespace.getName();
					ID id = createID(ns, name);
					if (id != null)
						resultList.add(id);
				}
			}
		}
		return (resultList.size() > 0) ? (ID[]) resultList
				.toArray(new ID[resultList.size()]) : null;
	}

	private Map<String, Object> getNonECFProperties(
			Map<String, Object> parsedProperties) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (String key : parsedProperties.keySet())
			if (!isECFProperty(key))
				result.put(key, parsedProperties.get(key));
		return result;
	}

	private void logError(String message, Throwable exception) {
		System.err.println(message);
		if (exception != null) {
			exception.printStackTrace(System.err);
		}
	}

	private String verifyStringProperty(Map properties, String propName) {
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

	private ID getContainerID(Map<String, Object> properties)
			throws IDCreateException {
		ID result = null;
		// First check to see if the container id and namespace have been
		// explicitly set
		String containerIDName = verifyStringProperty(properties,
				RemoteConstants.ENDPOINT_CONTAINER_ID);
		if (containerIDName != null) {
			String containerNS = verifyStringProperty(properties,
					RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE);
			Namespace ns = getNamespace(containerNS);
			if (ns != null)
				result = createID(ns, containerIDName);
		} else {
			// We try to get the ID from the OSGi id
			String osgiId = verifyStringProperty(
					properties,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID);
			if (osgiId == null)
				throw new IDCreateException(
						org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID
								+ " must not be null");
			Namespace ns = findNamespaceForOSGiId(osgiId);
			if (ns != null)
				result = createID(ns, osgiId);
		}
		return result;
	}

}
