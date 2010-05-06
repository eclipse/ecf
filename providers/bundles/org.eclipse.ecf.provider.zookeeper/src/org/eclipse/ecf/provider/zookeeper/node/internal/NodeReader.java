/*******************************************************************************
 *  Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Ahmed Aadel - initial API and implementation     
 *******************************************************************************/
package org.eclipse.ecf.provider.zookeeper.node.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.provider.zookeeper.core.DiscoverdService;
import org.eclipse.ecf.provider.zookeeper.core.internal.Localizer;
import org.eclipse.ecf.provider.zookeeper.core.internal.Notification;
import org.eclipse.ecf.provider.zookeeper.util.Logger;
import org.eclipse.ecf.provider.zookeeper.util.PrettyPrinter;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
public class NodeReader implements Watcher,
		org.apache.zookeeper.AsyncCallback.DataCallback {

	private String path;
	private DiscoverdService discovered;
	private ZooKeeper zookeeper;
	private String ip;
	boolean isNodePublished;
	private boolean isDisposed;

	public NodeReader(String path, ReadRoot readRoot) {
		Assert.isNotNull(path);
		Assert.isNotNull(readRoot);
		this.path = path;
		this.zookeeper = readRoot.getReadKeeper();
		this.ip = readRoot.getIp();
		this.zookeeper.getData(getAbsolutePath(), this, this, null);
		this.zookeeper.exists(getAbsolutePath(), this, null, null);
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public String getAbsolutePath() {
		return INode.ROOT_SLASH + getPath();
	}

	public synchronized void dispose() {
		if (null != ReadRoot.discoverdServices.remove(this.discovered
				.getServiceID().getServiceTypeID().getName())) {
			this.discovered.dispose();
			this.discovered = null;
		}
	}

	public void processResult(int rc, String p, Object ctx, byte[] data,
			Stat stat) {
		if (p == null || !p.equals(getAbsolutePath())) {
			return;
		}
		ByteArrayInputStream bis = null;
		Properties props = new Properties();
		try {
			bis = new ByteArrayInputStream(data);
			props.load(bis);
			if (props.isEmpty()) {
				return;
			}
			if (props.containsKey(Constants.OBJECTCLASS)) {
				props.put(Constants.OBJECTCLASS, ((String) props
						.get(Constants.OBJECTCLASS)).split(INode.STRING_DELIM));
			}
			if (props.containsKey(INode.NODE_PROPERTY_NAME_SCOPE)) {
				props.put(INode.NODE_PROPERTY_NAME_SCOPE, ((String) props
						.get(INode.NODE_PROPERTY_NAME_SCOPE))
						.split(INode.STRING_DELIM));
			}
			if (props.containsKey(INode.NODE_PROPERTY_NAME_PROTOCOLS)) {
				props.put(INode.NODE_PROPERTY_NAME_PROTOCOLS, ((String) props
						.get(INode.NODE_PROPERTY_NAME_PROTOCOLS))
						.split(INode.STRING_DELIM));
			}
			if (props.containsKey(INode.NODE_PROPERTY_SERVICES)) {
				props.put(INode.NODE_PROPERTY_SERVICES, ((String) props
						.get(INode.NODE_PROPERTY_SERVICES))
						.split(INode.STRING_DELIM));
			}
			bis.close();
			this.discovered = new DiscoverdService(getPath(), props);
			ReadRoot.discoverdServices.put(this.discovered.getServiceID()
					.getServiceTypeID().getName(), this.discovered);
			PrettyPrinter.prompt(PrettyPrinter.REMOTE_AVAILABLE,
					this.discovered);
			 Localizer.getSingleton().localize(
			 new Notification(this.discovered, Notification.AVAILABLE));

		} catch (IOException e) {
			Logger.log(LogService.LOG_DEBUG, e.getMessage(), e);
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return this.ip;
	}

	public void process(WatchedEvent event) {

		if (this.isDisposed) {
			// Already disposed
			return;
		}
		if (event.getState() == KeeperState.Disconnected
				|| event.getState() == KeeperState.Expired
				|| event.getType() == EventType.NodeDeleted) {
			/*
			 * This node is deleted or the connection with the server we're
			 * reading from is down. This discovered service wrapped by this
			 * node is no more available.
			 */
			dispose();
			this.isDisposed = true;
		}

	}

}