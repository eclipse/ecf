/****************************************************************************
 * Copyright (c) 2011 Composent, Inc. and others.
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
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.core.ContainerTypeDescription;

/**
 * @since 2.0
 */
public class SelectContainerException extends Exception {

	private static final long serialVersionUID = -5507248105370677422L;

	private ContainerTypeDescription containerTypeDescription;

	public SelectContainerException(String message, Throwable cause,
			ContainerTypeDescription containerTypeDescription) {
		super(message, cause);
		this.containerTypeDescription = containerTypeDescription;
	}

	public ContainerTypeDescription getContainerTypeDescription() {
		return containerTypeDescription;
	}
}
