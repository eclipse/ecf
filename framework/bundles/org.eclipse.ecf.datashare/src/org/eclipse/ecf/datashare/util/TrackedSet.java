/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author pnehrer
 */
public class TrackedSet extends NotifyingSet implements
		NotifyingSet.IChangeListener {

	private static final long serialVersionUID = 3258134660897847607L;

	private transient LinkedList changes = new LinkedList();

	/**
	 * 
	 */
	public TrackedSet() {
		addChangeListener(this);
	}

	/**
	 * @param set
	 */
	public TrackedSet(Set set) {
		super(set);
		addChangeListener(this);
	}

	public synchronized ChangeDelta[] getChanges() {
		return (ChangeDelta[]) changes.toArray(new ChangeDelta[changes.size()]);
	}

	/**
	 * @param deltas
	 */
	public synchronized void apply(ChangeDelta[] deltas) {
		removeChangeListener(this);
		LinkedList oldChanges = new LinkedList(changes);
		while (!changes.isEmpty())
			undo((ChangeDelta) changes.removeLast());

		for (int i = 0; i < deltas.length; ++i)
			apply(deltas[i]);

		addChangeListener(this);
		for (Iterator i = oldChanges.iterator(); i.hasNext();)
			apply((ChangeDelta) i.next());
	}

	private void undo(ChangeDelta delta) {
		switch (delta.getKind()) {
		case ChangeDelta.ADD:
			remove(delta.getMember());
			break;
		case ChangeDelta.REMOVE:
			add(delta.getMember());
			break;
		}
	}

	private void apply(ChangeDelta delta) {
		switch (delta.getKind()) {
		case ChangeDelta.ADD:
			add(delta.getMember());
			break;
		case ChangeDelta.REMOVE:
			remove(delta.getMember());
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.util.NotifyingSet.IChangeListener#changed(org.eclipse.ecf.datashare.util.NotifyingSet.ChangeEvent)
	 */
	public synchronized void changed(ChangeEvent e) {
		ChangeDelta[] deltas = e.getChangeDeltas();
		for (int i = 0; i < deltas.length; ++i)
			changes.add(deltas[i]);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		changes = new LinkedList();
		addChangeListener(this);
	}
}
