/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.provider;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;

/**
 * Interface that must be implemented by extensions of the containerFactory
 * extension point
 * 
 */
public interface IContainerInstantiator {
	/**
	 * Create instance of IContainer. This is the interface that container
	 * provider implementations must implement for the containerFactory
	 * extension point. The caller may optionally specify both argument types
	 * and arguments that will be passed into this method (and therefore to the
	 * provider implementation implementing this method). For example:
	 * <p>
	 * </p>
	 * <p>
	 * <b> ContainerFactory.getDefault().createContainer("foocontainer",new
	 * String [] { java.lang.String }, new Object { "hello" });</b>
	 * </p>
	 * <p>
	 * </p>
	 * 
	 * @param description
	 *            the ContainerTypeDescription associated with the registered
	 *            container provider implementation
	 * @param args
	 *            arguments specified by the caller. May be null if no arguments
	 *            are passed in by caller to
	 *            ContainerFactory.getDefault().createContainer(...)
	 * @return IContainer instance. The provider implementation must return a
	 *         valid object implementing IContainer OR throw a
	 *         ContainerCreateException. Null may not be returned.
	 * @throws ContainerCreateException
	 */
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException;
}