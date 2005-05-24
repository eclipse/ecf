/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare.multicast;

import java.io.IOException;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.datashare.DataSharePlugin;

/**
 * @author pnehrer
 */
public class SimpleMulticaster extends AbstractMulticaster {

	public static final String TRACE_TAG = "SimpleMulticaster";

	public synchronized boolean sendMessage(Object message) throws ECFException {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "sendMessage[message=" + message + "]");

		try {
			if (!waitToSend())
				return false;

			version = new Version(localContainerID, version.getSequence() + 1);
			context.sendMessage(null, new Message(version, message));
			return true;
		} catch (IOException e) {
			throw new ECFException(e);
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
	}

	protected void receiveMessage(Object message) {
	}
}