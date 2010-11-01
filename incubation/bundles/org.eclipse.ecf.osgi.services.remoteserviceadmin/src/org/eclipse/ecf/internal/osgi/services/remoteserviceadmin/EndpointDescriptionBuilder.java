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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.StringID;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionParseException;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;

public class EndpointDescriptionBuilder {

	public EndpointDescription[] createEndpointDescriptions(InputStream input) throws IOException, EndpointDescriptionParseException {
		// First create parser
		EndpointDescriptionParser parser = new EndpointDescriptionParser();
		// Parse input stream
		parser.parse(input);
		// Get possible endpoint descriptions
		List<EndpointDescriptionParser.EndpointDescription> parsedDescriptions = parser.getEndpointDescriptions();
		List<EndpointDescription> results = new ArrayList();
		// For each one parsed, get properties and 
		for(EndpointDescriptionParser.EndpointDescription ed: parsedDescriptions) {
			Map endpointDescriptionProperties = createProperties(ed.getProperties());
			if (endpointDescriptionProperties != null) results.add(new EndpointDescription(endpointDescriptionProperties));
		}
		return (EndpointDescription[]) results.toArray(new EndpointDescription[] {});
	}

	private String verifyStringProperty(Map<String,Object> properties, String propName) {
		Object r = properties.get(propName);
		try {
			return (String) r;
		}
		catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"property value is not a String: " + propName);
			iae.initCause(e);
			throw iae;
		}
	}

	private ID getContainerID(Map<String,Object> properties) throws IDCreateException {
		ID result = null;
		// First check to see if the container id and namespace have been explicitly set
		String containerIDName = verifyStringProperty(properties, RemoteConstants.ENDPOINT_CONTAINER_ID);
		if (containerIDName != null) {
			String containerNS = verifyStringProperty(properties, RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE);
			Namespace ns = getNamespace(containerNS);
			if (ns != null) result = createID(ns,containerIDName);
		} else {
			// We try to get the ID from the OSGi id
			String osgiId = verifyStringProperty(properties, org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID);
			if (osgiId == null) throw new IDCreateException("OSGi endpoint.id must not be null");
			Namespace ns = findNamespaceForOSGiId(osgiId);
			if (ns != null) result = createID(ns,osgiId);
		}
		return result;
	}
	
	private Namespace findNamespace(String namespaceName) {
		return getIDFactory().getNamespaceByName(namespaceName);
	}
	
	private Namespace findNamespaceForOSGiId(String osgiId) {
		int colonIndex = osgiId.indexOf(':');
		if (colonIndex <= 0) return null;
		String scheme = osgiId.substring(0,colonIndex-1);
		// First try to find the Namespace using the protocol directly
		Namespace ns = findNamespace(scheme);
		if (ns == null) {
			// Then try to find by comparing to all Namespace.getScheme()
			ns = findNamespaceByScheme(scheme);
		}
		return ns;
	}

	private Namespace findNamespaceByScheme(String scheme) {
		if (scheme == null) return null;
		List namespaces = getIDFactory().getNamespaces();
		for(Iterator i=namespaces.iterator(); i.hasNext(); ) {
			Namespace ns = (Namespace) i.next();
			if (scheme.equals(ns.getScheme())) {
				// found it...so return
				return ns;
			}
		}
		return null;
	}
	
	private IIDFactory getIDFactory() {
		return IDFactory.getDefault();
	}
	
	private Namespace getNamespace(String namespaceName) {
		Namespace result = findNamespace(namespaceName);
		return (result == null)?getIDFactory().getNamespaceByName(StringID.class.getName()):result;
	}
	
	private Map createProperties(Map<String, Object> properties) {
		try {
			ID containerID = getContainerID(properties);
			if (containerID == null) throw new NullPointerException("ECF EndpointDescriptions must include non-null value for "+RemoteConstants.ENDPOINT_CONTAINER_ID+" of type ID");
			properties.put(RemoteConstants.ENDPOINT_CONTAINER_ID, containerID);
			properties.remove(RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE);
			Long rsId = getRemoteServiceID(properties);
			if (rsId == null) throw new NullPointerException("ECF EndpointDescription must include non-null value of "+RemoteConstants.ENDPOINT_REMOTESERVICE_ID+" of type Long");
			properties.put(RemoteConstants.ENDPOINT_REMOTESERVICE_ID, rsId);
			
			// XXX do targetID, idFilter IDs
			
			return properties;
		} catch (Exception e) {
			logError("createStandardProperties","unexpected error getting ECF properties from properties="+properties,e);
			return null;
		}
	}

	private Long getRemoteServiceID(Map<String, Object> properties) {
		Long remoteServiceID = (Long) properties.get(RemoteConstants.ENDPOINT_REMOTESERVICE_ID);
		if (remoteServiceID != null) return remoteServiceID;
		return new Long(0);
	}

	private ID createID(Namespace ns, String containerIDName) throws IDCreateException {
		return getIDFactory().createID(ns, new Object[] { containerIDName });
	}

	private void logError(String string, String string2, Throwable e) {
		// TODO Auto-generated method stub
		
	}
}
