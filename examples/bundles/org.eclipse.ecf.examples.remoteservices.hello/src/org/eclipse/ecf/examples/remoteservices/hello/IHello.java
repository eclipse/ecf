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
package org.eclipse.ecf.examples.remoteservices.hello;

public interface IHello {

	/**
	 * @since 2.0
	 */
	public String hello(String from);
	
	/**
	 * @since 3.0
	 */
	public String helloMessage(HelloMessage message);
}
