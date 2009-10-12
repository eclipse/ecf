/* 
 * Copyright (c) 2009 Siemens Enterprise Communications GmbH & Co. KG, 
 * Germany. All rights reserved.
 *
 * Siemens Enterprise Communications GmbH & Co. KG is a Trademark Licensee 
 * of Siemens AG.
 *
 * This material, including documentation and any related computer programs,
 * is protected by copyright controlled by Siemens Enterprise Communications 
 * GmbH & Co. KG and its licensors. All rights are reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ecf.osgi.services.discovery.local;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.dgc.VMID;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.service.discovery.ServiceEndpointDescription;
import org.osgi.service.discovery.ServicePublication;

public class ServiceEndpointDescriptionImpl implements
		ServiceEndpointDescription {

	public ServiceEndpointDescriptionImpl() {
		super();
		endpointID = getUUID();
	}

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	private String endpointID = null;

	private final Map listOfJSLPSEDs = Collections
			.synchronizedMap(new HashMap());

	private Map properties = new HashMap();

	/**
	 * 
	 * @param interfaceNames
	 * @param interfacesAndVersions
	 * @param endPointInterfaces
	 * @param props
	 * @throws ServiceLocationException
	 */
	public ServiceEndpointDescriptionImpl(
			final Collection/* <String> */interfaceNames,
			final Collection/* <String> */interfacesAndVersions,
			final Collection/* <String> */endPointInterfaces, final Map props,
			final String endpntID) {
		// check the java interface map for validity
		if (interfaceNames == null) {
			throw new IllegalArgumentException(
					"Given set of Java interfaces must not be null.");
		}
		if (interfaceNames.size() <= 0) {
			throw new IllegalArgumentException(
					"Given set of Java interfaces must contain at least one service interface name.");
		}

		// separate given interface and version strings and put it in a map
		Map interfaceAndVersionsMap = new HashMap();
		if (interfacesAndVersions != null) {
			Iterator versionIterator = interfacesAndVersions.iterator();
			while (versionIterator.hasNext()) {
				String interfaceAndVersion = (String) versionIterator.next();
				int separatorIndex = interfaceAndVersion
						.indexOf(ServicePublication.SEPARATOR);
				// if separator doesn't exist or it's index is invalid (at the
				// very beginning, at the very end)
				if (separatorIndex <= 0
						|| (separatorIndex + 1) == interfaceAndVersion.length()) {
					break;
				}
				String interfaceName = interfaceAndVersion.substring(0,
						separatorIndex);
				String version = interfaceAndVersion
						.substring(separatorIndex + 1);
				if (interfaceName != null && interfaceName.length() > 0
						&& version != null && version.length() > 0) {
					interfaceAndVersionsMap.put(interfaceName, version);
				}
			}
		}

		// separate given java interface and endpoint interface and put it in a
		// map
		Map endPointInterfacesMap = new HashMap();
		if (endPointInterfaces != null) {
			Iterator endpIterator = endPointInterfaces.iterator();
			while (endpIterator.hasNext()) {
				String interfaceAndEndpoint = (String) endpIterator.next();
				int separatorIndex = interfaceAndEndpoint
						.indexOf(ServicePublication.SEPARATOR);
				// if separator doesn't exist or it's index is invalid (at the
				// very beginning, at the very end)
				if (separatorIndex <= 0
						|| (separatorIndex + 1) == interfaceAndEndpoint
								.length()) {
					break;
				}
				String interfaceName = interfaceAndEndpoint.substring(0,
						separatorIndex);
				String endpInterface = interfaceAndEndpoint
						.substring(separatorIndex + 1);
				if (interfaceName != null && interfaceName.length() > 0
						&& endpInterface != null && endpInterface.length() > 0) {
					endPointInterfacesMap.put(interfaceName, endpInterface);
				}
			}
		}

		// create interface-specific SEDs
		Iterator it = interfaceNames.iterator();
		while (it.hasNext()) {
			String ifName = (String) it.next();

			OneInterfaceSED jslpSED = new OneInterfaceSED();
			jslpSED.setInterfaceName(ifName);
			jslpSED.setVersion((String) interfaceAndVersionsMap.get(ifName));
			jslpSED.setEndpointInterface((String) endPointInterfacesMap
					.get(ifName));
			listOfJSLPSEDs.put(ifName, jslpSED);
		}
		if (endpntID != null) {
			this.endpointID = endpntID;
		} else {
			this.endpointID = getUUID();
		}

		if (props != null) {
			this.properties = new HashMap(props);
		}
		addInterfacesAndVersionsToProperties(interfaceNames,
				interfacesAndVersions, endPointInterfaces, endpointID);
	}

	/**
	 * adds the endpoint interfaces and versions to the properties map
	 * 
	 * @throws ServiceLocationException
	 */
	private void addInterfacesAndVersionsToProperties(
			Collection interfaceNames, Collection versions,
			Collection endPointInterfaces, String endpntID) {

		if (properties == null) {
			properties = new HashMap();
		}
		properties.put(ServicePublication.SERVICE_INTERFACE_NAME,
				interfaceNames);
		if (versions != null) {
			properties.put(ServicePublication.SERVICE_INTERFACE_VERSION,
					versions);
		}
		if (endPointInterfaces != null) {
			properties.put(ServicePublication.ENDPOINT_INTERFACE_NAME,
					endPointInterfaces);
		}

		if (endpntID != null) {
			properties.put(ServicePublication.ENDPOINT_ID, endpntID);
		}
	}

	public Map getProperties() {
		return new HashMap(properties);
	}

	public Object getProperty(final String key) {
		return getProperties().get(key);
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("Service:" + LINE_SEPARATOR);
		if (endpointID != null) {
			sb.append("EndpointID = ");
			sb.append(endpointID);
			sb.append(LINE_SEPARATOR);
		}
		synchronized (listOfJSLPSEDs) {
			Iterator it = listOfJSLPSEDs.values().iterator();
			int i = 1;
			while (it.hasNext()) {
				sb.append("Interface ");
				sb.append(i);
				sb.append(LINE_SEPARATOR);
				sb.append((OneInterfaceSED) it.next());
				i++;
			}
		}
		String key;
		Object value;
		Iterator it = properties.keySet().iterator();
		if (it.hasNext()) {
			sb.append("properties=" + LINE_SEPARATOR);
		}
		while (it.hasNext()) {
			key = (String) it.next();
			value = properties.get(key);
			if (value == null) {
				value = "<null>";
			}

			sb.append("\t");
			sb.append(key);
			sb.append("=");
			sb.append(value.toString());
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	/**
	 * 
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getProvidedInterfaces()
	 */
	public Collection getProvidedInterfaces() {
		List l = new ArrayList();
		synchronized (listOfJSLPSEDs) {
			Iterator it = listOfJSLPSEDs.values().iterator();
			while (it.hasNext()) {
				l.add(((OneInterfaceSED) it.next()).getInterfaceName());
			}
		}
		return l;
	}

	/**
	 * 
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getEndpointInterfaceName(java.lang.String)
	 */
	public String getEndpointInterfaceName(String interfaceName) {
		OneInterfaceSED jSED = ((OneInterfaceSED) listOfJSLPSEDs
				.get(interfaceName));
		if (jSED != null) {
			return jSED.getEndpointInterface();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getVersion(java.lang.String)
	 */
	public String getVersion(String interfaceName) {
		OneInterfaceSED jSED = ((OneInterfaceSED) listOfJSLPSEDs
				.get(interfaceName));
		if (jSED != null) {
			return jSED.getVersion();
		} else {
			return null;
		}
	}

	public URI getLocation() {
		Object uriObject = getProperty(ServicePublication.ENDPOINT_LOCATION);
		if (uriObject instanceof URI) {
			return (URI) uriObject;
		} else if (uriObject instanceof String) {
			try {
				return new URI((String) uriObject);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e.getMessage());
			}
		} else if (uriObject == null) {
			return null;
		} else {
			throw new RuntimeException(
					"Service location property is not of expected type URI or String. Property = "
							+ uriObject.toString());
		}
	}

	/**
	 * 
	 * @return Collection
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getPropertyKeys()
	 */
	public Collection getPropertyKeys() {
		return getProperties().keySet();
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void addProperty(String key, Object value) {
		synchronized (listOfJSLPSEDs) {
			Iterator it = listOfJSLPSEDs.values().iterator();
			while (it.hasNext()) {
				((OneInterfaceSED) it.next()).addProperty(key, value);
			}
		}
		if (key.equals(ServicePublication.ENDPOINT_ID)) {
			endpointID = (String) value;
		}
		properties.put(key, value);
	}

	/**
	 * @return
	 */
	private String getUUID() {
		return new UID().toString() + new VMID().toString();
	}

	/**
	 * 
	 * @param javaInterfaceName
	 * @return String
	 */
	public static String convertJavaInterface2Path(
			final String javaInterfaceName) {
		return javaInterfaceName != null ? ":"
				+ javaInterfaceName.replace('.', '/') : "";
	}

	/**
	 * 
	 * @param interfaceNameEncodedAsPath
	 * @return String
	 */
	public static String convertPath2JavaInterface(
			final String interfaceNameEncodedAsPath) {
		return interfaceNameEncodedAsPath != null ? interfaceNameEncodedAsPath
				.replace('/', '.') : null;
	}

	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object serviceDescription) {
		if (!(serviceDescription instanceof ServiceEndpointDescription)) {
			return false;
		}

		ServiceEndpointDescription descr = (ServiceEndpointDescription) serviceDescription;

		// if one has an EndpointID
		if (this.endpointID != null || descr.getEndpointID() != null) {
			if (this.endpointID != null) {
				return this.endpointID.equals(descr.getEndpointID());
			} else {
				// we don't have an endpointID but only the other.
				return false;
			}
		}

		Collection descrInterfaces = descr.getProvidedInterfaces();
		if (descrInterfaces == null) {
			throw new RuntimeException(
					"The service does not contain requiered parameter interfaces. "
							+ descr);
		}

		boolean found = false;
		synchronized (listOfJSLPSEDs) {
			Iterator it = listOfJSLPSEDs.values().iterator();
			while (it.hasNext()) {
				OneInterfaceSED sed = (OneInterfaceSED) it.next();
				if (sed.equals(serviceDescription)) {
					found = true;
				}
			}
		}

		return found;
	}

	/**
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// In case endpointID has been provided by DSW / another Discovery or
		// has been generated
		if (endpointID != null) {
			return endpointID.hashCode();
		} else {
			int result = 17;
			synchronized (listOfJSLPSEDs) {
				Iterator it = listOfJSLPSEDs.values().iterator();
				while (it.hasNext()) {
					result = 37 * result
							+ ((OneInterfaceSED) it.next()).hashCode();
				}
			}

			if (endpointID != null) {
				result = 37 * result + endpointID.hashCode();
			}

			if (properties != null) {
				result = 37 * result + properties.hashCode();
			}

			return result;
		}
	}

	/**
	 * 
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getEndpointID()
	 */
	public String getEndpointID() {
		return endpointID;
	}

	/**
	 * 
	 * @param interfaceNames
	 *            the list of interfaceNames provided by this
	 *            ServiceEndpointDescription
	 */
	public void setProvidedInterfaces(Collection interfaceNames) {
		if (interfaceNames == null || interfaceNames.isEmpty()) {
			return;
		}
		listOfJSLPSEDs.clear();
		// create interface-specific SEDs
		Iterator it = interfaceNames.iterator();
		while (it.hasNext()) {
			String ifName = (String) it.next();
			OneInterfaceSED jslpSED = new OneInterfaceSED();
			jslpSED.setInterfaceName(ifName);
			listOfJSLPSEDs.put(ifName, jslpSED);
		}
	}

	public void setProperties(Map props) {
		// TODO implement setting of members (endpointID, endpointinterfaces,
		// versions)
		properties = new HashMap(props);
	}
}
