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

/**
 * Identity type to represent discovered service
 * 
 */
public class ServiceID extends BaseID {
	private static final long serialVersionUID = 1L;

	protected String type;

	protected String name;

	protected ServiceID(Namespace namespace, String type, String name) {
		super(namespace);
		if (type == null)
			throw new NullPointerException("ServiceID type cannot be null");
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

	protected int namespaceCompareTo(BaseID o) {
		if (o instanceof ServiceID) {
			ServiceID other = (ServiceID) o;
			String typename = other.getFullyQualifiedName();
			return getFullyQualifiedName().compareTo(typename);
		} else {
			return 1;
		}
	}

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

	protected String namespaceGetName() {
		return getFullyQualifiedName();
	}

	protected int namespaceHashCode() {
		return getFullyQualifiedName().hashCode();
	}

	public String getServiceType() {
		return type;
	}

	public String getServiceName() {
		return name;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ServiceID[");
		buf.append("type=").append(type).append(";name=").append(name).append(
				";full=" + getFullyQualifiedName()).append("]");
		return buf.toString();
	}
}
