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
package org.eclipse.ecf.provider.irc.bot;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.ecf.provider.irc.internal.bot.IBotEntry;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class IRCBotApplication implements IApplication {
	
	public Object start(IApplicationContext context) throws Exception {
		
		Map bots = Activator.getDefault().getBots();
		
		for(Iterator it = bots.values().iterator(); it.hasNext();) {
			IBotEntry entry = (IBotEntry) it.next();
			new Bot(entry);
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
