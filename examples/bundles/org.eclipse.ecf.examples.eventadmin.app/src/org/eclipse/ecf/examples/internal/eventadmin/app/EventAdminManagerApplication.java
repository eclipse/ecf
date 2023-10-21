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

import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.equinox.app.IApplication;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class EventAdminManagerApplication extends AbstractEventAdminApplication
		implements IApplication {

	private static final String DEFAULT_CONTAINER_TYPE = "ecf.jms.activemq.tcp.manager";
	public static final String DEFAULT_CONTAINER_ID = "tcp://localhost:61616/exampleTopic";

	private TestSender testSender;
	private ServiceRegistration testEventHandlerRegistration;

	protected Object run() {
		Properties props = new Properties();
		props.put(EventConstants.EVENT_TOPIC, "*");
		testEventHandlerRegistration = bundleContext.registerService(
				EventHandler.class.getName(), new TestEventHandler("Server"), (Dictionary) props);

		// XXX for testing, setup a test sender
		testSender = new TestSender(eventAdminImpl, topics, container.getID()
				.getName());
		new Thread(testSender).start();

		waitForDone();

		return IApplication.EXIT_OK;
	}

	protected void shutdown() {
		if (testSender != null) {
			testSender.stop();
			testSender = null;
		}
		if (testEventHandlerRegistration != null) {
			testEventHandlerRegistration.unregister();
			testEventHandlerRegistration = null;
		}
		super.shutdown();
	}

	protected String usageApplicationId() {
		return "org.eclipse.ecf.examples.eventadmin.app.EventAdminManager";
	}

	protected String usageParameters() {
		StringBuffer buf = new StringBuffer("\n\t-containerType <default:"
				+ DEFAULT_CONTAINER_TYPE + ">");
		buf.append("\n\t-containerId <default:" + DEFAULT_CONTAINER_ID + ">");
		buf.append("\n\t-topic <default:" + DEFAULT_TOPIC + ">");
		return buf.toString();
	}

	protected void processArgs(String[] args) {
		containerType = DEFAULT_CONTAINER_TYPE;
		containerId = DEFAULT_CONTAINER_ID;
		targetId = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-containerType")) {
				containerType = args[i + 1];
				i++;
			} else if (args[i].equals("-containerId")) {
				containerId = args[i + 1];
				i++;
			} else if (args[i].equals("-topic")) {
				topics = new String[] {args[i + 1]};
				i++;
			}
		}
	}

}
