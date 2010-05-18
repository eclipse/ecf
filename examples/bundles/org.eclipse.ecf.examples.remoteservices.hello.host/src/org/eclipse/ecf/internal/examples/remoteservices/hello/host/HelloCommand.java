/*******************************************************************************
 *  Copyright (c)2010 REMAIN B.V. (http://www.remainsoftware.com).
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Wim Jongman - initial API and implementation     
 *******************************************************************************/
package org.eclipse.ecf.internal.examples.remoteservices.hello.host;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

// referenced in component.xml
public class HelloCommand implements CommandProvider {

	private ServiceRegistration helloRegistration;
	private String containerType;
	private String containerId;
	private BundleContext context;

	public HelloCommand(BundleContext context,
			ServiceRegistration helloRegistration, String containerType,
			String containerId) {
		this.context = context;
		this.helloRegistration = helloRegistration;
		this.containerType = containerType;
		this.containerId = containerId;

		Dictionary props = new Hashtable();
		props.put(org.osgi.framework.Constants.SERVICE_RANKING, new Integer(
				Integer.MAX_VALUE - 100));

		context.registerService(CommandProvider.class.getName(), this, props);
	}

	public void _hello(CommandInterpreter ci) {
		String arg = ci.nextArgument();

		if (arg == null) {
			return;
		}

		if (arg.equalsIgnoreCase("stop")) {
			stopService();
		}
		if (arg.equalsIgnoreCase("start")) {
			startService();
		}
	}

	private void startService() {
		if (helloRegistration == null) {
			helloRegistration = HelloHostApplication.startService(context,
					containerType, containerId);
			System.out.println("Service started");
		}
	}

	private void stopService() {
		if (helloRegistration != null) {
			helloRegistration = HelloHostApplication
					.stopService(helloRegistration);
			System.out.println("Service stopped");
		}
	}

	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("---ECF Example Command---\n");
		buffer.append("\thello stop - stop the service. It should be undiscovered remote\n");
		buffer.append("\thello start - start the service. It should be discovered remote\n");
		return buffer.toString();
	}
}