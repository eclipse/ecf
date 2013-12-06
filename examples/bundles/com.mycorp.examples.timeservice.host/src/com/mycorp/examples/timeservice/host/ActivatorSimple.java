/*******************************************************************************
 * Copyright (c) 2013 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package com.mycorp.examples.timeservice.host;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.mycorp.examples.timeservice.ITimeService;

public class ActivatorSimple implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("service.exported.interfaces", "*");
		props.put("service.exported.configs","ecf.generic.server");
		props.put("ecf.generic.server.id", "ecftcp://localhost:3288/server");
		ServiceRegistration<ITimeService> timeServiceRegistration = context
				.registerService(ITimeService.class, new TimeServiceImpl(),
						props);
		System.out.println("MyTimeService host registered with registration="
				+ timeServiceRegistration);
	}

	public void stop(BundleContext context) throws Exception {
		// do nothing
	}

}
