/****************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.datashare;

import java.util.Map;
import org.eclipse.ecf.core.identity.ID;

/**
 * Base class implementation of {@link IChannelConfig}. Subclasses may be
 * created as appropriate.
 * 
 */
public class BaseChannelConfig implements IChannelConfig {

	protected ID id = null;

	protected IChannelListener listener = null;

	protected Map properties = null;

	public BaseChannelConfig() {
		//
	}

	public BaseChannelConfig(ID id, IChannelListener listener, Map properties) {
		if (id == null)
			throw new NullPointerException("id cannot be null"); //$NON-NLS-1$
		this.id = id;
		this.listener = listener;
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IChannelConfig#getListener()
	 */
	public IChannelListener getListener() {
		return listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IChannelConfig#getProperties()
	 */
	public Map getProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}
}
