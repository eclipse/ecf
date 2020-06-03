/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;
import org.eclipse.ecf.core.security.IConnectContext;

/**
 * Base implementation of IContainer. Subclasses may be created to fill out the
 * behavior of this base implementation. Also, adapter factories may be created
 * via adapterFactory extension point to allow adapters to be added to this
 * BaseContainer implementation without the need to create a separate IContainer
 * implementation class.
 */
public class BaseContainer extends AbstractContainer {

	public static class Instantiator extends BaseContainerInstantiator {

		/**
		 * @since 3.4
		 */
		public static final String NAME = "ecf.base"; //$NON-NLS-1$

		private static long nextBaseContainerID = 0L;

		public IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException {
			try {
				if (parameters != null && parameters.length > 0) {
					if (parameters[0] instanceof ID)
						return new BaseContainer((ID) parameters[0]);
					if (parameters[0] instanceof String)
						return new BaseContainer(IDFactory.getDefault().createStringID((String) parameters[0]));
				}
			} catch (IDCreateException e) {
				throw new ContainerCreateException("Could not create ID for basecontainer"); //$NON-NLS-1$
			}
			return new BaseContainer(nextBaseContainerID++);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
		 */
		public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
			return getInterfacesAndAdaptersForClass(BaseContainer.class);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.core.provider.BaseContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
		 */
		public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
			return new Class[][] { {}, {ID.class}, {String.class}};
		}

	}

	private ID id = null;

	protected BaseContainer(long idl) throws ContainerCreateException {
		try {
			this.id = IDFactory.getDefault().createLongID(idl);
		} catch (IDCreateException e) {
			throw new ContainerCreateException("Could not create ID for basecontainer", e); //$NON-NLS-1$
		}
	}

	protected BaseContainer(ID id) {
		Assert.isNotNull(id);
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID,
	 *      org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void connect(ID targetID, IConnectContext connectContext) throws ContainerConnectException {
		throw new ContainerConnectException("Connect not supported"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		// Nothing to disconnect
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainer#getConnectNamespace()
	 */
	public Namespace getConnectNamespace() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		return id;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("BaseContainer["); //$NON-NLS-1$
		sb.append("id=").append(getID()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return sb.toString();
	}
}
