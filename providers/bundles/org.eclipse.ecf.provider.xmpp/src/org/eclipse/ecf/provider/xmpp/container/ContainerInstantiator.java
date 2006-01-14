/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.xmpp.container;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.ContainerInstantiationException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;


public class ContainerInstantiator implements IContainerInstantiator {
    public ContainerInstantiator() {
        
    }
    protected ID getIDFromArg(Class type, Object arg)
            throws IDInstantiationException {
        if (arg instanceof ID)
            return (ID) arg;
        if (arg instanceof String) {
            String val = (String) arg;
            if (val == null || val.equals("")) {
                return IDFactory.getDefault().createGUID();
            } else
                return IDFactory.getDefault().createStringID((String) arg);
        } else if (arg instanceof Integer) {
            return IDFactory.getDefault().createGUID(((Integer) arg).intValue());
        } else
            return IDFactory.getDefault().createGUID();
    }

    protected Integer getIntegerFromArg(Class type, Object arg)
            throws NumberFormatException {
        if (arg instanceof Integer)
            return (Integer) arg;
        else if (arg != null) {
            return new Integer((String) arg);
        } else
            return new Integer(-1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(.ContainerDescription,
     *      java.lang.Class[], java.lang.Object[])
     */
    public IContainer createInstance(
            ContainerTypeDescription description, Class[] argTypes,
            Object[] args)
            throws ContainerInstantiationException {
        try {
            Integer ka = new Integer(XMPPClientSOContainer.DEFAULT_KEEPALIVE);
            String name = null;
            if (args != null) {
                if (args.length > 0) {
                    name = (String) args[0];
                    if (args.length > 1) {
                        ka = getIntegerFromArg(argTypes[1], args[1]);
                    }
                }
            }
            if (name == null) {
                if (ka == null) {
                    return new XMPPClientSOContainer();
                } else {
                    return new XMPPClientSOContainer(ka.intValue());
                }
            } else {
                if (ka == null) {
                    ka = new Integer(XMPPClientSOContainer.DEFAULT_KEEPALIVE);
                }
                return new XMPPClientSOContainer(name,ka.intValue());                
            }
        } catch (Exception e) {
            throw new ContainerInstantiationException(
                    "Exception creating generic container", e);
        }
    }
}