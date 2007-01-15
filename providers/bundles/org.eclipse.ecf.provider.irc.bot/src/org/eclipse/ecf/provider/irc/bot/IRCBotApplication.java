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
			Bot bot = new Bot(entry);
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
