/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.provider.generic.gmm;

import org.eclipse.ecf.core.identity.ID;

public class Member implements Comparable {
	ID member;
	Object data;

	public Member(ID member) {
		this(member, null);
	}

	public Member(ID member, Object data) {
		this.member = member;
		this.data = data;
	}

	public boolean equals(Object o) {
		if (o != null && o instanceof Member) {
			return member.equals(((Member) o).member);
		}
		return false;
	}

	public int hashCode() {
		return member.hashCode();
	}

	public int compareTo(Object o) {
		if (o != null && o instanceof Member) {
			return member.compareTo(((Member) o).member);
		}
		return 0;
	}

	public ID getID() {
		return member;
	}

	public Object getData() {
		return data;
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Member[").append(member).append(";").append(data) //$NON-NLS-1$ //$NON-NLS-2$
				.append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}