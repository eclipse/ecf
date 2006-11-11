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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.core.ECFDebugOptions;
import org.eclipse.ecf.internal.core.ECFPlugin;

/**
 * Description of an {@link IContainer} type.
 * 
 * @see ContainerFactory
 */
public class ContainerTypeDescription {
	protected String name;

	protected String instantiatorClass;

	protected ClassLoader classLoader;

	protected IContainerInstantiator instantiator;

	protected String description;

	protected String[] parameterDefaults;

	protected int hashCode = 0;

	protected static final String[] EMPTY = new String[0];

	private static final int GET_PARAMETER_TYPES_ERROR_CODE = 2672;

	private static final int GET_SUPPORTED_ADAPTERS_ERROR_CODE = 2673;

	protected Map properties = null;

	public ContainerTypeDescription(ClassLoader loader, String name,
			String instantiatorClass, String desc) {
		this(loader, name, instantiatorClass, desc, EMPTY);
	}

	public ContainerTypeDescription(String name, String instantiatorClass,
			String desc) {
		this(null, name, instantiatorClass, desc);
	}

	public ContainerTypeDescription(ClassLoader loader, String name,
			String instantiatorClass, String desc, String[] parameterDefaults) {
		this(loader, name, instantiatorClass, desc, parameterDefaults, null);
	}

	public ContainerTypeDescription(ClassLoader loader, String name,
			String instantiatorClass, String desc, String[] parameterDefaults,
			Map props) {
		this.classLoader = loader;
		if (name == null)
			throw new RuntimeException(
					new InstantiationException(
							"SharedObjectContainerDescription<init> name cannot be null"));
		this.name = name;
		this.hashCode = name.hashCode();
		if (instantiatorClass == null)
			throw new RuntimeException(
					new InstantiationException(
							"SharedObjectContainerDescription<init> instantiatorClass cannot be null"));
		this.instantiatorClass = instantiatorClass;
		this.description = desc;
		this.parameterDefaults = parameterDefaults;
		this.properties = (props == null) ? new HashMap() : props;
	}

	public ContainerTypeDescription(String name, IContainerInstantiator inst,
			String desc) {
		this(name, inst, desc, EMPTY);
	}

	public ContainerTypeDescription(String name, IContainerInstantiator inst,
			String desc, String[] parameterDefaults) {
		this(name, inst, desc, parameterDefaults, null);
	}

	public ContainerTypeDescription(String name, IContainerInstantiator inst,
			String desc, String[] parameterDefaults, Map props) {
		if (name == null)
			throw new RuntimeException(
					new InstantiationException(
							"SharedObjectContainerDescription<init> name cannot be null"));
		this.name = name;
		this.hashCode = name.hashCode();
		if (inst == null)
			throw new RuntimeException(
					new InstantiationException(
							"SharedObjectContainerDescription<init> instantiator instance cannot be null"));
		this.instantiator = inst;
		this.classLoader = this.instantiator.getClass().getClassLoader();
		this.description = desc;
		this.parameterDefaults = parameterDefaults;
		this.properties = (props == null) ? new HashMap() : props;
	}

	/**
	 * Get ContainerTypeDescription name
	 * 
	 * @return String name for the ContainerTypeDescription. Will not be null.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get ClassLoader for this ContainerTypeDescription
	 * 
	 * @return ClassLoader associated with this ContainerTypeDescription
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ContainerTypeDescription))
			return false;
		ContainerTypeDescription scd = (ContainerTypeDescription) other;
		return scd.name.equals(name);
	}

	public int hashCode() {
		return hashCode;
	}

	public String toString() {
		StringBuffer b = new StringBuffer("ContainerTypeDescription[");
		b.append("name=").append(name).append(";");
		if (instantiator == null)
			b.append("class=").append(instantiatorClass).append(";");
		else
			b.append("instantiator=").append(instantiator).append(";");
		b.append("desc=").append(description).append(";");
		b.append("argdefaults=").append(Arrays.asList(parameterDefaults))
				.append(";");
		return b.toString();
	}

	protected IContainerInstantiator getInstantiator()
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
		instantiator = (IContainerInstantiator) clazz.newInstance();
	}

	/**
	 * Get the String description associated with this ContainerTypeDescription
	 * instance
	 * 
	 * @return String description. May be null.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get parameter defaults.
	 * 
	 * @return String [] default parameters
	 */
	public String[] getParameterDefaults() {
		return parameterDefaults;
	}

