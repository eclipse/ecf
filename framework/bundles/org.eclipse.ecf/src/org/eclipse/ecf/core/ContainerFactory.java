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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.util.AbstractFactory;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.core.ECFDebugOptions;
import org.eclipse.ecf.internal.core.ECFPlugin;
import org.eclipse.ecf.internal.core.IDisposable;

/**
 * Factory for creating {@link IContainer} instances. This class provides ECF
 * clients an entry point to constructing {@link IContainer} instances. <br>
 * <br>
 * Here is an example use of the ContainerFactory to construct an instance of
 * the 'standalone' container (has no connection to other containers): <br>
 * <br>
 * <code>
 * 	    IContainer container = <br>
 * 			ContainerFactory.getDefault().createContainer("ecf.generic.client");
 *      <br><br>
 *      ...further use of container here...
 * </code>
 * For more details on the creation and lifecycle of IContainer instances created via this 
 * factory see {@link IContainer}.
 * 
 * @see IContainer
 */
public class ContainerFactory implements IContainerFactory {
	private static final int DISPOSE_ERROR_CODE = 100;
	private static Hashtable containerdescriptions = new Hashtable();
	protected static IContainerFactory instance = null;
	
	protected static Map containers = new WeakHashMap();
	
	static {
		instance = new ContainerFactory();
	}

	protected ContainerFactory() {
		try {
			ECFPlugin.getDefault().addDisposable(new IDisposable() {
				public void dispose() {
					doDispose();
				}});
		} catch (Exception e) {
			System.err.println("WARNING:  Exception accessing ECFPlugin within ContainerFactory initialization.  May not be running as OSGI bundle");
		}
	}

	public static IContainerFactory getDefault() {
		return instance;
	}

	private static void trace(String msg) {
		Trace.trace(ECFPlugin.getDefault(),msg);
	}

	private static void dumpStack(String msg, Throwable e) {
		Trace.catching(ECFPlugin.getDefault(), ECFDebugOptions.EXCEPTIONS_CATCHING, ContainerFactory.class, "dumpStack", e);
	}
	protected void addContainer(IContainer container) {
		containers.put(container,null);
	}
	protected void removeContainer(IContainer container) {
		containers.remove(container);
	}
	protected void doDispose() {
		for (Iterator i = containers.keySet().iterator(); i.hasNext();) {
			IContainer c = (IContainer) i.next();
			if (c != null) {
				try {
					c.dispose();
				} catch (Exception e) {
					// Log exception
					ECFPlugin.log(new Status(Status.ERROR, ECFPlugin
							.getDefault().getBundle().getSymbolicName(),
							DISPOSE_ERROR_CODE, "container dispose error", e));
				}
			}
		}
		containers.clear();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#addDescription(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public ContainerTypeDescription addDescription(ContainerTypeDescription scd) {
		trace("addDescription(" + scd + ")");
		return addDescription0(scd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#getDescriptions()
	 */
	public List getDescriptions() {
		return getDescriptions0();
	}

	protected List getDescriptions0() {
		return new ArrayList(containerdescriptions.values());
	}

	protected ContainerTypeDescription addDescription0(ContainerTypeDescription n) {
		if (n == null)
			return null;
		return (ContainerTypeDescription) containerdescriptions.put(n.getName(), n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#containsDescription(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public boolean containsDescription(ContainerTypeDescription scd) {
		return containsDescription0(scd);
	}

	protected boolean containsDescription0(ContainerTypeDescription scd) {
		if (scd == null)
			return false;
		return containerdescriptions.containsKey(scd.getName());
	}

	protected ContainerTypeDescription getDescription0(ContainerTypeDescription scd) {
		if (scd == null)
			return null;
		return (ContainerTypeDescription) containerdescriptions.get(scd.getName());
	}

	protected ContainerTypeDescription getDescription0(String name) {
		if (name == null)
			return null;
		return (ContainerTypeDescription) containerdescriptions.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainerFactory#getDescriptionByName(java.lang.String)
	 */
	public ContainerTypeDescription getDescriptionByName(String name)
			throws ContainerCreateException {
		trace("getDescriptionByName(" + name + ")");
		ContainerTypeDescription res = getDescription0(name);
		if (res == null) {
			throw new ContainerCreateException(
					"ContainerTypeDescription named '" + name + "' not found");
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#createContainer(org.eclipse.ecf.core.ContainerTypeDescription,
	 *      java.lang.String[], java.lang.Object[])
	 */
	public IContainer createContainer(ContainerTypeDescription desc,
			String[] argTypes, Object[] args)
			throws ContainerCreateException {
		trace("createContainer(" + desc + ","
				+ Trace.getArgumentsString(argTypes) + ","
				+ Trace.getArgumentsString(args) + ")");
		if (desc == null)
			throw new ContainerCreateException(
					"ContainerTypeDescription cannot be null");
		ContainerTypeDescription cd = getDescription0(desc);
		if (cd == null)
			throw new ContainerCreateException(
					"ContainerTypeDescription named '" + desc.getName()
							+ "' not found");
		Class clazzes[] = null;
		IContainerInstantiator instantiator = null;
		try {
			instantiator = (IContainerInstantiator) cd.getInstantiator();
			clazzes = AbstractFactory.getClassesForTypes(argTypes, args, cd
					.getClassLoader());
		} catch (Exception e) {
			ContainerCreateException newexcept = new ContainerCreateException(
					"createContainer exception with description: " + desc + ": "
							+ e.getClass().getName() + ": " + e.getMessage());
			newexcept.setStackTrace(e.getStackTrace());
			dumpStack("Exception in createContainer", newexcept);
			throw newexcept;
		}
		if (instantiator == null)
			throw new ContainerCreateException(
					"Instantiator for ContainerTypeDescription " + cd.getName()
							+ " is null");
		// Ask instantiator to actually create instance
		IContainer container = instantiator.createInstance(desc, clazzes, args);
		if (container == null) throw new ContainerCreateException("Container instantiator returned null for createInstance " + cd.getName());
		// Add to containers map
		addContainer(container);
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#createContainer(java.lang.String)
	 */
	public IContainer createContainer(String descriptionName)
			throws ContainerCreateException {
		return createContainer(getDescriptionByName(descriptionName), null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#createContainer(java.lang.String,
	 *      java.lang.Object[])
	 */
	public IContainer createContainer(String descriptionName, Object[] args)
			throws ContainerCreateException {
		return createContainer(getDescriptionByName(descriptionName), null, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#createContainer(java.lang.String,
	 *      java.lang.String[], java.lang.Object[])
	 */
	public IContainer createContainer(String descriptionName, String[] argsTypes,
			Object[] args) throws ContainerCreateException {
		return createContainer(getDescriptionByName(descriptionName), argsTypes,
				args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#removeDescription(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public ContainerTypeDescription removeDescription(ContainerTypeDescription scd) {
		trace("removeDescription(" + scd + ")");
		return removeDescription0(scd);
	}

	protected ContainerTypeDescription removeDescription0(ContainerTypeDescription n) {
		if (n == null)
			return null;
		return (ContainerTypeDescription) containerdescriptions.remove(n.getName());
	}
}