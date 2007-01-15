package org.eclipse.ecf.provider.irc.internal.bot;

import java.util.List;

public interface IBotEntry {
	
	public String getId();
	
	public String getName();
	
	public String getServer();
	
	public String getChannel();
	
	public List getCommands();

}
