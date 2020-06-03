/****************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence.bot;

import java.util.List;

/**
 * Contents for creating a instant messaging bot.
 * 
 * "This interface is not intended to be implemented by clients.
 */
public interface IIMBotEntry {

	public String getId();

	public String getName();

	public String getContainerFactoryName();

	public String getConnectID();

	public String getPassword();

	public List getCommands();

}
