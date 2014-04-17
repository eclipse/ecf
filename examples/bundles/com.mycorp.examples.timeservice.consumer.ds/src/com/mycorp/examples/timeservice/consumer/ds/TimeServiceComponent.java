/*******************************************************************************
 * Copyright (c) 2013 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package com.mycorp.examples.timeservice.consumer.ds;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mycorp.examples.timeservice.ITimeService;
import com.mycorp.examples.timeservice.ITimeServiceAsync;

public class TimeServiceComponent {

	void bindTimeService(ITimeService timeService) {
		// Invoke synchronously
		System.out.println("Discovered ITimeService via DS");
		// Call the service and print out result!
		System.out.println("Current time is: " + timeService.getCurrentTime());
		
		// Then invoke asynchronously
	    if (timeService instanceof ITimeServiceAsync) {
	        ITimeServiceAsync asyncTimeService = (ITimeServiceAsync) timeService;
	        System.out.println("Discovered ITimeServiceAsync via DS");
	        // Call the asynchronous remote service.  Unlike the synchronous getTimeService(),
	        // this method will not block
	        Future<Long> currentTimeFuture = asyncTimeService.getCurrentTimeAsync();
	        // potentially do other operations here...
	        try {
				System.out.println("Current time via future.get is: " + currentTimeFuture.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}  
	    }
	}
}
