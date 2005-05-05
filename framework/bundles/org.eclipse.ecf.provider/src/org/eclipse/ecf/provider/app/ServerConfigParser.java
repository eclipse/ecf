package org.eclipse.ecf.provider.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ServerConfigParser {

	public static final int DEFAULT_PORT = 3282;
	public static final long DEFAULT_TIMEOUT = 10000L;
	
	public static final String SERVER_ELEMENT = "server";
	public static final String CONNECTOR_ELEMENT = "connector";
	public static final String GROUP_ELEMENT = "group";
	
	public static final String PORT_ATTR = "port";
	public static final String TIMEOUT_ATTR = "timeout";
	public static final String NAME_ATTR = "name";
	
	protected int defaultPort = DEFAULT_PORT;
	protected long defaultTimeout = DEFAULT_TIMEOUT;
	
	class Connector {
		int port = defaultPort;
		long timeout = defaultTimeout;
		List groups = new ArrayList();
		
		public Connector() {
			
		}
		public Connector(int port, long timeout) {
			this.port = port;
			this.timeout = timeout;
		}
		public void addGroup(NamedGroup grp) {
			groups.add(grp);
		}
		public int getPort() {
			return port;
		}
		public long getTimeout() {
			return timeout;
		}
		public List getGroups() {
			return groups;
		}
	}
	class NamedGroup {
		Connector parent;
		String name;
		
		public NamedGroup(String name) {
			this.name = name;
		}
		protected void setParent(Connector c) {
			this.parent = c;
		}
		public String getGroup() {
			return name;
		}
	}
    protected void findElementsNamed(Node top, String name, List aList) {
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
	protected List processConnectorNodes(List connectorNodes) {
		List res = new ArrayList();
		for(Iterator i=connectorNodes.iterator(); i.hasNext(); ) {
			Node n = (Node) i.next();
			String ports = getAttributeValue(n,PORT_ATTR);
			int port = defaultPort;
			if (ports != null) {
				try {
					Integer porti = new Integer(ports);
					port = porti.intValue();
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			String timeouts = getAttributeValue(n,TIMEOUT_ATTR);
			long timeout = defaultTimeout;
			if (timeouts != null) {
				try {
					Long timeouti = new Long(timeouts);
					timeout = timeouti.longValue();
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			Connector c = new Connector(port,timeout);
			processConnector(n,c);
			res.add(c);
		}
		return res;
	}
	protected void processConnector(Node n, Connector c) {
		List groupList = new ArrayList();
		findElementsNamed(n,GROUP_ELEMENT,groupList);
		for(Iterator i=groupList.iterator(); i.hasNext(); ) {
			Node node = (Node) i.next();
			String name = getAttributeValue(n,NAME_ATTR);
			if (name != null && !name.equals("")) {
				NamedGroup g = new NamedGroup(name);
				c.addGroup(g);
			}
		}
	}
	protected List loadConnectors(Document doc) {
    	List ps = new ArrayList();
    	findElementsNamed(doc,CONNECTOR_ELEMENT,ps);
		return processConnectorNodes(ps);
	}
    protected String getAttributeValue(Node node, String attrName) {
        NamedNodeMap attrs = node.getAttributes();
        Node attrNode = attrs.getNamedItem(attrName);
        if (attrNode != null) {
            return attrNode.getNodeValue();
        } else
            return "";
    }
	public List load(InputStream ins) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(ins);
		return loadConnectors(doc);
	}
}
