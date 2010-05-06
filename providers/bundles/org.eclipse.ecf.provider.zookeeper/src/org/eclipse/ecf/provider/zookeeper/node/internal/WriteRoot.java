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

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.provider.zookeeper.util.Logger;
import org.osgi.service.log.LogService;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
class WriteRoot implements Watcher {
	private ZooKeeper writeKeeper;
	private String ip;
	private WatchManager watchManager;
	private boolean isConnected = false;

	WriteRoot(String ip, WatchManager watchManager) {
		Assert.isNotNull(ip);
		Assert.isNotNull(watchManager);
		this.ip = ip;
		this.watchManager = watchManager;
		initWriteKeeper();
	}

	@SuppressWarnings( { "incomplete-switch" })
	public void process(WatchedEvent event) {
		switch (event.getState()) {
		case Disconnected:
			this.isConnected = false;
			this.watchManager.unpublishAll();
			connect();
			break;
		case Expired:
			this.isConnected = false;
			this.watchManager.unpublishAll();

			connect();
			break;
		case SyncConnected:
			if (!this.isConnected) {
				this.isConnected = true;
				this.watchManager.republishAll();
			}
			break;
		// ignore @deprecated cases
		}
	}

	private void connect() {
		/*
		 * this.isConnected = this.writeKeeper != null &&
		 * this.writeKeeper.getState() == ZooKeeper.States.CONNECTED &
		 * this.writeKeeper.getState() != ZooKeeper.States.CONNECTING;
		 */
		if (this.isConnected) {
			return;
		}
		try {
			if (this.writeKeeper != null) {
				this.writeKeeper.close();
				this.writeKeeper = null;
				this.watchManager.removeZooKeeper(this.writeKeeper);
			}
			this.writeKeeper = new ZooKeeper(this.ip, 3000, this);

		} catch (Exception e) {
			Logger.log(LogService.LOG_DEBUG, e.getMessage(), e);
		}
	}

	private void initWriteKeeper() {
		try {
			this.writeKeeper = new ZooKeeper(this.ip, 3000, this);
			while (!this.isConnected) {
				try {
					Stat s = this.writeKeeper.exists(INode.ROOT, this);
					this.isConnected = true;
					if (s == null) {
						this.writeKeeper.create(INode.ROOT, new byte[0],
								Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}

				} catch (KeeperException e) {
					if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
						this.isConnected = false;
					}
					Logger.log(LogService.LOG_INFO,
							"Trying to connect to " + this.ip, e); //$NON-NLS-1$
				}
			}
			synchronized (this) {
				this.notifyAll();
			}
			this.watchManager.addZooKeeper(this.writeKeeper);

		} catch (Exception e) {
			Logger.log(LogService.LOG_DEBUG, e.getMessage(), e);
		}
	}

	public ZooKeeper getWriteKeeper() {
		return this.writeKeeper;
	}

	public boolean isConnected() {
		return this.isConnected;
	}

}