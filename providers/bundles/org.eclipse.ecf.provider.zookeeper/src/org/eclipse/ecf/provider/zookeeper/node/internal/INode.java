/*******************************************************************************
 *  Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    Wim Jongman - initial API and implementation 
 *    Ahmed Aadel - initial API and implementation     
 *******************************************************************************/
package org.eclipse.ecf.provider.zookeeper.node.internal;

import org.eclipse.ecf.discovery.IServiceInfo;

public interface INode {

	String ROOT = "/zoodiscovery_root";//$NON-NLS-1$
	String SLASH = "/";//$NON-NLS-1$
	String ROOT_SLASH = ROOT + SLASH;
	String _URI_ = "_uri_";//$NON-NLS-1$
	int ID_POSITION = 0;
	int URI_POSITION = 1;
	int ZOODISCOVERYID_POSITION = 2;
	String NODE_PROPERTY_NAME_PROTOCOLS = "node.property.name.protocols";//$NON-NLS-1$
	String NODE_PROPERTY_NAME_SCOPE = "node.property.name.scope";//$NON-NLS-1$
	String NODE_PROPERTY_NAME_NA = "node.property.name.na";//$NON-NLS-1$
	String NODE_PROPERTY_SERVICES = "node.property.name.services";//$NON-NLS-1$
	String NODE_SERVICE_PROPERTIES = "node.property.service.properties";//$NON-NLS-1$;
	String NODE_PROPERTY_SERVICE_NAME = "node.property.service.name";//$NON-NLS-1$;
	String STRING_DELIM = " ";//$NON-NLS-1$
	// the id of this running ZooDiscovery
	String _ZOODISCOVERYID_ = "_zdid_";//$NON-NLS-1$
	// prefix of of a property key having byte[] as value
	String _BYTES_ = "_bytes_";

	String getPath();

	String getAbsolutePath();

	boolean isLocalNode();

	IServiceInfo getWrappedService();

	void regenerateNodeId();
}
