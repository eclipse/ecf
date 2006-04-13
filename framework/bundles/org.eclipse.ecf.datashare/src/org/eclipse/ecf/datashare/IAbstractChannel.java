package org.eclipse.ecf.datashare;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.IIdentifiable;

public interface IAbstractChannel extends IAdaptable, IIdentifiable {
	/**
	 * Get IChannelListener instance for this IAbstractChannel
	 * 
	 * @return IChannelListener for this IAbstractChannel instance.  If null, the channel has no listener
	 */
	public IChannelListener getListener();
}
