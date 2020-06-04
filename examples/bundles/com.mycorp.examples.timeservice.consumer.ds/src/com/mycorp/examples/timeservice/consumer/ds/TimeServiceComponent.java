/****************************************************************************
 * Copyright (c) 2013 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package com.mycorp.examples.timeservice.consumer.ds;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.mycorp.examples.timeservice.ITimeService;

@Component(immediate=true)
public class TimeServiceComponent {

	// Called by DS upon ITimeService discovery
	@Reference
	void bindTimeService(ITimeService timeService) {
		// Call the service and print out result!
		System.out.println("Current time on remote is: " + timeService.getCurrentTime());
	}
	
	// Called by DS upon ITimeService undiscovery
	void unbindTimeService(ITimeService timeService) {
		System.out.println("Undiscovered ITimeService via DS.  Instance="+timeService);
	}
}
