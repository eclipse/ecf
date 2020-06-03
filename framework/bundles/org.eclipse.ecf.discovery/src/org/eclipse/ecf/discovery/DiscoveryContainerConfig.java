/****************************************************************************
 * Copyright (c) 2007 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.discovery;

import org.eclipse.ecf.core.identity.ID;

public class DiscoveryContainerConfig {

	private ID id;

	/**
	 * @param anID
	 *            an ID
	 */
	public DiscoveryContainerConfig(ID anID) {
		id = anID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IContainerConfig#getID()
	 */
	public ID getID() {
		return id;
	}

}
