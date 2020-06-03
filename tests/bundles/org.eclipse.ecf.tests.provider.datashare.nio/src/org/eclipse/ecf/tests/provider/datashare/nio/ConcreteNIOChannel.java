/****************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.datashare.nio;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.provider.datashare.nio.NIOChannel;
import org.eclipse.ecf.provider.datashare.nio.NIODatashareContainer;

public class ConcreteNIOChannel extends NIOChannel {

	public ConcreteNIOChannel(NIODatashareContainer datashareContainer,
			ID userId, ID id, IChannelListener listener) throws ECFException {
		super(datashareContainer, userId, id, listener);
	}

	protected void log(IStatus status) {
		System.err.println(status.getMessage());
		Throwable t = status.getException();
		if (t != null) {
			t.printStackTrace(System.err);
		}
	}

	public int getPort() {
		return getLocalPort();
	}

	protected void sendRequest(ID receiver) throws ECFException {
		// nothing to do
	}

}
