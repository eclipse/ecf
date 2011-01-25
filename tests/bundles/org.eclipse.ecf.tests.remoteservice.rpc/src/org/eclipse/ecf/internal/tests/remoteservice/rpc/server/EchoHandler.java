/*******************************************************************************
 * Copyright (c) 2009-2010 Naumen. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Pavel Samolisov - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.tests.remoteservice.rpc.server;

public class EchoHandler {
	public String echo(String str) {
		System.out.println(str); // TODO using logger		
		return str;
	}
}
