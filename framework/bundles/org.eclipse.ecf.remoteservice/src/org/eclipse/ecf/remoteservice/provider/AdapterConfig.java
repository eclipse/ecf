/****************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.provider;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * An adapter config is used to setup an adaptable.   
 * @since 8.7
 */
public class AdapterConfig {
	private final IAdapterFactory adapterFactory;
	private final Class<?> adaptable;

	/**
	 * 
	 * @param adapterFactory the adapter factory to use for the given adaptable.  Must not be <code>null</code>
	 * @param adaptable the Class that the adapterFactory is to use as the adaptable.
	 * Must not be <code>null</code>.
	 */
	public AdapterConfig(IAdapterFactory adapterFactory, Class<?> adaptable) {
		this.adapterFactory = adapterFactory;
		Assert.isNotNull(this.adapterFactory);
		this.adaptable = adaptable;
		Assert.isNotNull(this.adaptable);
	}

	public IAdapterFactory getAdapterFactory() {
		return this.adapterFactory;
	}

	public Class<?> getAdaptable() {
		return this.adaptable;
	}
}