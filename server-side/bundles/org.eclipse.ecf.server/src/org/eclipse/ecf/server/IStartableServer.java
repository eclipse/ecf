/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
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
package org.eclipse.ecf.server;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.util.ECFException;

/**
 * @since 2.1
 */
public interface IStartableServer {

	public void start() throws ECFException;

	public void stop();

	public IContainer getContainer();
}
