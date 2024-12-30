/****************************************************************************
 * Copyright (c) 2014 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package com.mycorp.examples.timeservice.host.generic.auth;

import com.mycorp.examples.timeservice.ITimeService;

public class TimeServiceImpl implements ITimeService {

	/**
	 * Implementation of my time service. 
	 */
	public Long getCurrentTime() {
		// Print out to host std out that a call to this service was received.
		System.out.println("TimeServiceImpl.  Received call to getCurrentTime()");
		// Eventually, this should (e.g.) contact NIST time server and return more
		// accurate time.  For the time being, we will return the System time for
		// this host.
		return Long.valueOf(System.currentTimeMillis());
	}

}
