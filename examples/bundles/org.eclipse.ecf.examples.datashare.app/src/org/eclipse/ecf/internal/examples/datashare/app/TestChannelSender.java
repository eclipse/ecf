/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.examples.datashare.app;

import org.eclipse.ecf.datashare.IChannel;

public class TestChannelSender implements Runnable {

	private static final long DEFAULT_WAITTIME = 2000;

	private long waittime = DEFAULT_WAITTIME;

	private IChannel channel;
	private boolean done = false;

	public TestChannelSender(IChannel channel) {
		this.channel = channel;
	}

	public void run() {
		synchronized (this) {
			while (!done) {
				try {
					wait(waittime);
					String message = "hello.  Local time is: "
							+ System.currentTimeMillis();
					// Now send this message
					channel.sendMessage(message.getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void stop() {
		synchronized (this) {
			done = true;
			notifyAll();
		}
	}

}
