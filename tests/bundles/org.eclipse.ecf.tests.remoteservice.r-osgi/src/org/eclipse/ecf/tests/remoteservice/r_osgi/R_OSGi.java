/****************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.tests.remoteservice.r_osgi;

/**
 * Constants for setting up an R-OSGi test environment.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public interface R_OSGi {

	public static final String CONSUMER_CONTAINER_TYPE = "ecf.r_osgi.peer";
	public static final String HOST_CONTAINER_TYPE = "ecf.r_osgi.peer";

	public static final String HOST_CONTAINER_ENDPOINT_ID = "r-osgi://localhost:9278";

}
