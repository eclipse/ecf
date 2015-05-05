/*******************************************************************************
 * Copyright (c) 2013 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package com.mycorp.examples.timeservice.consumer.ds;

import com.mycorp.examples.timeservice.ITimeService;

public class TimeServiceComponent {

	// Called by DS upon ITimeService discovery
	void bindTimeService(ITimeService timeService) {
		System.out.println("Discovered ITimeService via DS.  Instance="+timeService);
		// Call the service and print out result!
		System.out.println("Current time is: " + timeService.getCurrentTime());
	}
	
	// Called by DS upon ITimeService undiscovery
	void unbindTimeService(ITimeService timeService) {
		System.out.println("Undiscovered ITimeService via DS.  Instance="+timeService);
	}
}
