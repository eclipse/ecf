/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery.identity;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.Namespace;

/**
 * ServiceTypeID base class.
 */
public class ServiceTypeID extends BaseID implements IServiceTypeID {

	private static final long serialVersionUID = 2546630451825262145L;
	protected String type = null;
	protected String namingAuthority;
	protected String[] protocols;
	protected String[] scopes;
	protected String[] services;

	protected ServiceTypeID(Namespace namespace, String aType) {
		super(namespace);
		Assert.isNotNull(aType);
		type = aType;
	}

	protected ServiceTypeID(Namespace namespace, String typeName, String[] services, String[] scopes, String[] protocols, String namingAuthority) {
		this(namespace, typeName);
		this.services = services;
		this.scopes = scopes;
		this.protocols = protocols;
		this.namingAuthority = namingAuthority;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#getName()
	 */
	public String getName() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceCompareTo(org.eclipse.ecf.core.identity.BaseID)
	 */
	protected int namespaceCompareTo(BaseID o) {
		if (o instanceof ServiceTypeID) {
			final ServiceTypeID other = (ServiceTypeID) o;
			final String typename = other.getName();
			return getName().compareTo(typename);
		} else {
			return 1;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceEquals(org.eclipse.ecf.core.identity.BaseID)
	 */
	protected boolean namespaceEquals(BaseID o) {
		if (o == null)
			return false;
		if (o instanceof ServiceTypeID) {
			final ServiceTypeID other = (ServiceTypeID) o;
			if (other.getName().equals(getName())) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceGetName()
	 */
	protected String namespaceGetName() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceHashCode()
	 */
	protected int namespaceHashCode() {
		return getName().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.identity.IServiceTypeID#getNamingAuthority()
	 */
	public String getNamingAuthority() {
		return namingAuthority;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.identity.IServiceTypeID#getProtocols()
	 */
	public String[] getProtocols() {
		return protocols;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.identity.IServiceTypeID#getScopes()
	 */
	public String[] getScopes() {
		return scopes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.identity.IServiceTypeID#getServices()
	 */
	public String[] getServices() {
		return services;
	}
}
