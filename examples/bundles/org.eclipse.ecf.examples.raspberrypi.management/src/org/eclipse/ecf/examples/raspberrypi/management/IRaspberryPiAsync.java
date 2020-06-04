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
import java.util.concurrent.CompletableFuture;

public interface IRaspberryPiAsync {

	/**
	 * Get remote system properties via CompletableFuture for non-blocking.
	 * Note:  signature of this method is connected to {@link IRaspberryPi#getSystemProperties()}.
	 * 
	 * @return CompletableFuture
	 */
	public CompletableFuture<Map<String,String>> getSystemPropertiesAsync();
	
}
