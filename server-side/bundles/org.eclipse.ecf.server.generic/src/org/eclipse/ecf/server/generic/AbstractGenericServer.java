package org.eclipse.ecf.server.generic;

import java.io.IOException;
import java.security.PermissionCollection;
import java.util.*;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectHandlerPolicy;
import org.eclipse.ecf.internal.server.generic.Activator;
import org.eclipse.ecf.provider.generic.*;

public abstract class AbstractGenericServer {

	protected TCPServerSOContainerGroup serverGroup;

	public AbstractGenericServer(String host, int port) {
		this.serverGroup = new TCPServerSOContainerGroup(host, port);
	}

	/**
	 * @since 2.0
	 */
	public GenericServerContainer getFirstServerContainer() {
		return getServerContainer(0);
	}

	public List /* GenericServerContainer */getServerContainers() {
		List result = new ArrayList();
		for (Iterator i = serverGroup.elements(); i.hasNext();) {
			result.add(i.next());
		}
		return result;
	}

	/**
	 * @since 2.0
	 */
	public GenericServerContainer getServerContainer(int index) {
		return (GenericServerContainer) getServerContainers().get(index);
	}

	protected void putOnTheAir() throws IOException {
		if (!serverGroup.isOnTheAir())
			serverGroup.putOnTheAir();
	}

	protected void takeOffTheAir() {
		if (serverGroup.isOnTheAir())
			serverGroup.takeOffTheAir();
	}

	public synchronized void start(String path, int keepAlive) throws Exception {
		createAndInitializeServer(path, keepAlive);
		putOnTheAir();
	}

	public synchronized void stop() {
		if (serverGroup != null) {
			serverGroup.takeOffTheAir();
		}
		List servers = getServerContainers();
		for (Iterator i = servers.iterator(); i.hasNext();) {
			GenericServerContainer s = (GenericServerContainer) i.next();
			s.ejectAllGroupMembers("Shutting down immediately"); //$NON-NLS-1$
			s.dispose();
		}
	}

	protected void createAndInitializeServer(String path) throws IDCreateException {
		createAndInitializeServer(path, TCPServerSOContainer.DEFAULT_KEEPALIVE);
	}

	protected void createAndInitializeServer(String path, int keepAlive) throws IDCreateException {
		if (path == null || path.equals("")) //$NON-NLS-1$
			throw new NullPointerException("Cannot create ID with null or empty path"); //$NON-NLS-1$
		GenericServerContainer s = new GenericServerContainer(this, createServerConfig(path), serverGroup, path, keepAlive);
		IContainerManager containerManager = Activator.getDefault().getContainerManager();
		if (containerManager != null) {
			ContainerTypeDescription ctd = containerManager.getContainerFactory().getDescriptionByName("ecf.generic.server"); //$NON-NLS-1$
			containerManager.addContainer(s, ctd);
		}
		IConnectHandlerPolicy policy = createConnectHandlerPolicy(s, path);
		if (policy != null)
			s.setConnectPolicy(policy);
	}

	protected PermissionCollection checkConnect(Object address, ID fromID, ID targetID, String targetGroup, Object connectData) throws Exception {
		return null;
	}

	protected abstract void handleDisconnect(ID targetID);

	protected abstract void handleEject(ID targetID);

	/**
	 * @since 2.0
	 */
	protected IConnectHandlerPolicy createConnectHandlerPolicy(GenericServerContainer s, String path) {
		return new IConnectHandlerPolicy() {
			public PermissionCollection checkConnect(Object address, ID fromID, ID targetID, String targetGroup, Object connectData) throws Exception {
				return AbstractGenericServer.this.checkConnect(address, fromID, targetID, targetGroup, connectData);
			}

			public void refresh() {
				// do nothing
			}
		};
	}

	protected ID createServerIDFromPath(String path) throws IDCreateException {
		if (!path.startsWith("/"))path = "/" + path; //$NON-NLS-1$//$NON-NLS-2$
		String id = TCPServerSOContainer.DEFAULT_PROTOCOL + "://" //$NON-NLS-1$
				+ getHost() + ":" + getPort() + path; //$NON-NLS-1$
		return IDFactory.getDefault().createStringID(id);
	}

	protected SOContainerConfig createServerConfig(String path) throws IDCreateException {
		return new SOContainerConfig(createServerIDFromPath(path));
	}

	protected String getHost() {
		return this.serverGroup.getName();
	}

	protected int getPort() {
		return this.serverGroup.getPort();
	}

}
