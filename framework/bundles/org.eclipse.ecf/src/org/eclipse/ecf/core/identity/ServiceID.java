/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.identity;

import java.net.URI;
import java.net.URISyntaxException;


public class ServiceID extends BaseID {

	private static final long serialVersionUID = 1L;

	String type;
	String name;
	
	protected ServiceID(Namespace namespace, String type, String name) {
		super(namespace);
		if (type == null) throw new NullPointerException("ServiceID type cannot be null");
		if (name == null) throw new NullPointerException("ServiceID name cannot be null");
		this.type = type;
		this.name = name;
	}

	protected String getFullyQualifiedName() {
		return type+name;
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
		if (o == null) return false;
		if (o instanceof ServiceID) {
			ServiceID other = (ServiceID) o;
			if (other.getServiceType().equals(type) && other.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	protected String namespaceGetName() {
		return name;
	}

	protected int namespaceHashCode() {
		return name.hashCode();
	}

	protected URI namespaceToURI() throws URISyntaxException {
		throw new URISyntaxException("cannot create URI from service id with name "+getName(),getName());
	}
	public String getServiceType() {
		return type;
	}
}
