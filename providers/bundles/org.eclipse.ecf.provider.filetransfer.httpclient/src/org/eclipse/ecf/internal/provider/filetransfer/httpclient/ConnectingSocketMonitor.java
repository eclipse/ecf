/*******************************************************************************
* Copyright (c) 2009 IBM, and others. 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   IBM Corporation - initial API and implementation
******************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.httpclient;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ConnectingSocketMonitor implements ISocketConnectionCallback {

	private Set connectingSockets;

	public ConnectingSocketMonitor(int initialCapacity) {
		connectingSockets = Collections.synchronizedSet(new HashSet(initialCapacity));
	}

	public ConnectingSocketMonitor() {
		connectingSockets = Collections.synchronizedSet(new HashSet());
	}

	public Collection getConnectingSockets() {
		return Collections.unmodifiableCollection(connectingSockets);
	}

	public void clear() {
		connectingSockets.clear();
	}

	public void onSocketConnected(Socket socket) {
		connectingSockets.remove(socket);
	}

	public void onSocketConnectionFailed(Socket socket, IOException e) {
		connectingSockets.remove(socket);
	}

	public void onSocketCreated(Socket socket) {
		connectingSockets.add(socket);
	}
}
