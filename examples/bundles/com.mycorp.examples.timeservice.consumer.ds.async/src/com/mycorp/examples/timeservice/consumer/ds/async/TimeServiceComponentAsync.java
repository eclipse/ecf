/*******************************************************************************
 * Copyright (c) 2014 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package com.mycorp.examples.timeservice.consumer.ds.async;

import java.util.concurrent.CompletableFuture;

import com.mycorp.examples.timeservice.ITimeServiceAsync;

public class TimeServiceComponentAsync {

	void bindTimeService(ITimeServiceAsync timeService) {
		System.out.println("Discovered ITimeServiceAsync via DS");
		// Get the CompletableFuture...no blocking here
		CompletableFuture<Long> cf = timeService.getCurrentTimeAsync();
		// print out time when done...no blocking anywhere!
		cf.thenAccept((time) -> System.out.println("Remote time is: " + time));
	}
}
