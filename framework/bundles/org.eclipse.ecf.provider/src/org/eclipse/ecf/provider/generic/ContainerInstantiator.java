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

package org.eclipse.ecf.provider.generic;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerInstantiationException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;

public class ContainerInstantiator implements
        ISharedObjectContainerInstantiator {
    public ContainerInstantiator() {
        super();
    }

    public ISharedObjectContainer makeInstance(Class[] argTypes, Object[] args)
            throws SharedObjectContainerInstantiationException {
        try {
            Boolean isClient = new Boolean(true);
            ID id = null;
            Integer keepAlive = new Integer(TCPServerSOContainer.DEFAULT_KEEPALIVE);
            if (args != null) {
                if (args.length == 3) {
                    isClient = (Boolean) args[0];
                    id = (ID) args[1];
                    keepAlive = (Integer) args[2];
                } else if (args.length == 2) {
                    id = (ID) args[0];
                    keepAlive = (Integer) args[1];
                } else if (args.length == 1) {
                    id = (ID) args[0];
                }
            } else {
                id = IDFactory.makeGUID();
            }
            ISharedObjectContainer result = null;
            if (isClient.booleanValue()) {
                return new TCPClientSOContainer(new SOContainerConfig(id),keepAlive.intValue());
            } else {
                return new TCPServerSOContainer(new SOContainerConfig(id),keepAlive.intValue());
            }
        } catch (Exception e) {
            throw new SharedObjectContainerInstantiationException(
                    "Exception creating generic container", e);
        }
    }
}