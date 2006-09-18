/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.provider.datashare;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectTransactionConfig;
import org.eclipse.ecf.core.SharedObjectInstantiationException;
import org.eclipse.ecf.core.SharedObjectTypeDescription;
import org.eclipse.ecf.core.provider.ISharedObjectInstantiator;
import org.eclipse.ecf.datashare.IChannelListener;

public class ChannelFactory implements ISharedObjectInstantiator {
	public static final String CHANNEL_FACTORY_NAME = "ecf.provider.datashare.channelimpl";
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.ISharedObjectInstantiator#createInstance(org.eclipse.ecf.core.SharedObjectTypeDescription,
	 *      java.lang.Class[], java.lang.Object[])
	 */
	public ISharedObject createInstance(
			SharedObjectTypeDescription typeDescription, Class[] argTypes,
			Object[] args) throws SharedObjectInstantiationException {
		if (args != null && args.length == 3) {
			// do stuff
			return new BaseChannel((ISharedObjectTransactionConfig) args[0],
					(IChannelListener) args[2]);
		} else
			throw new SharedObjectInstantiationException(
					"args provided to ChannelFactory are not valid");
	}
}
