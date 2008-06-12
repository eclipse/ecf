/****************************************************************************
 * Copyright (c) 2008 Mustafa K. Isik and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mustafa K. Isik - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.docshare;

import org.eclipse.ecf.docshare.messages.UpdateMessage;

public class IdentityMapping implements SynchronizationStrategy {

	private static IdentityMapping instance;

	public static IdentityMapping getInstance() {
		if (instance == null) {
			instance = new IdentityMapping();
		}
		return instance;
	}

	public UpdateMessage registerOutgoingMessage(UpdateMessage localMsg) {
		return localMsg;
	}

	public UpdateMessage transformIncomingMessage(UpdateMessage remoteMsg) {
		return remoteMsg;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("IdentityMapping[]"); //$NON-NLS-1$
		return buf.toString();
	}

}
