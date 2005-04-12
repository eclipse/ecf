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
package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerInstantiationException;

public interface IPresenceContainer {
    
	public void addSubscribeListener(ISubscribeListener listener);
	
    public void addPresenceListener(IPresenceListener listener);
    public void addMessageListener(IMessageListener listener);
    public void addSharedObjectMessageListener(ISharedObjectMessageListener listener);

    public IMessageSender getMessageSender();
    public IPresenceSender getPresenceSender();
	public IAccountManager getAccountManager();
	
    public ISharedObjectContainer makeSharedObjectContainer(Class [] types, Object [] args) throws SharedObjectContainerInstantiationException;
    
}
