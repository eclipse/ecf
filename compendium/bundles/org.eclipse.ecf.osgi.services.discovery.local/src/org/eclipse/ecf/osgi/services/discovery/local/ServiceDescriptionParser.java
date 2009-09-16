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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ServiceDescriptionParser {

	private static final String SERVICEDESCRIPTION_ELEMENT = "service-description";

	private static final String PROVIDE_ELEMENT = "provide";
	private static final String PROPERTY_ELEMENT = "property";

	private static final String INTERFACE_ATTR = "interface";
	private static final String NAME_ATTR = "name";
	private static final String TYPE_ATTR = "type";

	private void findElementsNamed(Node top, String name, List aList) {
		int type = top.getNodeType();
		switch (type) {
		case Node.DOCUMENT_TYPE_NODE:
			// Print entities if any
			NamedNodeMap nodeMap = ((DocumentType) top).getEntities();
			for (int i = 0; i < nodeMap.getLength(); i++) {
				Entity entity = (Entity) nodeMap.item(i);
				findElementsNamed(entity, name, aList);
			}
			break;
		case Node.ELEMENT_NODE:
			String elementName = top.getNodeName();
			if (name.equals(elementName)) {
				aList.add(top);
			}
		default:
			for (Node child = top.getFirstChild(); child != null; child = child
					.getNextSibling()) {
				findElementsNamed(child, name, aList);
			}
		}
	}

	private Collection processServiceDescriptionNodes(
			List serviceDescriptionNodes) {
		Collection res = new ArrayList();
		for (Iterator i = serviceDescriptionNodes.iterator(); i.hasNext();) {
			Node n = (Node) i.next();

			ServiceEndpointDescriptionImpl c = new ServiceEndpointDescriptionImpl();
			processServiceEndpointDescription(n, c);
			res.add(c);
		}
		return res;
	}

	private Collection loadServiceDescriptions(Document doc) {
		List ps = new ArrayList();
		findElementsNamed(doc, SERVICEDESCRIPTION_ELEMENT, ps);
		return processServiceDescriptionNodes(ps);
	}

	private String getAttributeValue(Node node, String attrName) {
		NamedNodeMap attrs = node.getAttributes();
		Node attrNode = attrs.getNamedItem(attrName);
		if (attrNode != null) {
			return attrNode.getNodeValue();
		}
		return ""; //$NON-NLS-1$
	}

	private void processServiceEndpointDescription(Node n,
			ServiceEndpointDescriptionImpl c) {
		List groupList = new ArrayList();
		findElementsNamed(n, PROVIDE_ELEMENT, groupList);
		Collection/* <String> */providedInterfaces = null;
		if (!groupList.isEmpty()) {
			providedInterfaces = new ArrayList/* <String> */();
			for (Iterator i = groupList.iterator(); i.hasNext();) {
				Node node = (Node) i.next();
				String providedInterfaceValue = getAttributeValue(node,
						INTERFACE_ATTR);
				if (providedInterfaceValue != null
						&& !providedInterfaceValue.equals("")) { //$NON-NLS-1$
					providedInterfaces.add(providedInterfaceValue);
				}
			}
			c.setProvidedInterfaces(providedInterfaces);
		}
		groupList = new ArrayList();
		findElementsNamed(n, PROPERTY_ELEMENT, groupList);
		Map/* <String, String> */providedProperties = null;
		if (!groupList.isEmpty()) {
			providedProperties = new HashMap/* <String, String> */();
			for (Iterator i = groupList.iterator(); i.hasNext();) {
				Node node = (Node) i.next();
				String name = getAttributeValue(node, NAME_ATTR);
				String value = node.getFirstChild().getNodeValue();
				String type = getAttributeValue(node, TYPE_ATTR);
				if (name != null && !name.equals("")) { //$NON-NLS-1$
					if (value != null && !value.equals("")) {
						if (type == null || type.equals("")) {
							providedProperties.put(name, value);
						} else if (type.equals("String")) {
							providedProperties.put(name, value);
						} else if (type.equals("Long")) {
							providedProperties.put(name, Long.valueOf(value));
						} else if (type.equals("Double")) {
							providedProperties.put(name, Double.valueOf(value));
						} else if (type.equals("Float")) {
							providedProperties.put(name, Float.valueOf(value));
						} else if (type.equals("Integer")) {
							providedProperties
									.put(name, Integer.valueOf(value));
						} else if (type.equals("Byte")) {
							providedProperties.put(name, Byte.valueOf(value));
						} else if (type.equals("Character")) {
							providedProperties.put(name, new Character(value
									.charAt(0)));
						} else if (type.equals("Boolean")) {
							providedProperties
									.put(name, Boolean.valueOf(value));
						} else if (type.equals("Short")) {
							providedProperties.put(name, Short.valueOf(value));
						}
					}
				}
			}
			c.setProvidedInterfaces(providedInterfaces);
			c.setProperties(providedProperties);
		}
	}

	public Collection load(InputStream ins)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(ins);
		return loadServiceDescriptions(doc);
	}
}