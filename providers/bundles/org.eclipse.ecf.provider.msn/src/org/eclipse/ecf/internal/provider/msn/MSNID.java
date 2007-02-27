/****************************************************************************
 * Copyright (c) 2006, 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.msn;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;

final class MSNID implements ID {
	
	private final Namespace namespace;

	private final String email;

	MSNID(MSNNamespace namespace, String email) {
		this.namespace = namespace;
		this.email = email;
	}

	public String getName() {
		return email;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public String toExternalForm() {
		return email;
	}

	public int compareTo(Object another) {
		if (another == this) {
			return 0;
		} else {
			return email.compareTo(((MSNID) another).email);
		}
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof MSNID) {
			return email.equals(((MSNID) obj).email);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return MSNID.class.hashCode() ^ email.hashCode();
	}

	public String toString() {
		return toExternalForm();
	}

}
