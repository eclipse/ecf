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

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
/**
 * Default implementation of <code>IDiscoveryConfig</code>.
 * 
 */
public class DefaultDiscoveryConfig implements IDiscoveryConfig {

	private Map<String, Object> defaultConfigProperties = new HashMap<String, Object>();

	/*
	 * ====================================================================
	 * Default values and are pretty enough for most cases.
	 * ====================================================================
	 */

	/**
	 * Will be generated at runtime. The folder will be named "zookeeperData"
	 * <code>IDiscoveryConfig</code>
	 **/
	public static final String DATADIR_DEFAULT = "zookeeperData";//$NON-NLS-1$

	/**
	 * Will be generated at runtime. The folder will be named "zookeeperLog"
	 * <code>IDiscoveryConfig</code>
	 **/
	public static final String DATALOGDIR_DEFAULT = DATADIR_DEFAULT;

	public static final int SERVER_PORT_DEFAULT = 2888;
	public static final int ELECTION_PORT_DEFAULT = 3888;
	public static final int CLIENT_PORT_DEFAULT = 2181;
	public static final int TICKTIME_DEFAULT = 2000;
	public static final int INITLIMIT_DEFAULT = 50;
	public static final int SYNCLIMIT_DEFAULT = 2;

	public DefaultDiscoveryConfig() {

		// all the rest are optional for you and are(or will be at runtime)
		// taken care of.
		this.defaultConfigProperties.put(ZOOKEEPER_DATADIR, DATADIR_DEFAULT);
		this.defaultConfigProperties.put(ZOOKEEPER_DATALOGDIR,
				DATALOGDIR_DEFAULT);
		this.defaultConfigProperties.put(ZOOKEEPER_CLIENTPORT,
				CLIENT_PORT_DEFAULT);
		this.defaultConfigProperties.put(ZOOKEEPER_TICKTIME, TICKTIME_DEFAULT);
		this.defaultConfigProperties
				.put(ZOOKEEPER_INITLIMIT, INITLIMIT_DEFAULT);
		this.defaultConfigProperties
				.put(ZOOKEEPER_SYNCLIMIT, SYNCLIMIT_DEFAULT);
		this.defaultConfigProperties.put(ZOOKEEPER_SERVER_PORT,
				SERVER_PORT_DEFAULT);
		this.defaultConfigProperties.put(ZOOKEEPER_ELECTION_PORT,
				ELECTION_PORT_DEFAULT);
		this.defaultConfigProperties.put("preAllocSize", 1); //$NON-NLS-1$

	}

	public Map<String, Object> getConfigProperties() {
		return this.defaultConfigProperties;
	}

}
