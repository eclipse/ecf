/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.core.ECFDebugOptions;
import org.eclipse.ecf.internal.core.ECFPlugin;
import org.eclipse.ecf.internal.core.IDisposable;
import org.eclipse.ecf.internal.core.Messages;

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
 * </code> For more details on the creation
 * and lifecycle of IContainer instances created via this factory see
 * {@link IContainer}.
 * 
 * @see IContainer
 * @see IContainerFactory
 */
public class ContainerFactory implements IContainerFactory, IContainerManager {

	public static final String BASE_CONTAINER_NAME = Messages.ContainerFactory_Base_Container_Name;
	
	private static Hashtable containerdescriptions = new Hashtable();

	protected static IContainerFactory instance = null;

	protected static Map containers = Collections.synchronizedMap(new HashMap());

	static {
		instance = new ContainerFactory();
	}

	protected ContainerFactory() {
		ECFPlugin ecfplugin = ECFPlugin.getDefault();
		if (ecfplugin != null) {
			ecfplugin.addDisposable(new IDisposable() {
				public void dispose() {
					doDispose();
				}
			});
		}
	}

	public static IContainerFactory getDefault() {
		return instance;
	}

	protected void addContainer(IContainer container) {
		containers.put(container.getID(), container);
	}

	protected void removeContainer(IContainer container) {
		containers.remove(container.getID());
	}

