/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.server.generic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.internal.server.generic.Activator;
import org.eclipse.ecf.internal.server.generic.Messages;
import org.eclipse.ecf.provider.app.Connector;
import org.eclipse.ecf.provider.app.NamedGroup;
import org.eclipse.ecf.provider.app.ServerConfigParser;
import org.eclipse.ecf.provider.generic.SOContainerConfig;
import org.eclipse.ecf.provider.generic.TCPServerSOContainer;
import org.eclipse.ecf.provider.generic.TCPServerSOContainerGroup;
import org.eclipse.osgi.util.NLS;

public class ServerManager {

	static TCPServerSOContainerGroup serverGroups[] = null;

	static Map servers = new HashMap();

	public static final String EXTENSION_POINT_NAME = "configuration";

	public static final String EXTENSION_POINT = Activator.PLUGIN_ID + "."
			+ EXTENSION_POINT_NAME;

	public static final String CONFIGURATION_ELEMENT = "configuration";
	public static final String CONNECTOR_ELEMENT = "connector";
	public static final String GROUP_ELEMENT = "group";

	public static final String HOSTNAME_ATTR = "hostname";
	public static final String PORT_ATTR = "port";
	public static final String KEEPALIVE_ATTR = "keepAlive";
	public static final String NAME_ATTR = "name";

	public ServerManager() {
		IExtensionRegistry reg = Activator.getDefault().getExtensionRegistry();
		try {
			if (reg != null) {
				createServersFromExtensionRegistry(reg);
			} else {
				createServersFromConfigurationFile(Activator.getDefault()
						.getBundle().getEntry(Messages.Activator_SERVER_XML)
						.openStream());
			}
		} catch (Exception e) {
			Activator.log(Messages.ServerStarter_EXCEPTION_CREATING_SERVER, e);
		}
	}

	public synchronized ISharedObjectContainer getServer(ID id) {
		if (id == null)
			return null;
		return (ISharedObjectContainer) servers.get(id);
	}

	private void createServersFromExtensionRegistry(IExtensionRegistry registry)
			throws Exception {
		IExtensionPoint extensionPoint = registry
				.getExtensionPoint(EXTENSION_POINT);
		if (extensionPoint == null)
			return;
		IConfigurationElement[] elements = extensionPoint
				.getConfigurationElements();
		List connectors = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String portString = element.getAttribute(PORT_ATTR);
			int port = TCPServerSOContainer.DEFAULT_PORT;
			if (portString != null)
				port = Integer.parseInt(portString);
			int keepAlive = TCPServerSOContainer.DEFAULT_KEEPALIVE;
			String keepAliveString = element.getAttribute(KEEPALIVE_ATTR);
			if (keepAliveString != null)
				keepAlive = Integer.parseInt(keepAliveString);
			Connector connector = new Connector(null, element
					.getAttribute(HOSTNAME_ATTR), port, keepAlive);
			IConfigurationElement[] groupElements = element
					.getChildren(GROUP_ELEMENT);
			for (int j = 0; j < groupElements.length; j++) {
				String groupName = groupElements[i].getAttribute(NAME_ATTR);
				if (groupName != null)
					connector.addGroup(new NamedGroup(groupName));
			}
			connectors.add(connector);
		}
		createServersFromConnectorList(connectors);
	}

	protected boolean isActive() {
		return (servers.size() > 0);
	}

	public synchronized void closeServers() {
		for (Iterator i = servers.keySet().iterator(); i.hasNext();) {
			ID serverID = (ID) i.next();
			TCPServerSOContainer server = (TCPServerSOContainer) servers
					.get(serverID);
			if (server != null) {
				try {
					server.dispose();
				} catch (Exception e) {
					Activator.log(
							Messages.ServerStarter_EXCEPTION_DISPOSING_SERVER,
							e); //$NON-NLS-1$
				}
			}
		}
		servers.clear();
		if (serverGroups != null) {
			for (int i = 0; i < serverGroups.length; i++) {
				serverGroups[i].takeOffTheAir();
			}
			serverGroups = null;
		}
	}

	private synchronized void createServersFromConnectorList(List connectors)
			throws IDCreateException, IOException {
		serverGroups = new TCPServerSOContainerGroup[connectors.size()];
		int j = 0;
		for (Iterator i = connectors.iterator(); i.hasNext();) {
			Connector connect = (Connector) i.next();
			serverGroups[j] = createServerGroup(connect);
			List groups = connect.getGroups();
			for (Iterator g = groups.iterator(); g.hasNext();) {
				NamedGroup group = (NamedGroup) g.next();
				TCPServerSOContainer cont = createServerContainer(group
						.getIDForGroup(), serverGroups[j], group.getName(),
						connect.getTimeout());
				servers.put(cont.getID(), cont);
				Activator.log(NLS.bind(Messages.ServerStarter_STARTING_SERVER,
						cont.getID().getName())); //$NON-NLS-1$
			}
			serverGroups[j].putOnTheAir();
			j++;
		}
	}

	private void createServersFromConfigurationFile(InputStream ins)
			throws Exception {
		ServerConfigParser scp = new ServerConfigParser();
		List connectors = scp.load(ins);
		if (connectors != null)
			createServersFromConnectorList(connectors);
	}

	private TCPServerSOContainerGroup createServerGroup(Connector connector) {
		TCPServerSOContainerGroup group = new TCPServerSOContainerGroup(
				connector.getHostname(), connector.getPort());
		return group;
	}

	private TCPServerSOContainer createServerContainer(String id,
			TCPServerSOContainerGroup group, String path, int keepAlive)
			throws IDCreateException {
		ID newServerID = IDFactory.getDefault().createStringID(id);
		return new TCPServerSOContainer(new SOContainerConfig(newServerID),
				group, path, keepAlive);
	}
}
