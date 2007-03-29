/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.presence.bot;

import java.util.List;

public interface IChatRoomBotEntry {
	
	public String getId();
	
	public String getName();
	
	public String getContainerFactoryName();
	
	public String getConnectID();
	
	public String getPassword();
	
	public String getChatRoom();
	
	public String getChatRoomPassword();
	
	public List getCommands();

}
