/*******************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.tests.remoteservice.r_osgi;

/**
 * Constants for setting up an R-OSGi test environment.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public interface R_OSGi {

	/**
	 * Client containers equal servers, they are both peers in R-OSGi. The
	 * container name for R-OSGi peers in ECF is:
	 */
	public static final String CLIENT_CONTAINER_NAME = "ecf.r_osgi.peer";

	/**
	 * The server container is the one bound to the listener address. By
	 * default, this is port 9278 on the local host.
	 */
	public static final String SERVER_IDENTITY = "r-osgi://localhost:9278";

}
