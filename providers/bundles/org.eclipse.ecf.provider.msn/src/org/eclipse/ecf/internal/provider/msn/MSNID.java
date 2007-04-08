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
import org.eclipse.ecf.presence.im.IChatID;

class MSNID implements IChatID, ID {

	private final Namespace namespace;

	private final String email;

	private String userName;

	MSNID(Namespace namespace, String email) {
		this.namespace = namespace;
		this.email = email;
	}

	MSNID(Namespace namespace, String email, String userName) {
		this.namespace = namespace;
		this.email = email;
		this.userName = userName;
	}

	void setUserName(String userName) {
		this.userName = userName;
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
		if (another instanceof MSNID) {
			return email.compareTo(((MSNID) another).email);
		} else {
			throw new ClassCastException();
		}
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IChatID.class)) {
			return this;
		} else {
			return null;
		}
	}

	public String getUsername() {
		return userName == null ? email : userName;
	}

	public int hashCode() {
		return email.hashCode() ^ -31;
	}

	public boolean equals(Object obj) {
		if (obj instanceof MSNID) {
			return email.equals(((MSNID) obj).email);
		} else {
			return false;
		}
	}

	public String toString() {
		return email;
	}

}
