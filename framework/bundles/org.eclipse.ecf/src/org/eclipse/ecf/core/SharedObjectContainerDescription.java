/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import java.util.Arrays;
import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;

/**
 * Description of an ISharedObjectContainer factory implementation.
 * 
 * @see SharedObjectContainerFactory#addDescription(SharedObjectContainerDescription)
 *  
 */
public class SharedObjectContainerDescription {
    protected String name;
    protected String instantiatorClass;
    protected ClassLoader classLoader;
    protected ISharedObjectContainerInstantiator instantiator;
    protected String description;
    protected String[] argTypes;
    protected String[] argDefaults;
    protected String[] argNames;
    protected int hashCode = 0;
    protected static final String[] EMPTY = new String[0];

    public SharedObjectContainerDescription(ClassLoader loader, String name,
            String instantiatorClass, String desc) {
        this(loader, name, instantiatorClass, desc, EMPTY,
                EMPTY, EMPTY);
    }

    public SharedObjectContainerDescription(String name,
            String instantiatorClass, String desc) {
        this(null, name, instantiatorClass, desc);
    }

    public SharedObjectContainerDescription(ClassLoader loader, String name,
            String instantiatorClass, String desc, String[] argTypes,
            String[] argDefaults, String[] argNames) {
        this.classLoader = loader;
        if (name == null)
            throw new RuntimeException(
                    new InstantiationException(
                            "SharedObjectContainerDescription<init> name cannot be null"));
        this.name = name;
        if (instantiatorClass == null)
            throw new RuntimeException(
                    new InstantiationException(
                            "SharedObjectContainerDescription<init> instantiatorClass cannot be null"));
        this.instantiatorClass = instantiatorClass;
        this.hashCode = name.hashCode();
        this.description = desc;
        this.argTypes = argTypes;
        this.argDefaults = argDefaults;
        this.argNames = argNames;
    }

    public SharedObjectContainerDescription(String name,
            ISharedObjectContainerInstantiator inst, String desc,
            String[] argTypes, String[] argDefaults, String[] argNames) {
        if (name == null)
            throw new RuntimeException(
                    new InstantiationException(
                            "SharedObjectContainerDescription<init> name cannot be null"));
        if (inst == null)
            throw new RuntimeException(
                    new InstantiationException(
                            "SharedObjectContainerDescription<init> instantiator instance cannot be null"));
        this.instantiator = inst;
        this.name = name;
        this.classLoader = this.instantiator.getClass().getClassLoader();
        this.description = desc;
        this.argTypes = argTypes;
        this.argDefaults = argDefaults;
        this.argNames = argNames;
    }

    public SharedObjectContainerDescription(String name,
            ISharedObjectContainerInstantiator inst, String desc) {
        this(name, inst, desc, EMPTY, EMPTY, EMPTY);
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
        b.append("name:").append(name).append(";");
        if (instantiator == null)
            b.append("class:").append(instantiatorClass).append(";");
        else
            b.append("instantiator:").append(instantiator).append(";");
        b.append("desc:").append(description).append(";");
        b.append("argtypes:").append(Arrays.asList(argTypes)).append(";");
        b.append("argdefaults:").append(Arrays.asList(argDefaults)).append(";");
        b.append("argnames:").append(Arrays.asList(argNames)).append("]");
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

    public String[] getArgDefaults() {
        return argDefaults;
    }

    public String[] getArgTypes() {
        return argTypes;
    }

    public String[] getArgNames() {
        return argNames;
    }
}