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

import org.eclipse.ecf.discovery.IServiceInfo;

/**
 * @author Ahmed Aadel
 * @since 0.1
 */
public interface INode {

	String ROOT = "/industrial_root";//$NON-NLS-1$
	String SLASH = "/";//$NON-NLS-1$
	String ROOT_SLASH = ROOT + SLASH;
	String _URI_ = "_uri_";//$NON-NLS-1$
	int ID_POSITION = 0;
	int URI_POSITION = 1;
	String NODE_PROPERTY_NAME_PROTOCOLS = "node.property.name.protocols";//$NON-NLS-1$
	String NODE_PROPERTY_NAME_SCOPE = "node.property.name.scope";//$NON-NLS-1$
	String NODE_PROPERTY_NAME_NA = "node.property.name.na";//$NON-NLS-1$
	String NODE_PROPERTY_SERVICES = "node.property.name.services";//$NON-NLS-1$
	String STRING_DELIM = " ";//$NON-NLS-1$

	String getPath();

	String getAbsolutePath();

	boolean isLocalNode();

	IServiceInfo getWrappedService();

	void regenerateNodeId();

}
