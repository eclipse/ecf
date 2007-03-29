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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.ecf.internal.presence.bot.Activator;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class DefaultChatRoomBotApplication implements IApplication {
	
	protected Map getBotsFromExtensionRegistry() {
		return Activator.getDefault().getBots();
	}
	
	public Object start(IApplicationContext context) throws Exception {
		
		Map bots = getBotsFromExtensionRegistry();
		
		for(Iterator it = bots.values().iterator(); it.hasNext();) {
			IChatRoomBotEntry entry = (IChatRoomBotEntry) it.next();
			// Create default chat room bot
			DefaultChatRoomBot bot = new DefaultChatRoomBot(entry);
			// connect
			bot.connect();
		}
		
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
				break;
			}
		}
		
		return IApplication.EXIT_OK;
	}

	public void stop() {}

}
