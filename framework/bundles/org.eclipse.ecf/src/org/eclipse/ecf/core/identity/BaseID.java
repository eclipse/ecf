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

public abstract class BaseID implements ID {

    Namespace namespace;

	protected BaseID() {}

	protected BaseID(Namespace namespace) {
        if (namespace == null)
            throw new RuntimeException(new InstantiationException(
                    "namespace cannot be null"));
        this.namespace = namespace;
    }

    public int compareTo(Object o) {
        if (o == null || !(o instanceof BaseID))
            throw new ClassCastException("incompatible types for compare");
        return namespace.getCompareToForObject(this, (BaseID) o);
    }
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof BaseID)) {
            return false;
        }
        return namespace.testIDEquals(this, (BaseID) o);
    }
    public String getName() {
        return namespace.getNameForID(this);
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public int hashCode() {
        return namespace.getHashCodeForID(this);
    }
    protected abstract int namespaceCompareTo(BaseID o);
    protected abstract boolean namespaceEquals(BaseID o);
    protected abstract String namespaceGetName();
    protected abstract int namespaceHashCode();
    protected abstract URI namespaceToURI() throws URISyntaxException;
    public URI toURI() throws URISyntaxException {
        return namespace.getURIForID(this);
    }
}