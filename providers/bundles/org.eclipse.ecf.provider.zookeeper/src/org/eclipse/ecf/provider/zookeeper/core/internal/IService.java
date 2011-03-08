/*******************************************************************************
 *  Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Wim Jongman - initial API and implementation 
 *     Ahmed Aadel - initial API and implementation     
 *******************************************************************************/
package org.eclipse.ecf.provider.zookeeper.core.internal;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

public interface IService extends IServiceInfo {

	/**
	 * Holds the service location ( {@link IServiceInfo#getLocation()} ) in the
	 * zooKeeper node
	 **/
	String LOCATION = "discovery.service.location"; //$NON-NLS-1$ 

	/**
	 * Holds the service weight ( {@link IServiceInfo#getWeight()} ) in the
	 * zooKeeper node
	 **/
	String WEIGHT = "discovery.service.weight"; //$NON-NLS-1$

	/**
	 * Holds the service priority ({@link IServiceInfo#getPriority()()} ) in the
	 * zooKeeper node
	 **/
	String PRIORITY = "discovery.service.priority"; //$NON-NLS-1$

	/**
	 * Holds the service-type protocols ({@link IServiceTypeID#getProtocols()} )
	 * in the zooKeeper node
	 **/
	String PROTOCOLS = "discovery.service.protocol"; //$NON-NLS-1$

	/**
	 * Holds the service name ({@link IServiceInfo#getServiceName()}) in the
	 * zooKeeper node
	 **/
	String SERVICE_NAME = "discovery.service.name"; //$NON-NLS-1$

	/**
	 * The byte representation of the service properties, appropriate to be
	 * stored in the zooKeeper node
	 * 
	 * @return byte representation of the properties
	 */
	byte[] getPropertiesAsBytes();
}