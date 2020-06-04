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
package org.eclipse.ecf.provider.irc.internal.bot;

import java.util.List;

public interface IBotEntry {
	
	public String getId();
	
	public String getName();
	
	public String getServer();
	
	public String getChannel();
	
	public List getCommands();

}
