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

public class SynchConnectionEvent extends ConnectionEvent {
	private static final long serialVersionUID = 3257288019714324532L;

	public SynchConnectionEvent(ISynchConnection conn, Object data) {
        super(conn, data);
    }
}