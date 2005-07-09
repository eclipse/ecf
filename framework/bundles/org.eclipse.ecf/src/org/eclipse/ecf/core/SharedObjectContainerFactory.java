/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.ecf.core.provider.ISharedObjectContainerInstantiator;
import org.eclipse.ecf.core.util.AbstractFactory;
import org.eclipse.ecf.internal.core.Trace;

/**
 * Factory for creating {@link ISharedObjectContainer} instances. This
 * class provides ECF clients an entry point to constructing {@link ISharedObjectContainer}
 * instances.  
 * <br>
 * <br>
 * Here is an example use of the SharedObjectContainerFactory to construct an instance
 * of the 'standalone' container (has no connection to other containers):
 * <br><br>
 * <code>
 * 	    ISharedObjectContainer container = <br>
 * 			SharedObjectContainerFactory.getDefault().makeSharedObjectContainer('standalone');
 *      <br><br>
 *      ...further use of container variable here...
 * </code>
 * 
 */
public class SharedObjectContainerFactory implements ISharedObjectContainerFactory {

    private static Trace debug = Trace.create("containerfactory");
    
    private static Hashtable containerdescriptions = new Hashtable();
    protected static ISharedObjectContainerFactory instance = null;
    
    static {
    	instance = new SharedObjectContainerFactory();
    }
    protected SharedObjectContainerFactory() {
    }
    public static ISharedObjectContainerFactory getDefault() {
    	return instance;
    }
    private static void trace(String msg) {
        if (Trace.ON && debug != null) {
            debug.msg(msg);
        }
    }

    private static void dumpStack(String msg, Throwable e) {
        if (Trace.ON && debug != null) {
            debug.dumpStack(e, msg);
        }
    }
    /*
     * Add a SharedObjectContainerDescription to the set of known
     * SharedObjectContainerDescriptions.
     * 
     * @param scd the SharedObjectContainerDescription to add to this factory
     * @return SharedObjectContainerDescription the old description of the same
     * name, null if none found
     */
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#addDescription(org.eclipse.ecf.core.SharedObjectContainerDescription)
	 */
    public SharedObjectContainerDescription addDescription(
            SharedObjectContainerDescription scd) {
        trace("addDescription("+scd+")");
        return addDescription0(scd);
    }
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#getDescriptions()
	 */
    public List getDescriptions() {
        return getDescriptions0();
    }
    protected List getDescriptions0() {
        return new ArrayList(containerdescriptions.values());
    }
    protected SharedObjectContainerDescription addDescription0(
            SharedObjectContainerDescription n) {
        if (n == null)
            return null;
        return (SharedObjectContainerDescription) containerdescriptions.put(n
                .getName(), n);
    }
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#containsDescription(org.eclipse.ecf.core.SharedObjectContainerDescription)
	 */
    public boolean containsDescription(
            SharedObjectContainerDescription scd) {
        return containsDescription0(scd);
    }
    protected boolean containsDescription0(
            SharedObjectContainerDescription scd) {
        if (scd == null)
            return false;
        return containerdescriptions.containsKey(scd.getName());
    }
    protected SharedObjectContainerDescription getDescription0(
            SharedObjectContainerDescription scd) {
        if (scd == null)
            return null;
        return (SharedObjectContainerDescription) containerdescriptions.get(scd
                .getName());
    }
    protected SharedObjectContainerDescription getDescription0(
            String name) {
        if (name == null)
            return null;
        return (SharedObjectContainerDescription) containerdescriptions.get(name);
    }
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#getDescriptionByName(java.lang.String)
	 */
    public SharedObjectContainerDescription getDescriptionByName(
            String name) throws SharedObjectContainerInstantiationException {
        trace("getDescriptionByName("+name+")");
        SharedObjectContainerDescription res = getDescription0(name);
        if (res == null) {
            throw new SharedObjectContainerInstantiationException(
                    "SharedObjectContainerDescription named '" + name
                            + "' not found");
        }
        return res;
    }
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#makeSharedObjectContainer(org.eclipse.ecf.core.SharedObjectContainerDescription, java.lang.String[], java.lang.Object[])
	 */
    public ISharedObjectContainer makeSharedObjectContainer(
            SharedObjectContainerDescription desc, String[] argTypes,
            Object[] args) throws SharedObjectContainerInstantiationException {
        trace("makeSharedObjectContainer("+desc+","+Trace.convertStringAToString(argTypes)+","+Trace.convertObjectAToString(args)+")");
        if (desc == null)
            throw new SharedObjectContainerInstantiationException(
                    "SharedObjectContainerDescription cannot be null");
        SharedObjectContainerDescription cd = getDescription0(desc);
        if (cd == null)
            throw new SharedObjectContainerInstantiationException(
                    "SharedObjectContainerDescription named '" + desc.getName()
                            + "' not found");
        Class clazzes[] = null;
        ISharedObjectContainerInstantiator instantiator = null;
        try {
            instantiator = (ISharedObjectContainerInstantiator) cd
            .getInstantiator();
            clazzes = AbstractFactory.getClassesForTypes(argTypes, args, cd.getClassLoader());
        } catch (Exception e) {
            SharedObjectContainerInstantiationException newexcept = new SharedObjectContainerInstantiationException(
                    "makeSharedObjectContainer exception with description: "+desc+": "+e.getClass().getName()+": "+e.getMessage());
            newexcept.setStackTrace(e.getStackTrace());
            dumpStack("Exception in makeSharedObjectContainer",newexcept);
            throw newexcept;
        }
        if (instantiator == null)
            throw new SharedObjectContainerInstantiationException(
                    "Instantiator for SharedObjectContainerDescription "
                            + cd.getName() + " is null");
        // Ask instantiator to actually create instance
        return (ISharedObjectContainer) instantiator
                .makeInstance(desc,clazzes, args);
    }
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#makeSharedObjectContainer(java.lang.String)
	 */
    public ISharedObjectContainer makeSharedObjectContainer(
            String descriptionName)
            throws SharedObjectContainerInstantiationException {
        return makeSharedObjectContainer(
                getDescriptionByName(descriptionName), null, null);
    }
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#makeSharedObjectContainer(java.lang.String, java.lang.Object[])
	 */
    public ISharedObjectContainer makeSharedObjectContainer(
            String descriptionName, Object[] args)
            throws SharedObjectContainerInstantiationException {
        return makeSharedObjectContainer(
                getDescriptionByName(descriptionName), null, args);
    }
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#makeSharedObjectContainer(java.lang.String, java.lang.String[], java.lang.Object[])
	 */
    public ISharedObjectContainer makeSharedObjectContainer(
            String descriptionName, String[] argsTypes, Object[] args)
            throws SharedObjectContainerInstantiationException {
        return makeSharedObjectContainer(
                getDescriptionByName(descriptionName), argsTypes, args);
    }
    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#removeDescription(org.eclipse.ecf.core.SharedObjectContainerDescription)
	 */
    public SharedObjectContainerDescription removeDescription(
            SharedObjectContainerDescription scd) {
        trace("removeDescription("+scd+")");
        return removeDescription0(scd);
    }
    protected SharedObjectContainerDescription removeDescription0(
            SharedObjectContainerDescription n) {
        if (n == null)
            return null;
        return (SharedObjectContainerDescription) containerdescriptions.remove(n
                .getName());
    }

}