/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core;

import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;

public class SharedObjectContainerDescription {

    protected String name;
    protected String instantiatorClass;
    protected ClassLoader classLoader;
    protected ISharedObjectContainerInstantiator instantiator;
    protected String description;
    
    protected int hashCode = 0;

    public SharedObjectContainerDescription(ClassLoader loader, String name,
            String instantiatorClass, String desc) {
        this.classLoader = loader;
        if (name == null)
            throw new RuntimeException(new InstantiationException(
                    "sharedobjectcontainer description name cannot be null"));
        this.name = name;
        this.instantiatorClass = instantiatorClass;
        this.hashCode = name.hashCode();
        this.description = desc;
    }
    public SharedObjectContainerDescription(String name, ISharedObjectContainerInstantiator inst, String desc) {
        this.instantiator = inst;
        this.name = name;
        this.classLoader = this.instantiator.getClass().getClassLoader();
        this.description = desc;
    }
    public String getName() {
        return name;
    }
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    public boolean equals(Object other) {
        if (!(other instanceof SharedObjectContainerDescription))
            return false;
        SharedObjectContainerDescription scd = (SharedObjectContainerDescription) other;
        return scd.name.equals(name);
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        StringBuffer b = new StringBuffer("SharedObjectContainerDescription[");
        b.append(name).append(";");
        b.append(instantiatorClass).append("]");
        return b.toString();
    }

    protected ISharedObjectContainerInstantiator getInstantiator()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        synchronized (this) {
            if (instantiator == null)
                initializeInstantiator(classLoader);
            return instantiator;
        }
    }

    private void initializeInstantiator(ClassLoader cl)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        if (cl == null)
            cl = this.getClass().getClassLoader();
        // Load instantiator class
        Class clazz = Class.forName(instantiatorClass, true, cl);
        // Make new instance
        instantiator = (ISharedObjectContainerInstantiator) clazz.newInstance();
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
}