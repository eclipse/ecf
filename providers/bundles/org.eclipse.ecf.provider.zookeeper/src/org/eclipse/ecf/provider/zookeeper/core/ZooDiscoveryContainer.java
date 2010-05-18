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
package org.eclipse.ecf.provider.zookeeper.core;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.NIOServerCnxn.Factory;
import org.apache.zookeeper.server.PurgeTxnLog;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceTypeListener;
import org.eclipse.ecf.discovery.ServiceContainerEvent;
import org.eclipse.ecf.discovery.ServiceTypeContainerEvent;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.provider.zookeeper.core.internal.Advertiser;
import org.eclipse.ecf.provider.zookeeper.core.internal.Configuration;
import org.eclipse.ecf.provider.zookeeper.core.internal.Configurator;
import org.eclipse.ecf.provider.zookeeper.core.internal.Localizer;
import org.eclipse.ecf.provider.zookeeper.node.internal.WatchManager;
import org.eclipse.ecf.provider.zookeeper.util.Geo;
import org.eclipse.ecf.provider.zookeeper.util.Logger;
import org.eclipse.ecf.provider.zookeeper.util.PrettyPrinter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
public class ZooDiscoveryContainer extends AbstractDiscoveryContainerAdapter {

	private static ZooDiscoveryContainer discovery;
	public static final ExecutorService CACHED_THREAD_POOL = Executors
			.newCachedThreadPool();
	private QuorumPeer quorumPeer;
	// public final static int DEFAUL_PORT = 2181;

	private Properties DiscoveryProperties = new Properties();
	protected Advertiser advertiser;
	protected Localizer localizer;
	protected Thread zookeeperThread;
	private ZooKeeperServer zooKeeperServer;
	private ID targetId;
	protected static boolean isQuorumPeerReady;
	private ZooDiscoveryNamespace namespace;
	private Map<String, WatchManager> allWatchManagers = new HashMap<String, WatchManager>();
	private boolean isConnected;
	private static boolean isDisposed;

	public enum FLAVOR {
		STANDALONE, CENTRALIZED, REPLICATED;

		public String toString() {
			switch (this) {
			case STANDALONE:
				return DefaultDiscoveryConfig.ZOODISCOVERY_FLAVOR_STANDALONE;
			case CENTRALIZED:
				return DefaultDiscoveryConfig.ZOODISCOVERY_FLAVOR_CENTRALIZED;
			case REPLICATED:
				return DefaultDiscoveryConfig.ZOODISCOVERY_FLAVOR_REPLICATED;
			}
			throw new AssertionError("Unsupported configuration");
		}
	}

	private ZooDiscoveryContainer() {
		super(ZooDiscoveryNamespace.NAME, Configurator.INSTANCE);
		this.namespace = new ZooDiscoveryNamespace();
	}

	public static ZooDiscoveryContainer getSingleton() {
		if (discovery == null) {
			discovery = new ZooDiscoveryContainer();
		}
		isDisposed = false;
		return discovery;
	}

	public void init(ServiceReference reference) {
		Configuration conf = Configurator.INSTANCE.createConfig(reference)
				.configure();
		doStart(conf);

	}

	private void init(ID targetID) {
		Configuration conf = Configurator.INSTANCE.createConfig(targetID)
				.configure();
		doStart(conf);
		isConnected = true;
	}

	private void doStart(final Configuration conf) {
		final WatchManager watchManager = new WatchManager(conf);
		allWatchManagers.put(conf.toString(), watchManager);
		this.advertiser = Advertiser.getSingleton(watchManager);
		this.localizer = Localizer.getSingleton();
		if (conf.isCentralized()) {
			if (Geo.getHost().equals(conf.getServerIps().split(":")[0])) { //$NON-NLS-1$
				CACHED_THREAD_POOL.execute(new Runnable() {
					public void run() {
						startStandAlone(conf);
						try {
							ZooDiscoveryContainer.this.zookeeperThread.join();
						} catch (InterruptedException e) {
							Logger.log(LogService.LOG_ERROR, e.getMessage(), e);
						}
						watchManager.watch();
						ZooDiscoveryContainer.this.localizer.init();
					}
				});

			} else {
				watchManager.watch();
				this.localizer.init();
			}

		} else if (conf.isQuorum()) {
			CACHED_THREAD_POOL.execute(new Runnable() {
				public void run() {
					startQuorumPeer(conf);
					watchManager.watch();
					ZooDiscoveryContainer.this.localizer.init();
				}
			});
		}

		else if (conf.isStandAlone()) {
			CACHED_THREAD_POOL.execute(new Runnable() {
				public void run() {
					startStandAlone(conf);
					try {
						ZooDiscoveryContainer.this.zookeeperThread.join();
					} catch (InterruptedException e) {
						Logger.log(LogService.LOG_ERROR, e.getMessage(), e);
					}
					watchManager.watch();
					ZooDiscoveryContainer.this.localizer.init();
				}
			});
		}
	}

