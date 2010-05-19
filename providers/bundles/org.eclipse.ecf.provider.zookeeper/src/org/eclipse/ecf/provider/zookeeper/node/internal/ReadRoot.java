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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.provider.zookeeper.core.DiscoverdService;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainer;
import org.eclipse.ecf.provider.zookeeper.util.Logger;
import org.osgi.service.log.LogService;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
public class ReadRoot implements Watcher, ChildrenCallback {
	ZooKeeper readKeeper;
	String ip;
	private WatchManager watchManager;
	private boolean isConnected;
	private Map<String, NodeReader> nodeReaders = Collections
			.synchronizedMap(new HashMap<String, NodeReader>());
	public static Map<String, DiscoverdService> discoverdServices = Collections
			.synchronizedMap(new HashMap<String, DiscoverdService>());
	public static Map<String, List<DiscoverdService>> perTypeDiscoverdServices = Collections
			.synchronizedMap(new HashMap<String, List<DiscoverdService>>());

	ReadRoot(String ip, WatchManager watchManager) {
		Assert.isNotNull(ip);
		Assert.isNotNull(watchManager);
		this.ip = ip;
		this.watchManager = watchManager;
		connect();
	}

	public synchronized void process(final WatchedEvent event) {
		ZooDiscoveryContainer.CACHED_THREAD_POOL.execute(new Runnable() {
			public void run() {
				switch (event.getState()) {
				case Disconnected:
					ReadRoot.this.isConnected = false;
					connect();
					break;
				case Expired:
					ReadRoot.this.isConnected = false;
					connect();
					break;
				case SyncConnected:
					if (!ReadRoot.this.isConnected) {
						ReadRoot.this.isConnected = true;
						ReadRoot.this.watchManager
								.addZooKeeper(ReadRoot.this.readKeeper);
						ReadRoot.this.readKeeper.exists(INode.ROOT,
								ReadRoot.this, null, null);
						ReadRoot.this.readKeeper.getChildren(INode.ROOT,
								ReadRoot.this, ReadRoot.this, null);
					}
					break;

				// ignore @deprecated cases
				}
				switch (event.getType()) {
				case NodeDeleted:
					if (event.getPath() == null
							|| event.getPath().equals(INode.ROOT))
						break;
					ReadRoot.this.nodeReaders.remove(event.getPath());
					break;
				case NodeChildrenChanged:
					if (ReadRoot.this.isConnected) {
						ReadRoot.this.readKeeper.exists(INode.ROOT,
								ReadRoot.this, null, null);
						ReadRoot.this.readKeeper.getChildren(INode.ROOT,
								ReadRoot.this, ReadRoot.this, null);
					}
					break;
				}
			}
		});
	}

	private synchronized void connect() {

		// this.isConnected = this.readKeeper != null &&
		// this.readKeeper.getState() == ZooKeeper.States.CONNECTED &
		// this.readKeeper.getState() != ZooKeeper.States.CONNECTING;

		if (this.isConnected) {
			return;
		}
		try {
			this.nodeReaders.clear();
			if (this.readKeeper != null) {
				// discard the current stale reader
				this.readKeeper.close();
				this.readKeeper = null;
				this.watchManager.removeZooKeeper(this.readKeeper);
			}
			// try reconnecting
			this.readKeeper = new ZooKeeper(this.ip, 3000, this);

		} catch (Exception e) {
			Logger.log(LogService.LOG_DEBUG, e.getMessage(), e);
		}
	}

	public synchronized void processResult(int rc, final String path,
			Object ctx, final List<String> children) {

		// FIXME race condition when two servers run on the same machine
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//		}

		ZooDiscoveryContainer.CACHED_THREAD_POOL.execute(new Runnable() {
			public void run() {
				if (path == null || children == null || children.size() == 0) {
					/* No children available yet, set a watch on it */
					ReadRoot.this.readKeeper.getChildren(INode.ROOT,
							ReadRoot.this, ReadRoot.this, null);
					return;
				}
				for (String p : children) {
					// if (Geo.isLocal(p)) {
					// /* locals need not to be discovered */
					// continue;
					// }
					if (!ReadRoot.this.nodeReaders.containsKey(p)) {
						/* launch a new reader to handle this node's data */
						NodeReader nr = new NodeReader(p, ReadRoot.this);
						/* watch this very path for deletion */
						ReadRoot.this.readKeeper.exists(nr.getAbsolutePath(),
								ReadRoot.this, null, null);
						ReadRoot.this.nodeReaders.put(nr.getPath(), nr);
					}
				}
			}
		});

	}

	public ZooKeeper getReadKeeper() {
		return this.readKeeper;
	}

	public String getIp() {
		return this.ip;
	}

}