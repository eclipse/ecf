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
package org.eclipse.ecf.internal.examples.raspberrypi.management.host;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ecf.examples.raspberrypi.management.IRaspberryPi;

/**
 * Implementation of IRaspberryPi service interface.
 */
public class RaspberryPi implements IRaspberryPi {

	@Override
	public Map<String, String> getSystemProperties() {
		Properties props = System.getProperties();
		
		Map<String, String> result = new HashMap<String,String>();
		for (final String name: props.stringPropertyNames())
		    result.put(name, props.getProperty(name));
		
		System.out.println("REMOTE CALL: getSystemProperties()");
		return result;
	}

}
