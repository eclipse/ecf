package org.eclipse.ecf.internal.impl.standalone.gmm;

public class ItemChange {
	Item item;
	boolean added;

	public ItemChange(Item item, boolean added) {
		this.item = item;
		this.added = added;
	}

	public Item getItem() {
		return item;
	}

	public boolean isAdd() {
		return added;
	}
}