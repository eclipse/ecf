/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
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

package org.eclipse.ecf.datashare;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A container adapter that allows access to InputStream and OutputStream for 
 * the container.
 * 
 * @since 2.0.0
 */
public interface IStreamContainerAdapter {

	/**
	 * Get InputStream for communicating via container adapter.
	 * @return InputStream <code>null</code> if no InputStream available.
	 */
	public InputStream getInputStream();

	/**
	 * Get OutputStream for communicating via container adapter.
	 * @return OutputStream <code>null</code> if no OutputStream available.
	 */
	public OutputStream getOutputStream();

}
