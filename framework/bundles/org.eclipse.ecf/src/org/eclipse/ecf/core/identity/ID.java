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

/**
 * Contract for ECF identity.
 * <p>
 * The contract defined by this interface is similar-to, but stronger than that
 * implied by Object.equals() and Object.hashCode.
 * <p>
 * Any classes implementing this interface must provide instances that are
 * unique within the namespace returned by getNamespace(), rather than unique
 * just within the JVM that currently contains the object instance.
 * <p>
 * So, for example, if there are two instances of an email Namespace, equals
 * should return true if the email addresses are the same, and false if they are
 * not the same.
 * <p>
 * Typically, Namespaces are registered with the IDFactory class (via
 * addNamespace) in order to allow the custom creation of instances that
 * implement this interface.
 * 
 */

public interface ID extends java.io.Serializable, java.lang.Comparable,
        java.security.Principal {

    public boolean equals(Object obj);

    /**
     * Get the unique name of this identity.
     * 
     * @return String unique name for this identity
     */
    public String getName();

    /**
     * Get the Namespace instance associated with this identity
     * 
     * @return Namespace the Namespace corresponding to this identity
     */
    public Namespace getNamespace();

    public int hashCode();
    /**
     * If available, return this identity in URI form. If not available, throw
     * URISyntaxException
     * 
     * @return URI the URI representation of this identity
     */
    public URI toURI() throws URISyntaxException;
}