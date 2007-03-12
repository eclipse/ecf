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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.internal.server.generic.Activator;
import org.eclipse.ecf.internal.server.generic.Messages;
import org.eclipse.ecf.provider.app.Connector;
import org.eclipse.ecf.provider.app.NamedGroup;
import org.eclipse.ecf.provider.app.ServerConfigParser;
import org.eclipse.ecf.provider.generic.SOContainerConfig;
import org.eclipse.ecf.provider.generic.TCPServerSOContainer;
import org.eclipse.ecf.provider.generic.TCPServerSOContainerGroup;
import org.eclipse.osgi.util.NLS;

public class ServerStarter {

	static TCPServerSOContainerGroup serverGroups[] = null;

	static List servers = new ArrayList();

	public ServerStarter(InputStream ins) {
		try {
			createServers(ins);
		} catch (Exception e) {
			Activator.log(Messages.ServerStarter_EXCEPTION_CREATING_SERVER,e); //$NON-NLS-1$
		}
	}

	protected boolean isActive() {
		return (servers.size() > 0);
	}

	public synchronized void destroyServers() {
		for (Iterator i = servers.iterator(); i.hasNext();) {
			TCPServerSOContainer server = (TCPServerSOContainer) i.next();
			if (server != null) {
				try {
					server.dispose();
				} catch (Exception e) {
					Activator.log(Messages.ServerStarter_EXCEPTION_DISPOSING_SERVER,e); //$NON-NLS-1$
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

	protected synchronized void createServers(InputStream ins) throws Exception {
		ServerConfigParser scp = new ServerConfigParser();
		List connectors = scp.load(ins);
		if (connectors != null) {
			serverGroups = new TCPServerSOContainerGroup[connectors.size()];
			int j = 0;
			for (Iterator i = connectors.iterator(); i.hasNext();) {
				Connector connect = (Connector) i.next();
				serverGroups[j] = createServerGroup(connect.getHostname(),
						connect.getPort());
				List groups = connect.getGroups();
				for (Iterator g = groups.iterator(); g.hasNext();) {
					NamedGroup group = (NamedGroup) g.next();
					TCPServerSOContainer cont = createServerContainer(group
							.getIDForGroup(), serverGroups[j], group.getName(),
							connect.getTimeout());
					servers.add(cont);
					log(NLS.bind(Messages.ServerStarter_STARTING_SERVER,cont.getID().getName())); //$NON-NLS-1$
				}
				serverGroups[j].putOnTheAir();
				j++;
			}
		}
	}

	protected void log(String output) {
		Activator.log(output);
	}

	protected TCPServerSOContainerGroup createServerGroup(String name, int port) {
		TCPServerSOContainerGroup group = new TCPServerSOContainerGroup(name,
				port);
		return group;
	}

	protected TCPServerSOContainer createServerContainer(String id,
			TCPServerSOContainerGroup group, String path, int keepAlive)
			throws IDCreateException {
		ID newServerID = IDFactory.getDefault().createStringID(id);
		return new TCPServerSOContainer(new SOContainerConfig(newServerID),
				group, path, keepAlive);
	}
}
