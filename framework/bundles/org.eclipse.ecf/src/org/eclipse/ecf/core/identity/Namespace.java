/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.identity;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Namespace class. This class defines the properties associated with an
 * identity Namespace. Namespaces are defined by a unique 'name' (e.g. 'email',
 * 'icq', 'aolim'), and an 'instantiatorClass'. The instantiator class defines a
 * class that implements the org.eclipse.ecf.identity.provider.IDInstantiator
 * interface and is responsible for creating instances of the given namespace.
 * The instances created by the instantiatorClass <b>must </b> implement the
 * org.eclipse.ecf.identity.ID interface, but otherwise can provide any other
 * identity functionality desired.
 * 
 */
public abstract class Namespace implements Serializable {
	private static final long serialVersionUID = 3976740272094720312L;
	private String name;
	private String description;
	private int hashCode;
	private boolean isInitialized = false;

	public Namespace() {
	}

	public final boolean initialize(String name, String desc) {
		if (name == null)
			throw new RuntimeException(new InstantiationException(
					"Namespace<init> name cannot be null"));
		if (!isInitialized) {
			this.name = name;
			this.description = desc;
			this.hashCode = name.hashCode();
			return true;
		} else
			return false;
	}

	public Namespace(String name, String desc) {
		initialize(name, desc);
	}

	/**
	 * Override of Object.equals. This equals method returns true if the
	 * provided Object is also a Namespace instance, and the names of the two
	 * instances match.
	 * 
	 * @param other
	 *            the Object to test for equality
	 */
	public boolean equals(Object other) {
		if (!(other instanceof Namespace))
			return false;
		return ((Namespace) other).name.equals(name);
	}

	public int hashCode() {
		return hashCode;
	}

	protected boolean testIDEquals(BaseID first, BaseID second) {
		// First check that namespaces are the same and non-null
		Namespace sn = second.getNamespace();
		if (sn == null || !this.equals(sn))
			return false;
		return first.namespaceEquals(second);
	}

	protected String getNameForID(BaseID id) {
		return id.namespaceGetName();
	}

	protected URI getURIForID(BaseID id) throws URISyntaxException {
		return id.namespaceToURI();
	}

	protected int getCompareToForObject(BaseID first, BaseID second) {
		return first.namespaceCompareTo(second);
	}

	protected int getHashCodeForID(BaseID id) {
		return id.namespaceHashCode();
	}

	/**
	 * @return String name of Namespace instance
	 * 
	 */
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public abstract ID makeInstance(Class[] argTypes, Object[] args)
			throws IDInstantiationException;

	public String toString() {
		StringBuffer b = new StringBuffer("Namespace[");
		b.append(name).append(";");
		b.append(description).append("]");
		return b.toString();
	}
}