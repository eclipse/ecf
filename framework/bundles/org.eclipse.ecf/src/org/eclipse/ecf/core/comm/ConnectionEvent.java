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

import org.eclipse.ecf.core.util.Event;

public class ConnectionEvent implements Event {

    Object data = null;
    IConnection connection = null;

    public ConnectionEvent(IConnection source, Object data) {
        this.connection = source;
        this.data = data;
    }
    public IConnection getConnection() {
        return connection;
    }
    public Object getData() {
        return data;
    }

}