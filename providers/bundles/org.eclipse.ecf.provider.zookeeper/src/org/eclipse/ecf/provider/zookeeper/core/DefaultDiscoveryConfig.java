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

import java.util.HashMap;
import java.util.Map;

import org.apache.zookeeper.ZooDefs;
import org.eclipse.ecf.provider.zookeeper.util.Geo;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
/**
 * Default implementation of <code>IDiscoveryConfig</code>. Since this is
 * zookeeper related code, check the net for the zookeeper administration guide
 * 
 * @since 1.0.0
 */
public class DefaultDiscoveryConfig implements IDiscoveryConfig {

	private Map<String, Object> defaultConfigProperties = new HashMap<String, Object>();

	/*
	 * ====================================================================
	 * Default values and are pretty enough for most cases.
	 * ====================================================================
	 */

	/**
	 * This is the default flavor when no flavor has been passed
	 * 
	 * @since 1.0.0
	 */
	public static final String DEFAULT_FLAVOR = IDiscoveryConfig.ZOODISCOVERY_FLAVOR_STANDALONE// .ZOODISCOVERY_FLAVOR_STANDALONE
			+ "=" + Geo.getLocation();//$NON-NLS-1$

	/**
	 * The location of the zookeeper work directory. By default it will be the
	 * value of the <code>java.io.tmpdir</code> property. Can be controlled by
	 * either providing <code>-Dzoodiscovery.tempDir=/qualified/path/</code>
	 * where <code>/qualified/path/</code> is the absolute path name you want to
	 * define or by appending the string to the instantiation of the container
	 * id: <a href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery"
	 * >instantiation of the container id</a>.
	 * 
	 * @since 1.0.0
	 **/
	public static final String TEMPDIR_DEFAULT = System.getProperties()
			.getProperty("java.io.tmpdir"); //$NON-NLS-1$

	/**
	 * Directory will be created at runtime. The folder will be named
	 * "zookeeperData". Can be controlled by either providing
	 * <code>-Dzoodiscovery.dataDir=string</code> where <code>string</code> is
	 * the single directory name you want to define or by appending the string
	 * to the instantiation of the container id: <a
	 * href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery"
	 * >instantiation of the container id</a>.
	 * 
	 * @since 1.0.0
	 **/
	public static final String DATADIR_DEFAULT = "zookeeperData";//$NON-NLS-1$

	/**
	 * Will be generated at runtime. The folder will be named "zookeeperData"
	 * 
	 * @since 1.0.0
	 **/
	public static final String DATALOGDIR_DEFAULT = DATADIR_DEFAULT;

	/**
	 * The server port default. Can be controlled by either providing
	 * <code>-Dzoodiscovery.serverPort=nnnn</code> where <code>nnnn</code> is
	 * the port you want to define as the server port or by appending the string
	 * to the instantiation of the container id: <a
	 * href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery">zoodiscovery
	 * documentation</a>.
	 * 
	 * @since 1.0.0
	 */
	public static final int SERVER_PORT_DEFAULT = 2888;

	/**
	 * The election port default. Can be controlled by either providing
	 * <code>-Dzoodiscovery.electionPort=nnnn</code> where <code>nnnn</code> is
	 * the port you want to define as the election port or by appending the
	 * string to the instantiation of the <a
	 * href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery"
	 * >instantiation of the container id</a>.
	 * 
	 * @since 1.0.0
	 */
	public static final int ELECTION_PORT_DEFAULT = 3888;

	/**
	 * The client port default. Can be controlled by either providing
	 * <code>-Dzoodiscovery.clientPort=nnnn</code> where <code>nnnn</code> is
	 * the port you want to define as the client port or by appending the string
	 * to the instantiation of the <a
	 * href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery"
	 * >instantiation of the container id</a>.
	 * 
	 * @since 1.0.0
	 */
	public static final int CLIENT_PORT_DEFAULT = 2181;

	/**
	 * The tick time default. the length of a single tick, which is the basic
	 * time unit used by ZooKeeper, as measured in milliseconds. It is used to
	 * regulate heartbeats, and timeouts. For example, the minimum session
	 * timeout will be two ticks.
	 * <p>
	 * Can be controlled by either providing
	 * <code>-Dzoodiscovery.tickTime=nnnn</code> where <code>nnnn</code> is the
	 * tick time or by appending the string to the <a
	 * href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery"
	 * >instantiation of the container id</a>.
	 * 
	 * @since 1.0.0
	 */
	public static final int TICKTIME_DEFAULT = 2000;

	/**
	 * The server init limit default. Can be controlled by either providing
	 * <code>-Dzoodiscovery.initLimit=nnnn</code> where <code>nnnn</code> is the
	 * init limit or by appending the string to the <a
	 * href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery"
	 * >instantiation of the container id</a>.
	 * 
	 * @since 1.0.0
	 */
	public static final int INITLIMIT_DEFAULT = 50;

