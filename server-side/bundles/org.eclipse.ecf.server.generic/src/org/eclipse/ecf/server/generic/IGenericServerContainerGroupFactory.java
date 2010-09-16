/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.server.generic;

import java.util.Map;

/**
 * @since 3.1
 */
public interface IGenericServerContainerGroupFactory {

	public int DEFAULT_PORT = 3282;

	public IGenericServerContainerGroup createContainerGroup(String hostname, int port, Map defaultContainerProperties) throws GenericServerContainerGroupCreateException;

	public IGenericServerContainerGroup createContainerGroup(String hostname, int port) throws GenericServerContainerGroupCreateException;

	public IGenericServerContainerGroup createContainerGroup(String hostname) throws GenericServerContainerGroupCreateException;

	public IGenericServerContainerGroup getContainerGroup(String hostname, int port);

	public IGenericServerContainerGroup[] getContainerGroups();

	public IGenericServerContainerGroup removeContainerGroup(String hostname, int port);

}
