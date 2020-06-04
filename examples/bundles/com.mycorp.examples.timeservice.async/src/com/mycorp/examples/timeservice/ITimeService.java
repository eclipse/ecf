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
package com.mycorp.examples.timeservice;

/**
 * Example OSGi service for retrieving current time in milliseconds from January
 * 1, 1970.
 * 
 */
public interface ITimeService {

	/**
	 * Get current time.
	 * 
	 * @return Long current time in milliseconds since Jan 1, 1970. Will not
	 *         return <code>null</code>.
	 */
	public Long getCurrentTime();

}
