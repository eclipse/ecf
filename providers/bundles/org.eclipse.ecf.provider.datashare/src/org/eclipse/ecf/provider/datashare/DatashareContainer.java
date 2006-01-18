/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.datashare;

import java.util.Map;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectContainerConfig;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.ds.IChannel;
import org.eclipse.ecf.ds.IChannelContainer;
import org.eclipse.ecf.ds.IChannelListener;
import org.eclipse.ecf.provider.generic.TCPClientSOContainer;

public class DatashareContainer extends TCPClientSOContainer implements
		IChannelContainer {
	
	protected static final int KEEP_ALIVE = 30000;
	
	public DatashareContainer(ISharedObjectContainerConfig config) {
		super(config, KEEP_ALIVE);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ds.IChannelContainer#createChannel(org.eclipse.ecf.ds.IChannelConfig)
	 */
	public IChannel createChannel(ID newID, IChannelListener listener, Map properties) throws ECFException {
		ChannelImpl impl = new ChannelImpl(null,listener);
		getSharedObjectManager().addSharedObject(config.getID(), impl, properties);
		return impl;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ds.IChannelContainer#getChannel(org.eclipse.ecf.core.identity.ID)
	 */
	public IChannel getChannel(ID channelID) {
		return (IChannel) getSharedObjectManager().getSharedObject(channelID);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ds.IChannelContainer#disposeChannel(org.eclipse.ecf.core.identity.ID)
	 */
	public boolean disposeChannel(ID channelID) {
		ISharedObject o = getSharedObjectManager().removeSharedObject(channelID);
		return (o != null);
	}
}
