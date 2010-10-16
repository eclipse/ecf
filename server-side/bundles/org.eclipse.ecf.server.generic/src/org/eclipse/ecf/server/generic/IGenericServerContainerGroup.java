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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;

/**
 * @since 4.0
 */
public interface IGenericServerContainerGroup {

	public static final int DEFAULT_KEEPALIVE = 30000;

	public URI getGroupEndpoint();

	public ISharedObjectContainer createContainer(String path, int keepAlive, Map properties) throws ContainerCreateException;

	public ISharedObjectContainer getContainer(String path);

	public Map getContainers();

	public ISharedObjectContainer removeContainer(String path);

	public void startListening() throws IOException;

	public boolean isListening();

	public void stopListening();

	public void close();
}
