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

import java.util.*;
import org.eclipse.ecf.filetransfer.events.socket.*;

public class ConnectingSocketMonitor implements ISocketListener {

	private Map connectingSockets;

	public ConnectingSocketMonitor(int initialCapacity) {
		connectingSockets = Collections.synchronizedMap(new HashMap(initialCapacity));
	}

	public ConnectingSocketMonitor() {
		connectingSockets = Collections.synchronizedMap(new HashMap());
	}

	public Collection getConnectingSockets() {
		return Collections.unmodifiableCollection(connectingSockets.keySet());
	}

	public void clear() {
		connectingSockets.clear();
	}

	public void handleSocketEvent(ISocketEvent event) {
		if (event instanceof ISocketCreatedEvent) {
			connectingSockets.put(event.getFactorySocket(), event);
		} else if (event instanceof ISocketConnectedEvent) {
			connectingSockets.remove(event.getFactorySocket());
		} else if (event instanceof ISocketClosedEvent) {
			connectingSockets.remove(event.getFactorySocket());
		}
	}
}