	/**
	 * Start a ZooKeeer server locally to write nodes to. Implied by
	 * {@link IDiscoveryConfig#ZOODISCOVERY_FLAVOR_STANDALONE} configuration.
	 * 
	 * @param conf
	 */
	void startStandAlone(final Configuration conf) {

		if (this.zooKeeperServer != null && this.zooKeeperServer.isRunning())
			return;
		else if (this.zooKeeperServer != null
				&& !this.zooKeeperServer.isRunning())
			try {
				this.zooKeeperServer.startup();
				return;
			} catch (Exception e) {
				Logger.log(LogService.LOG_DEBUG,
						"Zookeeper server cannot be started! ", e);//$NON-NLS-1$				
			}

		// create brand new zooKeeper server. FIXME double cake?
		this.zookeeperThread = new Thread(new Runnable() {
			public void run() {
				try {
					ZooDiscoveryContainer.this.zooKeeperServer = new ZooKeeperServer();
					FileTxnSnapLog fileTxnSnapLog = new FileTxnSnapLog(conf
							.getZookeeperData(), conf.getZookeeperData());
					ZooDiscoveryContainer.this.zooKeeperServer
							.setTxnLogFactory(fileTxnSnapLog);
					ZooDiscoveryContainer.this.zooKeeperServer.setTickTime(conf
							.getTickTime());
					System.out.println(conf.getClientPort());
					// ZooDiscoveryContainer.this.zooKeeperServer
					// .setServerCnxnFactory(new NIOServerCnxn.Factory(
					// new InetSocketAddress(conf.getServerPort())));
					Factory cnxnFactory = new NIOServerCnxn.Factory(
							new InetSocketAddress(conf.getClientPort()));
					cnxnFactory
							.startup(ZooDiscoveryContainer.this.zooKeeperServer);
				} catch (Exception e) {
					Logger.log(
							LogService.LOG_ERROR,
							"Zookeeper server cannot be started! Possibly another instance is already running. ",
							e);
				}
			}
		});
		this.zookeeperThread.setDaemon(true);
		this.zookeeperThread.start();

	}

	/**
	 * Start a local ZooKeeer server to write nodes to. It plays as a peer
	 * within a replicated servers configuration. Implied by
	 * {@link IDiscoveryConfig#ZOODISCOVERY_FLAVOR_REPLICATED} configuration.
	 * 
	 * @param conf
	 */
	void startQuorumPeer(final Configuration conf) {
		if (this.quorumPeer != null && this.quorumPeer.isAlive()) {
			return;
		} else if (this.quorumPeer != null && !this.quorumPeer.isAlive()) {
			this.quorumPeer.start();
			return;
		}
		try {
			final QuorumPeerConfig quorumPeerConfig = new QuorumPeerConfig();
			quorumPeerConfig.parse(conf.getConfFile());
			QuorumPeer.Factory qpFactory = new QuorumPeer.Factory() {
				public QuorumPeer create(NIOServerCnxn.Factory cnxnFactory)
						throws IOException {
					ServerConfig serverConfig = new ServerConfig();
					serverConfig.readFrom(quorumPeerConfig);
					QuorumPeer peer = new QuorumPeer(
							quorumPeerConfig.getServers(), new File(
									serverConfig.getDataDir()), new File(
									serverConfig.getDataLogDir()),
							quorumPeerConfig.getElectionAlg(),
							quorumPeerConfig.getServerId(),
							quorumPeerConfig.getTickTime(),
							quorumPeerConfig.getInitLimit(),
							quorumPeerConfig.getSyncLimit(), cnxnFactory,
							quorumPeerConfig.getQuorumVerifier());
					ZooDiscoveryContainer.this.quorumPeer = peer;
					return peer;
				}

				public NIOServerCnxn.Factory createConnectionFactory()
						throws IOException {
					return new NIOServerCnxn.Factory(
							quorumPeerConfig.getClientPortAddress());
				}
			};
			quorumPeer = qpFactory.create(qpFactory.createConnectionFactory());
			quorumPeer.start();
			quorumPeer.setDaemon(true);
			isQuorumPeerReady = true;
		} catch (Exception e) {
			Logger.log(LogService.LOG_ERROR,
					"Zookeeper quorum cannot be started! ", e); //$NON-NLS-1$
			isQuorumPeerReady = false;
		}
	}

	public void setDiscoveryProperties(Properties discoveryProperties) {
		this.DiscoveryProperties = discoveryProperties;
	}

	public Properties getDiscoveryProperties() {
		return this.DiscoveryProperties;
	}

