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

package org.eclipse.ecf.example.collab.share;

import org.eclipse.ecf.core.identity.ID;

public final class SenderID {

	private ID myID;

	// No instances other than ones created in SharedObjectMsg.invokeFrom/2
	protected SenderID(ID theID) {
		myID = theID;
	}

	public ID getID() {
		return myID;
	}
}