	/**
	 * The sync limit default. Can be controlled by either providing
	 * <code>-Dzoodiscovery.syncLimit=nnnn</code> where <code>nnnn</code> is the
	 * sync limit you want to define or by appending the string to the
	 * instantiation of the container id: <a
	 * href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery"
	 * >instantiation of the container id</a>.
	 * 
	 * @since 1.0.0
	 */
	public static final int SYNCLIMIT_DEFAULT = 2;



	public DefaultDiscoveryConfig() {

		String zoodiscoveryClientPort = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_CLIENTPORT);

		String zoodiscoveryServerPort = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_SERVER_PORT);

		String zoodiscoveryElectionPort = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_ELECTION_PORT);

		String zoodiscoveryTempDir = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_TEMPDIR);

		String zoodiscoveryDataDir = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_DATADIR);

		String zoodiscoveryDataLogDir = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_DATALOGDIR);

		String zoodiscoveryTickTime = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_TICKTIME);

		String zoodiscoveryInitLimit = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_INITLIMIT);

		String zoodiscoverySyncLimit = System.getProperty("zoodiscovery."
				+ ZOOKEEPER_SYNCLIMIT);

		// Load from system properties
		this.defaultConfigProperties.put(ZOOKEEPER_TEMPDIR,
				zoodiscoveryTempDir == null ? TEMPDIR_DEFAULT
						: zoodiscoveryTempDir);

		this.defaultConfigProperties.put(ZOOKEEPER_DATADIR,
				zoodiscoveryDataDir == null ? DATADIR_DEFAULT
						: zoodiscoveryDataDir);

		this.defaultConfigProperties.put(
				ZOOKEEPER_DATALOGDIR,
				zoodiscoveryDataLogDir == null ? defaultConfigProperties
						.get(ZOOKEEPER_DATADIR) : zoodiscoveryDataLogDir);

		this.defaultConfigProperties.put(
				ZOOKEEPER_CLIENTPORT,
				zoodiscoveryClientPort == null ? CLIENT_PORT_DEFAULT : Integer
						.parseInt(zoodiscoveryClientPort));

		this.defaultConfigProperties.put(
				ZOOKEEPER_TICKTIME,
				zoodiscoveryTickTime == null ? TICKTIME_DEFAULT : Integer
						.parseInt(zoodiscoveryTickTime));

		this.defaultConfigProperties.put(
				ZOOKEEPER_INITLIMIT,
				zoodiscoveryTickTime == null ? INITLIMIT_DEFAULT : Integer
						.parseInt(zoodiscoveryInitLimit));

		this.defaultConfigProperties.put(
				ZOOKEEPER_SYNCLIMIT,
				zoodiscoverySyncLimit == null ? SYNCLIMIT_DEFAULT : Integer
						.parseInt(zoodiscoverySyncLimit));

		this.defaultConfigProperties.put(
				ZOOKEEPER_SERVER_PORT,
				zoodiscoveryServerPort == null ? SERVER_PORT_DEFAULT : Integer
						.parseInt(zoodiscoveryServerPort));

		this.defaultConfigProperties.put(ZOOKEEPER_ELECTION_PORT,
				zoodiscoveryElectionPort == null ? ELECTION_PORT_DEFAULT
						: Integer.parseInt(zoodiscoveryElectionPort));

		this.defaultConfigProperties.put("preAllocSize", 1); //$NON-NLS-1$

		//
		// Load flavor from system properties
		String zoodiscoveryFlavor = System.getProperty("zoodiscovery.flavor");
		if (zoodiscoveryFlavor != null) {
			if (zoodiscoveryFlavor.startsWith(ZOODISCOVERY_FLAVOR_STANDALONE))
				defaultConfigProperties.put(ZOODISCOVERY_FLAVOR_STANDALONE,
						zoodiscoveryFlavor);
			else if (zoodiscoveryFlavor
					.startsWith(ZOODISCOVERY_FLAVOR_REPLICATED))
				defaultConfigProperties.put(ZOODISCOVERY_FLAVOR_REPLICATED,
						zoodiscoveryFlavor);
			else if (zoodiscoveryFlavor
					.startsWith(ZOODISCOVERY_FLAVOR_CENTRALIZED))
				defaultConfigProperties.put(ZOODISCOVERY_FLAVOR_CENTRALIZED,
						zoodiscoveryFlavor);
		} else
			defaultConfigProperties.put(ZOODISCOVERY_FLAVOR_CENTRALIZED,
					DEFAULT_FLAVOR);
	}

	public Map<String, Object> getConfigProperties() {
		return this.defaultConfigProperties;
	}

}
