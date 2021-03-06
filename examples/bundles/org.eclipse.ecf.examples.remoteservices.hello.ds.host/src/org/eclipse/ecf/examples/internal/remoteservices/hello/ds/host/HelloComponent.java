/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.examples.internal.remoteservices.hello.ds.host;

import org.eclipse.ecf.examples.remoteservices.hello.HelloMessage;
import org.eclipse.ecf.examples.remoteservices.hello.IHello;

public class HelloComponent implements IHello {

	public String hello(String from) {
		// This is the implementation of the IHello service
		// This method can be executed via remote proxies
		System.out.println("received hello from="+from);
		return "Hello service host says 'Hi' back to "+from;
	}

	public String helloMessage(HelloMessage message) {
		System.out.println("received HelloMessage="+message);
		return "Server says 'Hi' back to "+message.getFrom();
	}
	
}
