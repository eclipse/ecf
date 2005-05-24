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
import java.io.Serializable;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pnehrer
 */
public class NotifyingSet implements Serializable {

	private static final long serialVersionUID = 3258134639355967536L;

	public static interface IChangeListener extends EventListener {

		void changed(ChangeEvent e);
	}

	public static class ChangeDelta implements Serializable {

		private static final long serialVersionUID = 3618133446644806960L;

		public static final short ADD = 0;

		public static final short REMOVE = 1;

		protected transient final NotifyingSet source;

		protected final Object member;

		private final short kind;

		protected ChangeDelta(NotifyingSet source, Object member, short kind) {
			this.source = source;
			this.member = member;
			this.kind = kind;
		}

		public final Object getMember() {
			return member;
		}

		public final short getKind() {
			return kind;
		}
	}

	public static class ChangeEvent extends EventObject {

		private static final long serialVersionUID = 3834307341121041721L;

		private final ChangeDelta[] deltas;

		protected ChangeEvent(NotifyingSet source, ChangeDelta[] deltas) {
			super(source);
			this.deltas = deltas;
		}

		protected ChangeEvent(NotifyingSet source, ChangeDelta delta) {
			this(source, new ChangeDelta[] { delta });
		}

		public NotifyingSet getTrackedSet() {
			return (NotifyingSet) source;
		}

		public ChangeDelta[] getChangeDeltas() {
			return deltas;
		}
	}

	private final HashSet set;

	private transient Set listeners = Collections
			.synchronizedSet(new HashSet());

	public NotifyingSet() {
		set = new HashSet();
	}

	public NotifyingSet(Set set) {
		this.set = new HashSet(set);
	}

	public void addChangeListener(IChangeListener l) {
		listeners.add(l);
	}

	public void removeChangeListener(IChangeListener l) {
		listeners.remove(l);
	}

	public synchronized boolean add(Object object) {
		boolean result = set.add(object);
		if (result)
			fireChangeEvent(new ChangeEvent(this, new ChangeDelta(this, object,
					ChangeDelta.ADD)));

		return result;
	}

	public synchronized boolean remove(Object object) {
		boolean result = set.remove(object);
		if (result)
			fireChangeEvent(new ChangeEvent(this, new ChangeDelta(this, object,
					ChangeDelta.REMOVE)));

		return result;
	}

	public synchronized boolean contains(Object object) {
		return set.contains(object);
	}
	
	public synchronized int size() {
		return set.size();
	}

	public synchronized Object[] toArray() {
		return set.toArray();
	}

	private void fireChangeEvent(ChangeEvent e) {
		IChangeListener[] l = (IChangeListener[]) listeners
				.toArray(new IChangeListener[listeners.size()]);
		for (int i = 0; i < l.length; ++i)
			l[i].changed(e);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		listeners = Collections.synchronizedSet(new HashSet());
	}
}