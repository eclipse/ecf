package org.eclipse.ecf.internal.impl.standalone.gmm;

import java.util.Observable;
import java.util.TreeSet;
import java.util.Iterator;

import org.eclipse.ecf.core.identity.ID;

public class GroupManager extends Observable {
	TreeSet items;

	public GroupManager() {
		items = new TreeSet();
	}

	public boolean addItem(Item m) {
		boolean res = items.add(m);
		if (res) {
			setChanged();
			notifyObservers(new ItemChange(m, true));
		}
		return res;
	}

	public boolean removeItem(Item m) {
		boolean res = items.remove(m);
		if (res) {
			setChanged();
			notifyObservers(new ItemChange(m, false));
		}
		return res;
	}

	public void removeAllItems() {
		Object items[] = getItems();
		for (int i = 0; i < items.length; i++) {
			removeItem((Item) items[i]);
		}
	}

	public Object[] getItems() {
		return items.toArray();
	}

	public ID[] getItemIDs(ID exclude) {
		TreeSet newSet = null;
		if (exclude != null) {
			newSet = (TreeSet) items.clone();
			newSet.remove(new Item(exclude));
		} else {
			newSet = items;
		}
		ID ids[] = new ID[newSet.size()];
		Iterator iter = newSet.iterator();
		int j = 0;
		while (iter.hasNext()) {
			ids[j++] = (ID) ((Item) iter.next()).getID();
		}
		return ids;
	}

	public int getSize() {
		return items.size();
	}

	public boolean containsItem(Item m) {
		return items.contains(m);
	}

	public Iterator iterator() {
		return items.iterator();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("GMM").append(items);
		return sb.toString();
	}
}