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

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class TestEventHandler implements EventHandler {

	private String name;

	public TestEventHandler(String name) {
		this.name = name;
	}
	
	public void handleEvent(Event event) {
		String extra = "";
		if (event.getProperty("nonserializable") != null) {
			extra = "\n\twrapped in non-serializable="
					+ ((NonSerializable) event.getProperty("nonserializable"))
							.getPayload();
		}
		
		System.out.println("handleEvent by: " + name + "\n\ttopic=" + event.getTopic()
				+ "\n\tmessage=" + event.getProperty("message") + "\n\tsender="
				+ event.getProperty("sender")
				+ extra);
	}

}
