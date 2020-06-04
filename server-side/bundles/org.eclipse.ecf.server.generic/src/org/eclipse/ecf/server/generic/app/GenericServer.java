/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server.generic.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PermissionCollection;
import java.util.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectHandlerPolicy;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerGroupManager;
import org.eclipse.ecf.provider.generic.*;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * This class controls all aspects of the application's execution
 */
public class GenericServer implements IApplication {

	private static Map serverGroups = new HashMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		try {
			final String[] args = mungeArguments((String[]) context.getArguments().get("application.args")); //$NON-NLS-1$
			if (args.length == 1 && (args[0].equals("-help") || args[0].equals("-h"))) { //$NON-NLS-1$ //$NON-NLS-2$
				usage();
				return IApplication.EXIT_OK;
			} else if (args.length == 2 && (args[0].equals("-config") || args[0].equals("-c"))) { //$NON-NLS-1$ //$NON-NLS-2$
				// Setup from configuration file (expected after -c <file>
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(args[1]);
					setupServerFromConfig(new ServerConfigParser().load(fis));
				} finally {
					if (fis != null)
						fis.close();
				}
			} else {
				String hostname = TCPServerSOContainer.DEFAULT_HOST;
				int port = TCPServerSOContainer.DEFAULT_PORT;
				String name = TCPServerSOContainer.DEFAULT_NAME;
				int keepAlive = TCPServerSOContainer.DEFAULT_KEEPALIVE;
				switch (args.length) {
					case 4 :
						keepAlive = Integer.parseInt(args[3]);
					case 3 :
						hostname = args[2];
					case 2 :
						name = args[1];
						if (!name.startsWith("/")) //$NON-NLS-1$
							name = "/" + name; //$NON-NLS-1$
					case 1 :
						port = Integer.parseInt(args[0]);
				}
				setupServerFromParameters(hostname, port, name, keepAlive);
			}
			synchronized (this) {
				this.wait();
			}
			return IApplication.EXIT_OK;
		} catch (final Exception e) {
			stop();
			throw e;
		}
	}

	private void usage() {
		System.out.println("Usage: eclipse.exe -application " //$NON-NLS-1$
				+ this.getClass().getName() + "[port [groupname [hostname [keepAlive]]]] | [-config|-c <configfile.xml>]"); //$NON-NLS-1$
		System.out.println("   Examples: eclipse -application org.eclipse.ecf.provider.GenericServer"); //$NON-NLS-1$
		System.out.println("             eclipse -application org.eclipse.ecf.provider.GenericServer " + 7777); //$NON-NLS-1$
		System.out.println("             eclipse -application org.eclipse.ecf.provider.GenericServer " + 7777 //$NON-NLS-1$
				+ " mygroup foobarhost.wherever.com 35000"); //$NON-NLS-1$
		System.out.println("             eclipse -application org.eclipse.ecf.provider.GenericServer -c myconfig.xml"); //$NON-NLS-1$

	}

	/**
	 * @param hostname
	 * @param port
	 * @param name
	 * @param keepAlive
	 */
	protected void setupServerFromParameters(String hostname, int port, String name, int keepAlive) throws IOException, IDCreateException {
		final String hostnamePort = hostname + ":" + port; //$NON-NLS-1$
		synchronized (serverGroups) {
			TCPServerSOContainerGroup serverGroup = (TCPServerSOContainerGroup) serverGroups.get(hostnamePort);
			if (serverGroup == null) {
				System.out.println("Putting server " + hostnamePort + " on the air..."); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					serverGroup = new TCPServerSOContainerGroup(hostname, port);
					final String url = TCPServerSOContainer.DEFAULT_PROTOCOL + "://" //$NON-NLS-1$
							+ hostnamePort + name;
					// Create
					final TCPServerSOContainer container = createServerContainer(url, serverGroup, name, keepAlive);
					// Configure
					configureServerContainer(container);
					// Put on the air
					serverGroup.putOnTheAir();
				} catch (final IOException e) {
					e.printStackTrace(System.err);
					throw e;
				} catch (IDCreateException e) {
					e.printStackTrace(System.err);
					throw e;
				}
				serverGroups.put(hostnamePort, serverGroup);
				System.out.println("GenericServerContainer " + hostnamePort + " on the air."); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				System.out.println("GenericServerContainer " + hostnamePort //$NON-NLS-1$
						+ " already on the air.  No changes made."); //$NON-NLS-1$
			}
		}

	}

	/**
	 * This method may be overridden by subclasses in order to customize the configuration of the
	 * newly created server containers (before they are put on the air).  For example, to set the appropriate
	 * connect policy.
	 * 
	 * @param container the container to configure
	 */
	protected void configureServerContainer(TCPServerSOContainer container) {
		// Setup join policy
		((ISharedObjectContainerGroupManager) container).setConnectPolicy(new JoinListener());

	}

	protected void setupServerFromConfig(List connectors) throws IOException, IDCreateException {
		for (final Iterator i = connectors.iterator(); i.hasNext();) {
			final Connector connector = (Connector) i.next();
			final String hostname = connector.getHostname();
			final int port = connector.getPort();
			final String hostnamePort = hostname + ":" + port; //$NON-NLS-1$
			TCPServerSOContainerGroup serverGroup = null;
			synchronized (serverGroups) {
				serverGroup = (TCPServerSOContainerGroup) serverGroups.get(hostnamePort);
				if (serverGroup == null) {
					System.out.println("Putting server " + hostnamePort + " on the air..."); //$NON-NLS-1$ //$NON-NLS-2$
					serverGroup = new TCPServerSOContainerGroup(hostname, port);
					final List groups = connector.getGroups();
					for (final Iterator g = groups.iterator(); g.hasNext();) {
						final NamedGroup group = (NamedGroup) g.next();
						// Create
						final TCPServerSOContainer container = createServerContainer(group.getIDForGroup(), serverGroup, group.getName(), connector.getTimeout());
						// Configure
						configureServerContainer(container);
					}
					serverGroup.putOnTheAir();
					serverGroups.put(hostnamePort, serverGroup);
					System.out.println("GenericServerContainer " + hostnamePort //$NON-NLS-1$
							+ " on the air."); //$NON-NLS-1$
				} else {
					System.out.println("GenericServerContainer " + hostnamePort //$NON-NLS-1$
							+ " already on the air.  No changes made."); //$NON-NLS-1$
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		synchronized (serverGroups) {
			for (final Iterator i = serverGroups.keySet().iterator(); i.hasNext();) {
				final TCPServerSOContainerGroup serverGroup = (TCPServerSOContainerGroup) serverGroups.get(i.next());
				serverGroup.takeOffTheAir();
				System.out.println("Taking " + serverGroup.getName() + ":" + serverGroup.getPort() + " off the air"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				final Iterator iter = serverGroup.elements();
				for (; iter.hasNext();) {
					final TCPServerSOContainer container = (TCPServerSOContainer) iter.next();
					container.dispose();
				}
			}
		}
		serverGroups.clear();
		synchronized (this) {
			this.notify();
		}
	}

	private String[] mungeArguments(String originalArgs[]) {
		if (originalArgs == null)
			return new String[0];
		final List l = new ArrayList();
		for (int i = 0; i < originalArgs.length; i++)
			if (!originalArgs[i].equals("-pdelaunch")) //$NON-NLS-1$
				l.add(originalArgs[i]);
		return (String[]) l.toArray(new String[] {});
	}

	private static TCPServerSOContainer createServerContainer(String id, TCPServerSOContainerGroup group, String path, int keepAlive) throws IDCreateException {
		System.out.println("  Creating container with id=" + id + " keepAlive=" + keepAlive); //$NON-NLS-1$ //$NON-NLS-2$ 
		final ID newServerID = IDFactory.getDefault().createStringID(id);
		final SOContainerConfig config = new SOContainerConfig(newServerID);
		return new TCPServerSOContainer(config, group, path, keepAlive);
	}

	static class JoinListener implements IConnectHandlerPolicy {
		public PermissionCollection checkConnect(Object addr, ID fromID, ID targetID, String targetGroup, Object joinData) throws Exception {
			System.out.println("CLIENT CONNECT: fromAddress=" + addr + ";fromID=" + fromID + ";targetGroup=" + targetGroup); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			return null;
		}

		public void refresh() {
			// nothing to do
		}

	}

}
