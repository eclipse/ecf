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

	protected static final String[] DEFAULT_PROTO = new String[] {"unknown"}; //$NON-NLS-1$
	protected static final String[] DEFAULT_SCOPE = new String[] {"default"}; //$NON-NLS-1$
	protected static final String DEFAULT_NA = "IANA"; //$NON-NLS-1$
	protected static final String DELIM = "._"; //$NON-NLS-1$

	protected String typeName = ""; //$NON-NLS-1$
	protected String namingAuthority;
	protected String[] protocols;
	protected String[] scopes;
	protected String[] services;

	protected ServiceTypeID(Namespace namespace) {
		super(namespace);
	}

	protected ServiceTypeID(Namespace namespace, String[] services, String[] scopes, String[] protocols, String namingAuthority) {
		super(namespace);
		Assert.isNotNull(services);
		this.services = services;
		Assert.isNotNull(scopes);
		this.scopes = scopes;
		Assert.isNotNull(protocols);
		this.protocols = protocols;
		Assert.isNotNull(namingAuthority);
		this.namingAuthority = namingAuthority;
		createType();
		Assert.isNotNull(typeName);
	}

	protected void createType() {
		final StringBuffer buf = new StringBuffer();
		//services
		buf.append("_"); //$NON-NLS-1$
		for (int i = 0; i < services.length; i++) {
			buf.append(services[i]);
			buf.append(DELIM);
		}
		//protocols
		for (int i = 0; i < protocols.length; i++) {
			buf.append(protocols[i]);
			buf.append(DELIM);
		}
		//scope
		for (int i = 0; i < scopes.length; i++) {
			buf.append(scopes[i]);
			buf.append(DELIM);
		}
		//naming authority
		buf.append(namingAuthority);

		typeName = buf.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceCompareTo(org.eclipse.ecf.core.identity.BaseID)
	 */
	protected int namespaceCompareTo(BaseID o) {
		if (o instanceof ServiceTypeID) {
			final ServiceTypeID other = (ServiceTypeID) o;
			final String typename = other.getName();
			return getName().compareTo(typename);
		}
		return 1;
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
		return typeName;
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
		final StringBuffer buf = new StringBuffer("ServiceTypeID[");
		buf.append("typeName=").append(typeName).append("]");
		return buf.toString();
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof IServiceTypeID)) {
			return false;
		}
		final IServiceTypeID stid = (ServiceTypeID) o;
		return stid.getName().equals(getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.identity.IServiceTypeID#getInternal()
	 */
	public String getInternal() {
		return typeName;
	}
}
