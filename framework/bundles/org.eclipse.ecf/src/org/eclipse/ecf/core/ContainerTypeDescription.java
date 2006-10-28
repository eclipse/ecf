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

import org.eclipse.ecf.core.provider.IContainerInstantiator;

/**
 * Description of an IContainer type implementation.
 * 
 */
public class ContainerTypeDescription {
	protected String name;

	protected String instantiatorClass;

	protected ClassLoader classLoader;

	protected IContainerInstantiator instantiator;

	protected String description;

	protected String[] argDefaults;

	protected int hashCode = 0;

	protected static final String[] EMPTY = new String[0];

	protected Map properties = null;

	public ContainerTypeDescription(String name) {
		this(name, null);
	}

	public ContainerTypeDescription(String name, String description) {
		this(name, description, (Map) null);
	}

	public ContainerTypeDescription(String name, String description,
			Map properties) {
		if (name == null)
			throw new NullPointerException("name cannot be null");
		this.name = name;
		this.description = description;
		this.properties = (properties == null)?new HashMap():properties;
	}

	public ContainerTypeDescription(ClassLoader loader, String name,
			String instantiatorClass, String desc) {
		this(loader, name, instantiatorClass, desc, EMPTY);
	}

	public ContainerTypeDescription(String name, String instantiatorClass,
			String desc) {
		this(null, name, instantiatorClass, desc);
	}

	protected ContainerTypeDescription(ClassLoader loader, String name,
			String instantiatorClass, String desc, String[] argDefaults) {
		this(loader, name, instantiatorClass, desc, argDefaults, null);
	}

	protected ContainerTypeDescription(ClassLoader loader, String name,
			String instantiatorClass, String desc, String[] argDefaults,
			Map props) {
		this.classLoader = loader;
		if (name == null)
			throw new NullPointerException("name cannot be null");
		this.name = name;
		if (instantiatorClass == null)
			throw new NullPointerException("instantiatorClass cannot be null");
		this.instantiatorClass = instantiatorClass;
		this.hashCode = name.hashCode();
		this.description = desc;
		this.argDefaults = argDefaults;
		if (props != null)
			this.properties = props;
	}

	public ContainerTypeDescription(String name, IContainerInstantiator inst,
			String desc, String[] argDefaults) {
		this(name, inst, desc, argDefaults, null);
	}

	public ContainerTypeDescription(String name, IContainerInstantiator inst,
			String desc, String[] argDefaults, Map props) {
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
		this.argDefaults = argDefaults;
		if (props != null)
			this.properties = props;
	}

	public ContainerTypeDescription(String name, IContainerInstantiator inst,
			String desc) {
		this(name, inst, desc, EMPTY);
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
		b.append("argdefaults=").append(Arrays.asList(argDefaults)).append(";");
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

	public String[] getArgDefaults() {
		return argDefaults;
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