	protected void doDispose() {
		synchronized (containers) {
			for (Iterator i = containers.keySet().iterator(); i.hasNext();) {
				IContainer c = (IContainer) i.next();
				if (c != null) {
					try {
						c.dispose();
					} catch (Exception e) {
						// Log exception
						ECFPlugin.getDefault().log(new Status(Status.ERROR, ECFPlugin
								.getDefault().getBundle().getSymbolicName(),
								Status.ERROR, "container dispose error", e)); //$NON-NLS-1$
						Trace.catching(ECFPlugin.PLUGIN_ID,
								ECFDebugOptions.EXCEPTIONS_CATCHING,
								ContainerFactory.class, "doDispose", e); //$NON-NLS-1$
					}
				}
			}
			containers.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#addDescription(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public ContainerTypeDescription addDescription(ContainerTypeDescription scd) {
		Trace.entering(ECFPlugin.PLUGIN_ID,
				ECFDebugOptions.METHODS_ENTERING, ContainerFactory.class,
				"addDescription", scd); //$NON-NLS-1$
		ContainerTypeDescription result = addDescription0(scd);
		Trace.exiting(ECFPlugin.PLUGIN_ID, ECFDebugOptions.METHODS_EXITING,
				ContainerFactory.class, "addDescription", result); //$NON-NLS-1$
		return result;
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

	protected ContainerTypeDescription addDescription0(
			ContainerTypeDescription n) {
		if (n == null)
			return null;
		return (ContainerTypeDescription) containerdescriptions.put(
				n.getName(), n);
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

	protected ContainerTypeDescription getDescription0(
			ContainerTypeDescription scd) {
		if (scd == null)
			return null;
		return (ContainerTypeDescription) containerdescriptions.get(scd
				.getName());
	}

	protected ContainerTypeDescription getDescription0(String name) {
		if (name == null)
			return null;
		return (ContainerTypeDescription) containerdescriptions.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#getDescriptionByName(java.lang.String)
	 */
	public ContainerTypeDescription getDescriptionByName(String name) {
		Trace.entering(ECFPlugin.PLUGIN_ID,
				ECFDebugOptions.METHODS_ENTERING, ContainerFactory.class,
				"getDescriptionByName", name); //$NON-NLS-1$
		ContainerTypeDescription res = getDescription0(name);
		Trace.exiting(ECFPlugin.PLUGIN_ID, ECFDebugOptions.METHODS_EXITING,
				ContainerFactory.class, "getDescriptionByName", res); //$NON-NLS-1$
		return res;
	}

	protected void throwContainerCreateException(String message,
			Throwable cause, String method) throws ContainerCreateException {
		ContainerCreateException except = (cause == null) ? new ContainerCreateException(
				message)
				: new ContainerCreateException(message, cause);
		Trace.throwing(ECFPlugin.PLUGIN_ID,
				ECFDebugOptions.EXCEPTIONS_THROWING, ContainerFactory.class,
				method, except);
		throw except;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainerFactory#createContainer()
	 */
	public IContainer createContainer() throws ContainerCreateException {
		return createContainer(BASE_CONTAINER_NAME);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#createContainer(org.eclipse.ecf.core.ContainerTypeDescription,
	 *      java.lang.Object[])
	 */
	public IContainer createContainer(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		String method = "createContainer"; //$NON-NLS-1$
		Trace.entering(ECFPlugin.PLUGIN_ID,
				ECFDebugOptions.METHODS_ENTERING, ContainerFactory.class,
				method, new Object[] { description,
						Trace.getArgumentsString(parameters) });
		if (description == null)
			throwContainerCreateException(
					Messages.ContainerFactory_Exception_Create_Container, null, method);
		ContainerTypeDescription cd = getDescription0(description);
		if (cd == null)
			throwContainerCreateException("ContainerTypeDescription '" //$NON-NLS-1$
					+ description.getName() + "' not found", null, method); //$NON-NLS-1$
		IContainerInstantiator instantiator = null;
		try {
			instantiator = cd.getInstantiator();
		} catch (Exception e) {
			throwContainerCreateException(
					"createContainer cannot get IContainerInstantiator for description : " //$NON-NLS-1$
							+ description, e, method);
		}
		// Ask instantiator to actually create instance
		IContainer container = instantiator.createInstance(description,
				parameters);
		if (container == null)
			throwContainerCreateException("Instantiator returned null for '" //$NON-NLS-1$
					+ cd.getName() + "'", null, method); //$NON-NLS-1$
		if (container.getID() == null) throwContainerCreateException("Container ID cannot be null",null,method);
		// Add to containers map
		addContainer(container);
		Trace.exiting(ECFPlugin.PLUGIN_ID, ECFDebugOptions.METHODS_EXITING,
				ContainerFactory.class, method, container);
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#createContainer(java.lang.String)
	 */
	public IContainer createContainer(String descriptionName)
			throws ContainerCreateException {
		return createContainer(getDescriptionByName(descriptionName), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#createContainer(java.lang.String,
	 *      java.lang.Object[])
	 */
	public IContainer createContainer(String descriptionName,
			Object[] parameters) throws ContainerCreateException {
		return createContainer(getDescriptionByName(descriptionName),
				parameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#removeDescription(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public ContainerTypeDescription removeDescription(
			ContainerTypeDescription scd) {
		Trace.entering(ECFPlugin.PLUGIN_ID,
				ECFDebugOptions.METHODS_ENTERING, ContainerFactory.class,
				"removeDescription", scd); //$NON-NLS-1$
		ContainerTypeDescription description = removeDescription0(scd);
		Trace.exiting(ECFPlugin.PLUGIN_ID, ECFDebugOptions.METHODS_EXITING,
				ContainerFactory.class, "removeDescription", description); //$NON-NLS-1$
		return description;

	}

	protected ContainerTypeDescription removeDescription0(
			ContainerTypeDescription n) {
		if (n == null)
			return null;
		return (ContainerTypeDescription) containerdescriptions.remove(n
				.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainerFactory#getDescriptionsForContainerAdapter(java.lang.Class)
	 */
	public ContainerTypeDescription[] getDescriptionsForContainerAdapter(
			Class containerAdapter) {
		if (containerAdapter == null)
			throw new NullPointerException(Messages.ContainerFactory_Exception_Adapter_Not_Null);
		List result = new ArrayList();
		List descriptions = getDescriptions();
		for (Iterator i = descriptions.iterator(); i.hasNext();) {
			ContainerTypeDescription description = (ContainerTypeDescription) i
					.next();
			String[] supportedAdapters = description.getSupportedAdapterTypes();
			if (supportedAdapters != null) {
				for (int j = 0; j < supportedAdapters.length; j++) {
					if (supportedAdapters[j].equals(containerAdapter.getName()))
						result.add(description);
				}
			}
		}
		return (ContainerTypeDescription[]) result
				.toArray(new ContainerTypeDescription[] {});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainerManager#getAllContainers()
	 */
	public IContainer[] getAllContainers() {
		List containersList = new ArrayList(containers.values());
		return (IContainer[]) containersList.toArray(new IContainer[] {});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainerManager#getContainer(org.eclipse.ecf.core.identity.ID)
	 */
	public IContainer getContainer(ID containerID) {
		if (containerID == null) return null;
		synchronized (containers) {
			for(Iterator i=containers.keySet().iterator(); i.hasNext(); ) {
				IContainer container = (IContainer) containers.get(containerID);
				if (container != null) return container;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainerManager#hasContainer(org.eclipse.ecf.core.identity.ID)
	 */
	public boolean hasContainer(ID containerID) {
		return containers.containsKey(containerID);
	}
}