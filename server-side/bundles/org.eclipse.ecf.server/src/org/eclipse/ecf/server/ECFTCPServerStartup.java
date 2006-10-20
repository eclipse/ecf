package org.eclipse.ecf.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.provider.app.Connector;
import org.eclipse.ecf.provider.app.NamedGroup;
import org.eclipse.ecf.provider.app.ServerConfigParser;
import org.eclipse.ecf.provider.generic.SOContainerConfig;
import org.eclipse.ecf.provider.generic.TCPServerSOContainer;
import org.eclipse.ecf.provider.generic.TCPServerSOContainerGroup;

public class ECFTCPServerStartup {
	static TCPServerSOContainerGroup serverGroups[] = null;
	static List servers = new ArrayList();
	public ECFTCPServerStartup(String configFileName) throws Exception {
		InputStream ins = this.getClass().getResourceAsStream(configFileName);
		if (ins != null) {
			createServers(ins);
		} else throw new NullPointerException("config file "+configFileName+" not found");
	}
	
	public ECFTCPServerStartup(InputStream ins) throws Exception {
		if (ins != null) createServers(ins);
		else throw new NullPointerException("config not found");
	}
	protected boolean isActive() {
		return (servers.size() > 0);
	}
	protected synchronized void destroyServers() {
		for (Iterator i = servers.iterator(); i.hasNext();) {
			TCPServerSOContainer s = (TCPServerSOContainer) i.next();
			if (s != null) {
				try {
					s.dispose();
				} catch (Exception e) {
					Activator.log("Exception destroying server "
							+ s.getConfig().getID());
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
					log("ECF server created: "
							+ cont.getConfig().getID().getName());
				}
				serverGroups[j].putOnTheAir();
				j++;
			}
		}
	}
	protected void log(String output) {
		System.out.println(output);
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
		SOContainerConfig config = new SOContainerConfig(newServerID);
		return new TCPServerSOContainer(config, group, path, keepAlive);
	}
}
