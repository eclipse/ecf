package org.eclipse.ecf.ds;

import java.util.Map;

import org.eclipse.ecf.core.ISharedObjectTransactionConfig;
import org.eclipse.ecf.core.identity.ID;

public interface IChannelConfig {
	public ID getID();
	public IChannelListener getListener();
	public ISharedObjectTransactionConfig getTransactionConfig();
	public Map getProperties();
}
