/****************************************************************************
 * Copyright (c) 2014 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis (slewis@composent.com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.examples.raspberrypi.management;

import java.util.Map;

public interface IRaspberryPi {

	/**
	 * Get system properties for the Raspberry Pi remote service host.
	 * @return Map<String,String> the system properties for the remote RP
	 */
	public Map<String,String> getSystemProperties();
	
}
