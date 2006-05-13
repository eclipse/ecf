/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.datashare;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.ISharedObjectTransactionConfig;
import org.eclipse.ecf.core.SharedObjectDescription;

/**
 * Channel configuration to be used during createChannel to 
 * configure the newly created IChannel implementation
 *
 */
public interface IChannelConfig extends IAdaptable {
	/**
	 * Get SharedObjectDescription to be used to create primary (local) IChannel implementation.
	 * @return SharedObjectDescription to be used to create primary IChannel implementation
	 */
	public SharedObjectDescription getPrimaryDescription();
	/**
	 * Get listener for channel being created.  Typically, provider will call this
	 * method during the implementation of createChannel.  If this method returns
	 * a non-null IChannelListener instance, the newly created channel must notify the
	 * given listener when channel events occur.  If this method returns null, then no 
	 * listener will be notified of channel events
	 * @return IChannelListener to use for notification of received channel events
	 */
	public IChannelListener getListener();
	/**
	 * Get the ISharedObjectTransactionConfig to use for replicating newly created IChannel
	 * @return ISharedObjectTransactionConfig.  Null if no transaction config specified.  If null,
	 * then provider implementers of createChannel are free to specify whatever transaction 
	 * config parameters appropriate
	 */
	public ISharedObjectTransactionConfig getTransactionConfig();
}
