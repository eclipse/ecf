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
package org.eclipse.ecf.examples.internal.eventadmin.app;

import java.util.Map;
import java.util.Properties;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class TestSender implements Runnable {

	private static final long DEFAULT_WAITTIME = 2000;

	private long waittime = DEFAULT_WAITTIME;

	private EventAdmin eventAdmin;
	private String[] topics;
	private String sender;
	private boolean done = false;
	private long messageCounter = 0L;

	public TestSender(EventAdmin eventAdmin, String[] topics, String sender) {
		this.eventAdmin = eventAdmin;
		this.topics = topics;
		this.sender = sender;
	}

	public void run() {
		synchronized (this) {
			int i = 0;
			while (!done) {
				try {
					wait(waittime);
					Map msgProps = new Properties();
					msgProps.put("message", "message #" + messageCounter++);
					msgProps.put("sender", sender);
					String topic = topics[i++ % topics.length];
					// Add a non-serializable object.
					// See that we have registered an SerializationHandler
					// for this topic (on both ends local & remote)
					// org.eclipse.ecf.examples.internal.eventadmin.app.AbstractEventAdminApplication.startup(IApplicationContext)
					if (topic.equals(AbstractEventAdminApplication.DEFAULT_TOPIC)) {
						msgProps.put("nonserializable", new NonSerializable("MessageCnt: " + messageCounter));
					}
					eventAdmin.postEvent(new Event(topic, msgProps));
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
