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

import org.eclipse.core.runtime.IAdaptable;

/**
 * Contract for ECF identity
 * <p>
 * ECF IDs are immutable once constructed, and unique within the containing
 * {@link Namespace}.
 * <p>
 * ID instances are created via the Namespace.createInstance(...) method. This
 * method is called by the IDFactory.createID(...) methods for the given
 * Namespace. So, for example, to create an ID instance with the name "slewis":
 * 
 * <pre>
 * ID id = IDFactory.getDefault().createID(namespace, &quot;slewis&quot;);
 * </pre>
 * 
 * <p>
 * 
 * @see Namespace
 * 
 */
public interface ID extends java.io.Serializable, java.lang.Comparable,
		java.security.Principal, IAdaptable {

	public boolean equals(Object obj);

	public int hashCode();

	/**
	 * Get the unique name of this identity.
	 * 
	 * @return String unique name for this identity. Must not be null, and must
	 *         be a unique String within the Namespace returned by
	 *         getNamespace()
	 */
	public String getName();

	/**
	 * Get the Namespace instance associated with this identity
	 * 
	 * @return Namespace the Namespace corresponding to this identity. Must not
	 *         return null.
	 */
	public Namespace getNamespace();

	/**
	 * If available, return this identity in URI form. If not available as URI,
	 * throw URISyntaxException
	 * 
	 * @return URI the URI representation of this identity. Will not return
	 *         null.
	 * @throws URISyntaxException
	 *             if this ID cannot be converted to URI form
	 */
	public URI toURI() throws URISyntaxException;
}