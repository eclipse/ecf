package org.eclipse.ecf.internal.impl.standalone.gmm;

import org.eclipse.ecf.core.identity.ID;

public class Item implements Comparable {
	ID itemID;
	Object itemData;

	public Item(ID id) {
		this(id, null);
	}

	public Item(ID id, Object data) {
		itemID = id;
		itemData = data;
	}

	public boolean equals(Object o) {
		if (o != null && o instanceof Item) {
			return itemID.equals(((Item) o).itemID);
		} else
			return false;
	}

	public int hashCode() {
		return itemID.hashCode();
	}

	public int compareTo(Object o) {
		if (o != null && o instanceof Item) {
			return itemID.compareTo(((Item) o).itemID);
		} else
			return 0;
	}

	public ID getID() {
		return itemID;
	}

	public Object getData() {
		return itemData;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Item[").append(itemID).append(";").append(
			itemData).append(
			"]");
		return sb.toString();
	}

}