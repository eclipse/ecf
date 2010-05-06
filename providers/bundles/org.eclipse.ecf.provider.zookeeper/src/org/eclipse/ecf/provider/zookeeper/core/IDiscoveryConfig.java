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

import java.util.Map;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */

/**
 * Configuration values of this ZooKeeper-based discovery provider.
 */
public interface IDiscoveryConfig {

	/**
	 * @return Map of properties used for configuration. All properties
	 *         understood by Apache ZooKeeper (v3.1.1) might be included as
	 *         well.
	 * @see <a href=
	 *      "http://hadoop.apache.org/zookeeper/docs/r3.2.1/zookeeperAdmin.html"
	 *      > ZooKeeper Administrator's Guide</a>
	 */
	Map<String, Object> getConfigProperties();

	/*
	 * ====================================================================
	 * Constants defining keys
	 * ====================================================================
	 */

	String ZOODISCOVERY_FLAVOR_STANDALONE = "zoodiscovery.flavor.standalone"; //$NON-NLS-1$
	String ZOODISCOVERY_FLAVOR_CENTRALIZED = "zoodiscovery.flavor.centralized"; //$NON-NLS-1$	
	String ZOODISCOVERY_FLAVOR_REPLICATED = "zoodiscovery.flavor.replicated"; //$NON-NLS-1$


	/** The number of milliseconds of each tick. OPTIONAL **/
	String ZOOKEEPER_TICKTIME = "tickTime"; //$NON-NLS-1$

	/**
	 * The full path to the directory where where the snapshot is stored.
	 * OPIONAL
	 **/
	String ZOOKEEPER_DATADIR = "dataDir"; //$NON-NLS-1$

	/**
	 * The full path to the directory where the log is stored. It may be the
	 * same as but better if separate.
	 * <code>GRAVITY_DISCOVERY_ZOOKEEPER_DATADIR</code>. OPTIONAL
	 **/
	String ZOOKEEPER_DATALOGDIR = "dataLogDir"; //$NON-NLS-1$

	/**
	 * The number of ticks that the initial synchronization phase can take.
	 * OPTIONAL
	 **/
	String ZOOKEEPER_INITLIMIT = "initLimit"; //$NON-NLS-1$

	/**
	 * The number of ticks that can pass between sending a request and getting
	 * an acknowledgment. OPTIONAL
	 **/
	String ZOOKEEPER_SYNCLIMIT = "syncLimit"; //$NON-NLS-1$

	/** The port at which the clients will connect. OPTIONAL **/
	String ZOOKEEPER_CLIENTPORT = "clientPort"; //$NON-NLS-1$

	/** Server to server port. OPTIONAL **/
	String ZOOKEEPER_SERVER_PORT = "serverPort"; //$NON-NLS-1$

	/** Leader election port. OPTIONAL **/
	String ZOOKEEPER_ELECTION_PORT = "electionPort"; //$NON-NLS-1$

}
