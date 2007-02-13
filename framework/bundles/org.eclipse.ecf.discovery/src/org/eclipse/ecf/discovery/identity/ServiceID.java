/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.discovery.identity;

import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.discovery.Messages;

/**
 * Service identity type.  ServiceIDs are IDs that uniquely identify
 * a remote service.  Subclasses may be created as appropriate.
 * 
 */
public class ServiceID extends BaseID {
	private static final long serialVersionUID = 1L;

	protected String type;

	protected String name;

	protected ServiceID(Namespace namespace, String type, String name) {
		super(namespace);
		if (type == null)
			throw new NullPointerException(Messages.getString("ServiceID.ServiceID_Not_Null")); //$NON-NLS-1$
		this.type = type;
		this.name = name;
	}

	public ServiceID(String type, String name) {
		this.name = name;
		this.type = type;
	}

	protected String getFullyQualifiedName() {
		if (name == null)
			return type;
		else
			return type + name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceCompareTo(org.eclipse.ecf.core.identity.BaseID)
	 */
	protected int namespaceCompareTo(BaseID o) {
		if (o instanceof ServiceID) {
			ServiceID other = (ServiceID) o;
			String typename = other.getFullyQualifiedName();
			return getFullyQualifiedName().compareTo(typename);
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
		if (o instanceof ServiceID) {
			ServiceID other = (ServiceID) o;
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
		return getFullyQualifiedName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceHashCode()
	 */
	protected int namespaceHashCode() {
		return getFullyQualifiedName().hashCode();
	}

	/**
	 * Get service type for this ID.
	 * @return String service type.  Will not be <code>null</code>.
	 */
	public String getServiceType() {
		return type;
	}

	/**
	 * Get service name for this ID.  
	 * 
	 * @return String service name.  Will not be <code>null</code>.
	 */
	public String getServiceName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ServiceID["); //$NON-NLS-1$
		buf.append("type=").append(type).append(";name=").append(name).append( //$NON-NLS-1$ //$NON-NLS-2$
				";full=" + getFullyQualifiedName()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
