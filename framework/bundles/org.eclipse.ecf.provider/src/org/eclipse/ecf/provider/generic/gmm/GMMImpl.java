/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
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

import java.util.*;
import org.eclipse.ecf.core.identity.ID;

public class GMMImpl extends Observable {
	TreeSet mySet;

	public GMMImpl() {
		mySet = new TreeSet();
	}

	public boolean addMember(Member m) {
		final boolean res = mySet.add(m);
		if (res) {
			setChanged();
			notifyObservers(new MemberChanged(m, true));
		}
		return res;
	}

	public boolean removeMember(Member m) {
		final boolean res = mySet.remove(m);
		if (res) {
			setChanged();
			notifyObservers(new MemberChanged(m, false));
		}
		return res;
	}

	public void removeAllMembers() {
		final Object members[] = getMembers();
		for (int i = 0; i < members.length; i++) {
			removeMember((Member) members[i]);
		}
	}

	public Object[] getMembers() {
		return mySet.toArray();
	}

	public ID[] getMemberIDs(ID exclude) {
		TreeSet newSet = null;
		if (exclude != null) {
			newSet = (TreeSet) mySet.clone();
			newSet.remove(new Member(exclude));
		} else {
			newSet = mySet;
		}
		final ID ids[] = new ID[newSet.size()];
		final Iterator iter = newSet.iterator();
		int j = 0;
		while (iter.hasNext()) {
			ids[j++] = ((Member) iter.next()).getID();
		}
		return ids;
	}

	public int getSize() {
		return mySet.size();
	}

	public boolean containsMember(Member m) {
		return mySet.contains(m);
	}

	public Iterator iterator() {
		return mySet.iterator();
	}

	public String toString() {
		return mySet.toString();
	}
}