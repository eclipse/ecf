/****************************************************************************
 * Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 *  Contributors:
 *     Wim Jongman - initial API and implementation 
 *     Ahmed Aadel - initial API and implementation    
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.zookeeper.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.provider.zookeeper.util.Geo;

/**
 * Default implementation of <code>IDiscoveryConfig</code>. Since this is
 * zookeeper related code, check the net for the zookeeper administration guide
 * */

public class DefaultDiscoveryConfig implements IDiscoveryConfig {

	protected static Map<String, Object> defaultConfigProperties = new HashMap<String, Object>();

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
	public static final String DEFAULT_FLAVOR = IDiscoveryConfig.ZOODISCOVERY_FLAVOR_STANDALONE
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
	public static final String SERVER_PORT_DEFAULT = "2888";//$NON-NLS-1$		

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
	public static final String ELECTION_PORT_DEFAULT = "3888";//$NON-NLS-1$		

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
	public static final String CLIENT_PORT_DEFAULT = "2181";//$NON-NLS-1$		

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
	public static final String TICKTIME_DEFAULT = "2000";//$NON-NLS-1$		

	/**
	 * The server init limit default. Can be controlled by either providing
	 * <code>-Dzoodiscovery.initLimit=nnnn</code> where <code>nnnn</code> is the
	 * init limit or by appending the string to the <a
	 * href="http://wiki.eclipse.org/Zookeeper_Based_ECF_Discovery"
	 * >instantiation of the container id</a>.
	 * 
	 * @since 1.0.0
	 */
	public static final String INITLIMIT_DEFAULT = "50";//$NON-NLS-1$		

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
	public static final String SYNCLIMIT_DEFAULT = "2";//$NON-NLS-1$		

	public static final String ZOODISCOVERY_PREFIX = "zoodiscovery.";//$NON-NLS-1$		
	static {

		// Check for configuration within system properties
		defaultConfigProperties.put(ZOOKEEPER_TEMPDIR, System.getProperty(
				ZOODISCOVERY_PREFIX + ZOOKEEPER_TEMPDIR, TEMPDIR_DEFAULT));

		defaultConfigProperties.put(ZOOKEEPER_DATADIR, System.getProperty(
				ZOODISCOVERY_PREFIX + ZOOKEEPER_DATADIR, DATADIR_DEFAULT));

		defaultConfigProperties.put(ZOOKEEPER_DATALOGDIR, System.getProperty(
				ZOODISCOVERY_PREFIX + ZOOKEEPER_DATALOGDIR, ZOOKEEPER_DATADIR));

		defaultConfigProperties.put(ZOOKEEPER_CLIENTPORT, System
				.getProperty(ZOODISCOVERY_PREFIX + ZOOKEEPER_CLIENTPORT,
						CLIENT_PORT_DEFAULT));

		defaultConfigProperties.put(ZOOKEEPER_TICKTIME, System.getProperty(
				ZOODISCOVERY_PREFIX + ZOOKEEPER_TICKTIME, TICKTIME_DEFAULT));

		defaultConfigProperties.put(ZOOKEEPER_INITLIMIT, System.getProperty(
				ZOODISCOVERY_PREFIX + ZOOKEEPER_INITLIMIT, INITLIMIT_DEFAULT));

		defaultConfigProperties.put(ZOOKEEPER_SYNCLIMIT, System.getProperty(
				ZOODISCOVERY_PREFIX + ZOOKEEPER_SYNCLIMIT, SYNCLIMIT_DEFAULT));

		defaultConfigProperties.put(ZOOKEEPER_SERVER_PORT, System.getProperty(
				ZOODISCOVERY_PREFIX + ZOOKEEPER_SERVER_PORT,
				SERVER_PORT_DEFAULT));

		defaultConfigProperties.put(ZOOKEEPER_ELECTION_PORT, System
				.getProperty(ZOODISCOVERY_PREFIX + ZOOKEEPER_ELECTION_PORT,
						ELECTION_PORT_DEFAULT));

		defaultConfigProperties.put("preAllocSize", 1); //$NON-NLS-1$		

		defaultConfigProperties.put(ZOODISCOVERY_CONSOLELOG, System.getProperty(ZOODISCOVERY_PREFIX + ZOODISCOVERY_CONSOLELOG, null));
	}

	public DefaultDiscoveryConfig() {
	}

	public Map<String, Object> getConfigProperties() {
		return Collections.unmodifiableMap(defaultConfigProperties);
	}

	public static String getDefaultTarget() {
		String f = System.getProperty("zoodiscovery.flavor");
		if (f == null) {
			f = DefaultDiscoveryConfig.DEFAULT_FLAVOR;
		}

		return f;
	}
	
	/**
	 * 
	 * @return true if consoleLogging was specified.
	 */
	public static boolean getConsoleLog() {
		Map<String, Object> props = new DefaultDiscoveryConfig().getConfigProperties();
		Object f = props.get(ZOODISCOVERY_CONSOLELOG);
		if (f == null) {
			return false;
		} else {
			return true;
		}
	}
}
