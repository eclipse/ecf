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

package org.eclipse.ecf.core.comm;

import org.eclipse.ecf.core.comm.provider.ISynchAsynchConnectionInstantiator;

public class ConnectionDescription {

    protected String name;
    protected String instantiatorClass;

    protected ISynchAsynchConnectionInstantiator instantiator;
    protected int hashCode = 0;
    protected ClassLoader classLoader = null;
    protected String description;
    
    public ConnectionDescription(ClassLoader loader,

    String name, String instantiatorClass, String desc) {
        if (name == null)
            throw new RuntimeException(new InstantiationException(
                    "ConnectionDescription<init> name cannot be null"));
        if (instantiatorClass == null)
            throw new RuntimeException(new InstantiationException(
                    "ConnectionDescription<init> instantiatorClass cannot be null"));
        this.classLoader = loader;
        this.name = name;
        this.instantiatorClass = instantiatorClass;
        this.hashCode = name.hashCode();
    }
    public ConnectionDescription(String name, ISynchAsynchConnectionInstantiator inst, String desc) {
        if (name == null)
            throw new RuntimeException(new InstantiationException(
                    "ConnectionDescription<init> name cannot be null"));
        if (inst == null)
            throw new RuntimeException(new InstantiationException(
                    "ConnectionDescription<init> instantiator instance cannot be null"));
        this.instantiator = inst;
        this.name = name;
        this.classLoader = this.instantiator.getClass().getClassLoader();
        this.instantiatorClass = this.instantiator.getClass().getName();
        this.hashCode = name.hashCode();
        this.description = desc;
    }
    public String getName() {
        return name;
    }
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    public boolean equals(Object other) {
        if (!(other instanceof ConnectionDescription))
            return false;
        ConnectionDescription scd = (ConnectionDescription) other;
        return scd.name.equals(name);
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        StringBuffer b = new StringBuffer("ConnectionDescription[");
        b.append("name:").append(name).append(";");
        if (instantiator == null)
        	b.append("class:").append(instantiatorClass).append(";");
        else
            b.append("instantiator:").append(instantiator).append(";");
        b.append("desc:").append(description).append("]");
        return b.toString();
    }

    protected ISynchAsynchConnectionInstantiator getInstantiator()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        synchronized (this) {
            if (instantiator == null)
                initializeInstantiator(classLoader);
            return instantiator;
        }
    }

    protected void initializeInstantiator(ClassLoader cl)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        if (cl == null)
            cl = this.getClass().getClassLoader();
        // Load instantiator class
        Class clazz = Class.forName(instantiatorClass, true, cl);
        // Make new instance
        instantiator = (ISynchAsynchConnectionInstantiator) clazz.newInstance();
    }

    public String getDescription() {
        return description;
    }
    
}