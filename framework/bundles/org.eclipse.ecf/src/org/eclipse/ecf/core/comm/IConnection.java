/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.core.comm;

import java.io.IOException;
import java.util.Map;
import org.eclipse.ecf.core.identity.ID;

public interface IConnection {
	public Object connect(ID remote, Object data, int timeout)
			throws IOException;

	public void disconnect() throws IOException;

	public boolean isConnected();

	public ID getLocalID();

	public void start();

	public void stop();

	public boolean isStarted();

	public Map getProperties();

	public void addCommEventListener(IConnectionEventHandler listener);

	public void removeCommEventListener(IConnectionEventHandler listener);

	public Object getAdapter(Class clazz);
}