	public void shutdown() {
		try {
			for (WatchManager wm : allWatchManagers.values())
				wm.dipose();

			if (this.localizer != null) {
				this.localizer.close();
			}
			if (this.zooKeeperServer != null) {
				// purge snaps and logs. Keep only last three of each
				PurgeTxnLog.purge(this.zooKeeperServer.getTxnLogFactory()
						.getDataDir(), this.zooKeeperServer.getTxnLogFactory()
						.getSnapDir(), 3);
				this.zooKeeperServer.shutdown();
			}
			if (this.quorumPeer != null) {
				// purge snaps and logs. Keep only last three of each
				PurgeTxnLog.purge(this.quorumPeer.getTxnFactory().getDataDir(),
						this.quorumPeer.getTxnFactory().getSnapDir(), 3);
				// shut down server
				if (this.quorumPeer.isAlive()) {
					this.quorumPeer.shutdown();
				}
				// shutdown sockets
				this.quorumPeer.getCnxnFactory().shutdown();
			}
		} catch (Throwable t) {
			Logger.log(LogService.LOG_ERROR, t.getMessage(), t);
		}
		// prompt we'r gone!
		PrettyPrinter.prompt(PrettyPrinter.DEACTIVATED, null);
		isConnected = false;
		targetId = null;
	}

	public ZooKeeperServer getLocalServer() {
		return this.zooKeeperServer;
	}

	public void connect(ID id, IConnectContext connectContext)
			throws ContainerConnectException {
		if (isDisposed)
			throw new ContainerConnectException("Container already disposed!");
		if (this.isConnected)
			throw new ContainerConnectException("Container already connected!");
		this.targetId = id;
		if (this.targetId == null) {
			this.targetId = this.getConnectNamespace().createInstance(
					new String[] { getDefaultTarget() });
		}
		init(this.targetId);
	}

	public void disconnect() {
		isConnected = false;
	}

	public Namespace getConnectNamespace() {
		return this.namespace;
	}

	public ID getConnectedID() {
		if (!isConnected)
			return null;
		return this.targetId;
	}

	public IServiceInfo getServiceInfo(IServiceID serviceID) {
		Assert.isNotNull(serviceID);
		return WatchManager.getAllKnownServices().get(serviceID.getName());
	}

	public IServiceTypeID[] getServiceTypes() {
		IServiceTypeID ids[] = new IServiceTypeID[getServices().length];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = getServices()[i].getServiceID().getServiceTypeID();
		}
		return ids;
	}

	public IServiceInfo[] getServices() {
		return WatchManager
				.getAllKnownServices()
				.values()
				.toArray(
						new IServiceInfo[WatchManager.getAllKnownServices()
								.size()]);
	}

	public IServiceInfo[] getServices(IServiceTypeID type) {
		Assert.isNotNull(type);
		List<IServiceInfo> services = new ArrayList<IServiceInfo>();
		for (IServiceInfo sinfo : WatchManager.getAllKnownServices().values()) {
			if (sinfo.getServiceID().getServiceTypeID().getInternal() == type
					.getInternal())
				services.add(sinfo);
		}
		return services.toArray(new IServiceInfo[services.size()]);
	}

	public Namespace getServicesNamespace() {
		return this.namespace;
	}

	public void registerService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		if (!isDisposed && !isConnected) {
			if (this.targetId == null) {
				this.targetId = this.getConnectNamespace().createInstance(
						new String[] { getDefaultTarget() });
			}
			init(this.targetId);
		}
		if (serviceInfo instanceof AdvertisedService) {
			for (WatchManager wm : allWatchManagers.values())
				wm.publish((AdvertisedService) serviceInfo);
		} else
			for (WatchManager wm : allWatchManagers.values())
				wm.publish(new AdvertisedService(serviceInfo));

	}

	/**
	 * Get the default target configuration from the system properties.
	 * 
	 * @return
	 */
	private String getDefaultTarget() {
		return System.getProperty("zoodiscovery.flavor");
	}

	public void unregisterAllServices() {
		for (WatchManager wm : allWatchManagers.values())
			wm.unpublishAll();
	}

	public void unregisterService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		for (WatchManager wm : allWatchManagers.values())
			wm.unpublish(serviceInfo.getServiceID().getServiceTypeID()
					.getInternal());

		fireUndiscovered(serviceInfo);
	}

	public Collection<IServiceListener> getAllServiceListeners() {
		return super.allServiceListeners;
	}

	public Collection<IServiceListener> getServiceListenersForType(
			IServiceTypeID type) {
		return super.getListeners(type);
	}

	public Collection<IServiceTypeListener> getServiceTypeListeners() {
		return super.serviceTypeListeners;
	}

	public void dispose() {
		super.dispose();
		isDisposed = true;
	}

	public ID getID() {
		return Configurator.INSTANCE.getID();
	}

	void fireDiscovered(final IServiceInfo serviceInfo) {
		fireServiceDiscovered(new ServiceContainerEvent(serviceInfo, getID()));
	}

	void fireTypeDiscovered(final IServiceTypeID serviceType) {
		fireServiceTypeDiscovered(new ServiceTypeContainerEvent(serviceType,
				getID()));
	}

	void fireUndiscovered(final IServiceInfo serviceInfo) {
		fireServiceUndiscovered(new ServiceContainerEvent(serviceInfo, getID()));
	}
}
