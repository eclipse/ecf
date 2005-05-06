/**
 * 
 */
package org.eclipse.ecf.provider.app;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.provider.generic.TCPServerSOContainer;


public class Connector {
	public static final int DEFAULT_PORT = TCPServerSOContainer.DEFAULT_PORT;
	public static final int DEFAULT_TIMEOUT = TCPServerSOContainer.DEFAULT_KEEPALIVE;
	public static final String DEFAULT_HOSTNAME = TCPServerSOContainer.DEFAULT_HOST;
	public static final String DEFAULT_SERVERNAME = TCPServerSOContainer.DEFAULT_NAME;
	public static final String DEFAULT_PROTOCOL = TCPServerSOContainer.DEFAULT_PROTOCOL;
	
	private final ServerConfigParser parser;
	int port = DEFAULT_PORT;
	int timeout = DEFAULT_TIMEOUT;
	String protocol = DEFAULT_PROTOCOL;
	String hostname = DEFAULT_HOSTNAME;
	List groups = new ArrayList();
	
	public Connector(ServerConfigParser parser, String protocol, String host, int port, int timeout) {
		this.parser = parser;
		if (protocol != null && !protocol.equals("")) this.protocol = protocol;
		if (host != null && !host.equals("")) this.hostname = host;
		else {
			try {
				InetAddress addr = InetAddress.getLocalHost();
				this.hostname = addr.getCanonicalHostName();
			} catch (Exception e) {
				this.hostname = "localhost";
			}
		}
		this.port = port;
		this.timeout = timeout;
	}
	public void addGroup(NamedGroup grp) {
		groups.add(grp);
	}
	public String getProtocol() {
		return protocol;
	}
	public String getHostname() {
		return hostname;
	}
	public int getPort() {
		return port;
	}
	public int getTimeout() {
		return timeout;
	}
	public List getGroups() {
		return groups;
	}
	public String getID() {
		return getProtocol()+"://"+getHostname()+":"+getPort();
	}
}