	/**
	 * Get array of supported adapters for the given container type description.
	 * Providers can implement this method to allow clients to inspect the
	 * adapter types implemented by the container described by the given
	 * description.
	 * 
	 * Note that the returned types do not guarantee that a subsequent call to
	 * {@link IContainer#getAdapter(Class)} with the same type name as a
	 * returned value will return a non-null result.
	 * <code>IContainer.getAdapter</code> may still return <code>null</code>. *
	 * 
	 * @return Class[] of supported adapters. Null will be returned if no
	 *         adapters are supported
	 */
	public String[] getSupportedAdapterTypes() {
		Trace.entering(ECFPlugin.getDefault(),
				ECFDebugOptions.METHODS_ENTERING, this.getClass(),
				"getSupportedAdapterTypes");
		String[] result = null;
		try {
			result = getInstantiator().getSupportedAdapterTypes(this);
		} catch (Exception e) {
			traceAndLogException(GET_SUPPORTED_ADAPTERS_ERROR_CODE,
					"getSupportedAdapterTypes", e);
		}
		Trace.exiting(ECFPlugin.getDefault(), ECFDebugOptions.METHODS_EXITING,
				this.getClass(), "getSupportedAdapterTypes", result);
		return result;
	}

	protected void traceAndLogException(int code, String method, Throwable e) {
		Trace
				.catching(ECFPlugin.getDefault(),
						ECFDebugOptions.EXCEPTIONS_CATCHING, this.getClass(),
						method, e);
		ECFPlugin.getDefault().getLog()
				.log(
						new Status(IStatus.ERROR, ECFPlugin.PLUGIN_ID, code,
								method, e));
	}

	/**
	 * Get array of parameter types for this ContainerTypeDescription. Each of
	 * the rows of the returned array specifies a Class[] of parameter types.
	 * These parameter types correspond to the types of Objects that can be
	 * passed into the second parameter of
	 * {@link IContainerInstantiator#createInstance(ContainerTypeDescription, Object[])}.
	 * For example, if this method returns a Class [] = {{ String.class,
	 * String.class }, { String.class }} this indicates that a call to
	 * createInstance(description,new String[] { "hello", "there" }) and a call
	 * to createInstance(description,new String[] { "hello" }) will be
	 * understood by the provider implementation.
	 * 
	 * @return Class[][] array of Class arrays. Each row corresponds to a
	 *         Class[] that describes the types of Objects for second parameter
	 *         to
	 *         {@link IContainerInstantiator#createInstance(ContainerTypeDescription, Object[])}.
	 *         Null may be returned
	 */
	public Class[][] getSupportedParameterTypes() {
		Trace.entering(ECFPlugin.getDefault(),
				ECFDebugOptions.METHODS_ENTERING, this.getClass(),
				"getParameterTypes");
		Class[][] result = null;
		try {
			result = getInstantiator().getSupportedParameterTypes(this);
		} catch (Exception e) {
			traceAndLogException(GET_PARAMETER_TYPES_ERROR_CODE,
					"getParameterTypes", e);
		}
		Trace.exiting(ECFPlugin.getDefault(), ECFDebugOptions.METHODS_EXITING,
				this.getClass(), "getParameterTypes", result);
		return result;
	}

	/**
	 * Get properties associated with this ContainerTypeDescription instance
	 * 
	 * @return Map the properties. Will not be null.
	 */
	public Map getProperties() {
		return properties;
	}
}