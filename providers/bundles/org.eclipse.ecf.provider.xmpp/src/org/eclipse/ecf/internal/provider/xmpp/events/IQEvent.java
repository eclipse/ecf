/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.xmpp.events;

import org.eclipse.ecf.core.util.Event;
import org.jivesoftware.smack.packet.IQ;

public class IQEvent implements Event {

	protected IQ iq = null;

	public IQEvent(IQ iq) {
		this.iq = iq;
	}

	public IQ getIQ() {
		return iq;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("IQEvent[]");
		return buf.toString();
	}
}
