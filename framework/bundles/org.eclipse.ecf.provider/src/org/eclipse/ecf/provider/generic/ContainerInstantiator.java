/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.generic;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerDescription;
import org.eclipse.ecf.core.SharedObjectContainerInstantiationException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;
import org.eclipse.ecf.provider.Trace;

public class ContainerInstantiator implements
        ISharedObjectContainerInstantiator {
    public static final String TCPCLIENT_NAME = "org.eclipse.ecf.provider.generic.Client";
    public static final String TCPSERVER_NAME = "org.eclipse.ecf.provider.generic.Server";

    public static final Trace debug = Trace.create("containerfactory");
    public ContainerInstantiator() {
        super();
    }
    protected void debug(String msg) {
        if (Trace.ON && debug != null) {
            debug.msg(msg);
        }
    }
    protected void dumpStack(String msg, Throwable t) {
        if (Trace.ON && debug != null) {
            debug.dumpStack(t,msg);
        }
    }
    protected ID getIDFromArg(Class type, Object arg)
            throws IDInstantiationException {
        if (arg instanceof ID) return (ID) arg;
        if (arg instanceof String) {
            String val = (String) arg;
            if (val == null || val.equals("")) {
                return IDFactory.getDefault().makeGUID();
            } else return IDFactory.getDefault().makeStringID((String) arg);
        } else if (arg instanceof Integer) {
            return IDFactory.getDefault().makeGUID(((Integer) arg).intValue());
        } else
            return IDFactory.getDefault().makeGUID();
    }

    protected Integer getIntegerFromArg(Class type, Object arg)
            throws NumberFormatException {
        if (arg instanceof Integer)
            return (Integer) arg;
        else if (arg != null) {
            return new Integer((String) arg);
        } else return new Integer(-1);
    }

    public ISharedObjectContainer makeInstance(
            SharedObjectContainerDescription description, Class[] argTypes,
            Object[] args) throws SharedObjectContainerInstantiationException {
        boolean isClient = true;
        if (description.getName().equals(TCPSERVER_NAME)) {
            debug("creating server");
            isClient = false;
        } else {
            debug("creating client");
        }
        try {
            String [] argDefaults = description.getArgDefaults();
            ID newID = (argDefaults==null||argDefaults.length==0)?null:getIDFromArg(String.class,
                    description.getArgDefaults()[0]);
            Integer ka = (argDefaults==null||argDefaults.length < 2)?null:getIntegerFromArg(String.class, description
                    .getArgDefaults()[1]);
            if (args != null) {
                if (args.length > 0) {
                    newID = getIDFromArg(argTypes[0], args[0]);
                    if (args.length > 1) {
                        ka = getIntegerFromArg(argTypes[1],args[1]);
                    }
                }
            }
            debug("id="+newID+";keepAlive="+ka);
            // new ID must not be null
            if (newID == null)
                throw new SharedObjectContainerInstantiationException(
                        "id must be provided");
            if (isClient) {
                return new TCPClientSOContainer(new SOContainerConfig(newID),
                        ka.intValue());
            } else {
                return new TCPServerSOContainer(new SOContainerConfig(newID),
                        ka.intValue());
            }
        } catch (ClassCastException e) {
            dumpStack("ClassCastException",e);
            throw new SharedObjectContainerInstantiationException(
                    "Parameter type problem creating container", e);
        } catch (Exception e) {
            dumpStack("Exception",e);
            throw new SharedObjectContainerInstantiationException(
                    "Exception creating generic container", e);
        }
    }
}