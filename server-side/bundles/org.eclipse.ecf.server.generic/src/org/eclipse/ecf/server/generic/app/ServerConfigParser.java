/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server.generic.app;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ServerConfigParser {

	public static final String SERVER_ELEMENT = "server"; //$NON-NLS-1$
	public static final String CONNECTOR_ELEMENT = "connector"; //$NON-NLS-1$
	public static final String GROUP_ELEMENT = "group"; //$NON-NLS-1$

	public static final String PROTOCOL_ATTR = "protocol"; //$NON-NLS-1$
	public static final String HOSTNAME_ATTR = "hostname"; //$NON-NLS-1$
	public static final String PORT_ATTR = "port"; //$NON-NLS-1$
	public static final String TIMEOUT_ATTR = "timeout"; //$NON-NLS-1$
	public static final String NAME_ATTR = "name"; //$NON-NLS-1$

	protected void findElementsNamed(Node top, String name, List aList) {
		int type = top.getNodeType();
		switch (type) {
			case Node.DOCUMENT_TYPE_NODE :
				// Print entities if any
				NamedNodeMap nodeMap = ((DocumentType) top).getEntities();
				for (int i = 0; i < nodeMap.getLength(); i++) {
					Entity entity = (Entity) nodeMap.item(i);
					findElementsNamed(entity, name, aList);
				}
				break;
			case Node.ELEMENT_NODE :
				String elementName = top.getNodeName();
				if (name.equals(elementName)) {
					aList.add(top);
				}
			default :
				for (Node child = top.getFirstChild(); child != null; child = child.getNextSibling()) {
					findElementsNamed(child, name, aList);
				}
		}
	}

	protected List processConnectorNodes(List connectorNodes) {
		List res = new ArrayList();
		for (Iterator i = connectorNodes.iterator(); i.hasNext();) {
			Node n = (Node) i.next();
			String ports = getAttributeValue(n, PORT_ATTR);
			int port = Connector.DEFAULT_PORT;
			if (ports != null) {
				try {
					Integer porti = Integer.valueOf(ports);
					port = porti.intValue();
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			String timeouts = getAttributeValue(n, TIMEOUT_ATTR);
			int timeout = Connector.DEFAULT_TIMEOUT;
			if (timeouts != null) {
				try {
					Integer timeouti = Integer.valueOf(timeouts);
					timeout = timeouti.intValue();
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			String prot = getAttributeValue(n, PROTOCOL_ATTR);
			String host = getAttributeValue(n, HOSTNAME_ATTR);
			Connector c = new Connector(prot, host, port, timeout);
			processConnector(n, c);
			res.add(c);
		}
		return res;
	}

	protected void processConnector(Node n, Connector c) {
		List groupList = new ArrayList();
		findElementsNamed(n, GROUP_ELEMENT, groupList);
		for (Iterator i = groupList.iterator(); i.hasNext();) {
			Node node = (Node) i.next();
			String name = getAttributeValue(node, NAME_ATTR);
			if (name != null && !name.equals("")) { //$NON-NLS-1$
				NamedGroup g = new NamedGroup(name);
				c.addGroup(g);
				g.setParent(c);
			}
		}
	}

	protected List loadConnectors(Document doc) {
		List ps = new ArrayList();
		findElementsNamed(doc, CONNECTOR_ELEMENT, ps);
		return processConnectorNodes(ps);
	}

	protected String getAttributeValue(Node node, String attrName) {
		NamedNodeMap attrs = node.getAttributes();
		Node attrNode = attrs.getNamedItem(attrName);
		if (attrNode != null) {
			return attrNode.getNodeValue();
		}
		return ""; //$NON-NLS-1$
	}

	public List load(InputStream ins) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
		try {
			dbf.setFeature(FEATURE, true);
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("ParserConfigurationException was thrown. The feature '"
		+ FEATURE + "' is not supported by your XML processor.", e);
		}
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(ins);
		return loadConnectors(doc);
	}

	public static void main(String[] args) throws Exception {
		InputStream ins = new FileInputStream(args[0]);
		ServerConfigParser configParser = new ServerConfigParser();
		configParser.load(ins);
	}
}
