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

package org.eclipse.ecf.core.provider;

import java.util.*;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.internal.core.ECFPlugin;

/**
 *  Default implementation of {@link IContainerInstantiator}.  ECF provider implementers
 *  may subclass as desired.
 */
public class BaseContainerInstantiator implements IContainerInstantiator {

	protected static String[] NO_ADAPTERS_ARRAY = new String[] {IContainer.class.getName()};
	protected static String[] EMPTY_STRING_ARRAY = new String[] {};
	protected static Class[][] EMPTY_CLASS_ARRAY = new Class[][] {{}};

	/**
	 * @since 3.6
	 */
	protected Integer getIntegerFromArg(Object arg) {
		if (arg == null)
			return new Integer(-1);
		if (arg instanceof Integer)
			return (Integer) arg;
		else if (arg instanceof String) {
			return new Integer((String) arg);
		} else
			throw new IllegalArgumentException("arg=" + arg + " is not of integer type"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected Set getAdaptersForClass(Class clazz) {
		Set result = new HashSet();
		IAdapterManager adapterManager = ECFPlugin.getDefault().getAdapterManager();
		if (adapterManager != null)
			result.addAll(Arrays.asList(adapterManager.computeAdapterTypes(clazz)));
		return result;
	}

	protected Set getInterfacesForClass(Set s, Class clazz) {
		if (clazz.equals(Object.class))
			return s;
		s.addAll(getInterfacesForClass(s, clazz.getSuperclass()));
		s.addAll(Arrays.asList(clazz.getInterfaces()));
		return s;
	}

	protected Set getInterfacesForClass(Class clazz) {
		Set clazzes = getInterfacesForClass(new HashSet(), clazz);
		Set result = new HashSet();
		for (Iterator i = clazzes.iterator(); i.hasNext();)
			result.add(((Class) i.next()).getName());
		return result;
	}

	protected String[] getInterfacesAndAdaptersForClass(Class clazz) {
		Set result = getAdaptersForClass(clazz);
		result.addAll(getInterfacesForClass(clazz));
		return (String[]) result.toArray(new String[] {});
	}

	/**
	 * @since 3.6
	 */
	@SuppressWarnings("rawtypes")
	protected Map getMapFromParameters(Object[] parameters) {
		if (parameters != null && parameters.length > 0)
			for (Object p : parameters)
				if (p instanceof Map)
					return (Map) p;
		return null;
	}

	/**
	 * @since 3.6
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getMapParameter(Object[] parameters, String key, Class<T> clazz, T def) {
		@SuppressWarnings("rawtypes")
		Map m = getMapFromParameters(parameters);
		if (m != null) {
			Object o = m.get(key);
			if (clazz.isInstance(o))
				return (T) o;
		}
		return def;
	}

	/**
	 * @since 3.6
	 */
	protected <T> T getMapParameter(Object[] parameters, String key, Class<T> clazz) {
		return getMapParameter(parameters, key, clazz, null);
	}

	/**
	 * @since 3.6
	 */
	protected String getMapParameterString(Object[] parameters, String key, String def) {
		return getMapParameter(parameters, key, String.class, def);
	}

	/**
	 * @since 3.6
	 */
	protected String getMapParameterString(Object[] parameters, String key) {
		return getMapParameter(parameters, key, String.class, null);
	}

	public IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException {
		throw new ContainerCreateException("createInstance not supported"); //$NON-NLS-1$
	}

	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		return NO_ADAPTERS_ARRAY;
	}

	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		return EMPTY_CLASS_ARRAY;
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}

}
