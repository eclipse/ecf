/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.comm;

import org.eclipse.ecf.core.comm.provider.ISynchAsynchConnectionInstantiator;

public class ConnectionTypeDescription {
	protected String name;
	protected String instantiatorClass;
	protected ISynchAsynchConnectionInstantiator instantiator;
	protected int hashCode = 0;
	protected ClassLoader classLoader = null;
	protected String description;
	protected String[] argTypes;
	protected String[] argDefaults;
	protected String[] argNames;
	protected static final String[] EMPTY = new String[0];

	public ConnectionTypeDescription(ClassLoader loader, String name,
			String instantiatorClass, String desc, String[] defTypes,
			String[] defValues, String[] defNames) {
		if (name == null)
			throw new RuntimeException(new InstantiationException(
					"ConnectionTypeDescription<init> name cannot be null"));
		if (instantiatorClass == null)
			throw new RuntimeException(
					new InstantiationException(
							"ConnectionTypeDescription<init> instantiatorClass cannot be null"));
		this.classLoader = loader;
		this.name = name;
		this.instantiatorClass = instantiatorClass;
		this.hashCode = name.hashCode();
		this.argTypes = defTypes;
		this.argDefaults = defValues;
		this.argNames = defNames;
	}

	public ConnectionTypeDescription(ClassLoader loader, String name,
			String instantiatorClass, String desc) {
		this(loader, name, instantiatorClass, desc, EMPTY, EMPTY, EMPTY);
	}

	public ConnectionTypeDescription(String name, String instantiatorClass,
			String desc) {
		this(null, name, instantiatorClass, desc);
	}

	public ConnectionTypeDescription(String name,
			ISynchAsynchConnectionInstantiator inst, String desc,
			String[] defTypes, String[] defValues, String[] defNames) {
		if (name == null)
			throw new RuntimeException(new InstantiationException(
					"ConnectionTypeDescription<init> name cannot be null"));
		if (inst == null)
			throw new RuntimeException(
					new InstantiationException(
							"ConnectionTypeDescription<init> instantiator instance cannot be null"));
		this.instantiator = inst;
		this.name = name;
		this.classLoader = this.instantiator.getClass().getClassLoader();
		this.instantiatorClass = this.instantiator.getClass().getName();
		this.hashCode = name.hashCode();
		this.description = desc;
		this.argTypes = defTypes;
		this.argDefaults = defValues;
		this.argNames = defNames;
	}

	public ConnectionTypeDescription(String name,
			ISynchAsynchConnectionInstantiator inst, String desc) {
		this(name, inst, desc, EMPTY, EMPTY, EMPTY);
	}

	public String getName() {
		return name;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ConnectionTypeDescription))
			return false;
		ConnectionTypeDescription scd = (ConnectionTypeDescription) other;
		return scd.name.equals(name);
	}

	public int hashCode() {
		return hashCode;
	}

	public String toString() {
		StringBuffer b = new StringBuffer("ConnectionTypeDescription[");
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

	public String[] getArgDefaults() {
		return argDefaults;
	}

	public String[] getArgNames() {
		return argNames;
	}

	public String[] getArgTypes() {
		return argTypes;
	}
}